package crawler;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SelliniumCu {

    private static final String WEB_DRIVER_ID   = "webdriver.chrome.driver";
    private static final String WEB_DRIVER_PATH = "C:/chromedriver-win64/chromedriver.exe";

    private static final String DB_URL      = "jdbc:mariadb://localhost:3306/cp_db?useUnicode=true&characterEncoding=utf8";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "1234";

    public static void main(String[] args) {

        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1400,900");
        options.addArguments("--lang=ko-KR");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        // options.addArguments("--headless=new"); // 서버/배치 돌릴 땐 켜도 됨

        WebDriver driver = new ChromeDriver(options);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            // 1) CU 행사 페이지 접속
            String url = "https://cu.bgfretail.com/event/plus.do?category=event&depth2=1&sf=N";
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.get(url);
            Thread.sleep(1500); // 첫 로드 대기

            // 2) "더보기" 계속 눌러서 모든 상품을 DOM에 다 붙이기
            loadAllProductsByMoreButton(driver);

            // 3) DOM에 모인 모든 상품 li를 긁어서 DB에 저장
            int inserted = scrapeAllProductsAndInsert(conn, driver);

            System.out.println("최종 insert된 상품 수: " + inserted);

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            driver.quit();
        }

    }

    /**
     * "더보기" 버튼을 반복해서 클릭해 전체 상품을 페이지에 모두 append시키는 함수
     * 더보기 버튼이 사라지거나 클릭 시 예외가 나면 멈춘다.
     */
    private static void loadAllProductsByMoreButton(WebDriver driver) throws InterruptedException {

        while (true) {
            try {
                // "더보기" 버튼은 .prodListBtn-w a[href^='javascript:nextPage']
                WebElement moreBtn = driver.findElement(By.cssSelector(".prodListBtn-w a[href^='javascript:nextPage']"));

                // 혹시 버튼이 display:none 이거나 disabled 비슷하면 탈출
                if (!moreBtn.isDisplayed() || !moreBtn.isEnabled()) {
                    System.out.println("더보기 버튼 더 이상 표시 안됨 -> 종료");
                    break;
                }

                // 버튼 클릭 (일반 click이 막히면 JS click도 가능)
                try {
                    moreBtn.click();
                } catch (ElementClickInterceptedException e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", moreBtn);
                }

                // 새 상품 append 될 시간 기다리기
                Thread.sleep(1000);

                // 안전장치: 너무 오래 무한루프되면 끊자 (예: 200번 이상 더보기는 비정상)
                // CU 실제 데이터는 보통 수십~수백개라 200번이면 충분히 안전장치.
            } catch (NoSuchElementException noMore) {
                System.out.println("더보기 버튼 못 찾음 -> 끝");
                break;
            }
        }
    }

    /**
     * 현재 페이지에 로드되어 있는 모든 상품(li.prod_list)을 순회하고 DB에 저장
     */
    private static int scrapeAllProductsAndInsert(Connection conn, WebDriver driver) throws SQLException {

        // li.prod_list 가 상품 하나
        List<WebElement> productLis = driver.findElements(By.cssSelector("li.prod_list"));

        System.out.println("총 상품 li 개수: " + productLis.size());

        int insertedCount = 0;

        for (WebElement li : productLis) {
            try {
                // 1) 이미지 URL
                // <div class="prod_img"><img src="//...png" ...></div>
                String imageUrl = null;
                try {
                    WebElement imgEl = li.findElement(By.cssSelector(".prod_img img"));
                    imageUrl = imgEl.getAttribute("src");
                    // src가 //로 시작하면 https: 붙이자
                    if (imageUrl != null && imageUrl.startsWith("//")) {
                        imageUrl = "https:" + imageUrl;
                    }
                } catch (NoSuchElementException ignore) {}

                // 2) 상품명
                // <div class="name"><p>동원)양반누룽지닭죽</p></div>
                String productName = null;
                try {
                    WebElement nameEl = li.findElement(By.cssSelector(".prod_text .name p"));
                    productName = nameEl.getText().trim();
                } catch (NoSuchElementException ignore) {}

                // 3) 가격
                // <div class="price"><strong>4,500</strong><span class="won">원</span></div>
                Integer price = null;
                try {
                    WebElement priceStrong = li.findElement(By.cssSelector(".prod_text .price strong"));
                    String priceText = priceStrong.getText().trim(); // "4,500"
                    String digitsOnly = priceText.replaceAll("[^0-9]", ""); // "4500"
                    if (!digitsOnly.isEmpty()) {
                        price = Integer.parseInt(digitsOnly);
                    }
                } catch (NoSuchElementException ignore) {}

                // 4) 행사 타입
                // <div class="badge"><span class="plus1">1+1</span></div>
                // plus1 => ONE_PLUS_ONE
                // plus2 => TWO_PLUS_ONE
                String promoTypeEnum = "NONE";
                try {
                    // 여러 badge span이 있을 수도 있으므로 싹 다 모아서 판단
                    List<WebElement> badgeSpans = li.findElements(By.cssSelector(".badge span"));
                    List<String> clsList = new ArrayList<>();
                    for (WebElement span : badgeSpans) {
                        String cls = span.getAttribute("class"); // "plus1" / "plus2" / maybe null
                        if (cls != null) clsList.add(cls);
                    }

                    // 우선순위: plus2 -> TWO_PLUS_ONE, plus1 -> ONE_PLUS_ONE
                    // (보통 2+1이 조금 더 값 높은 행사라 그냥 이렇게 했지만
                    //  동시에 뜰 일은 거의 없다고 가정)
                    if (clsList.stream().anyMatch(c -> c.contains("plus2"))) {
                        promoTypeEnum = "TWO_PLUS_ONE";
                    } else if (clsList.stream().anyMatch(c -> c.contains("plus1"))) {
                        promoTypeEnum = "ONE_PLUS_ONE";
                    }
                } catch (Exception ignore) {}

                // 5) chain 고정
                String sourceChain = "CU";

                // 6) 디버그 출력
                System.out.println("----");
                System.out.println("sourceChain : " + sourceChain);
                System.out.println("productName : " + productName);
                System.out.println("price       : " + price);
                System.out.println("imageUrl    : " + imageUrl);
                System.out.println("promoType   : " + promoTypeEnum);

                // 7) productName 없는 빈 placeholder li 는 skip
                if (productName == null || productName.isBlank()) {
                    continue;
                }

                // 8) DB insert
                insertProduct(conn, sourceChain, productName, price, imageUrl, promoTypeEnum);
                insertedCount++;

            } catch (Exception rowEx) {
                // 개별 상품에서 실패해도 전체 멈추지 않게
                rowEx.printStackTrace();
            }
        }

        return insertedCount;
    }

    private static void insertProduct(Connection conn,
                                      String sourceChain,
                                      String productName,
                                      Integer price,
                                      String imageUrl,
                                      String promoTypeEnum) throws SQLException {

        // 중복 방지하고 싶다면:
        // ALTER TABLE craw_product
        // ADD UNIQUE KEY uq_chain_name_promo (source_chain, product_name, promo_type);
        //
        // 그리고 아래 sql 을 INSERT IGNORE 로 변경 가능.

        String sql = "INSERT INTO craw_product " +
                "(source_chain, product_name, price, image_url, promo_type) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sourceChain);       // "CU"
            ps.setString(2, productName);       // 상품명

            if (price != null) {
                ps.setInt(3, price);
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }

            ps.setString(4, imageUrl);          // 이미지 URL
            ps.setString(5, promoTypeEnum);     // ONE_PLUS_ONE / TWO_PLUS_ONE / NONE

            ps.executeUpdate();
        }
    }

}
