package crawler;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SellinitumGs {
    private static final String WEB_DRIVER_ID   = "webdriver.chrome.driver";
    private static final String WEB_DRIVER_PATH = "C:/chromedriver-win64/chromedriver.exe";

    private static final String DB_URL      = "jdbc:mariadb://localhost:3306/cp_db?useUnicode=true&characterEncoding=utf8";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "1234";

    // 정규식 패턴 (텍스트 백업용)
    private static final Pattern NAME_PATTERN  =
            Pattern.compile("<p\\s+class=\"tit\">([^<]+)</p>");
    private static final Pattern PRICE_PATTERN =
            Pattern.compile("<span\\s+class=\"cost\">\\s*([^<]+?)\\s*<span>");
    private static final Pattern PROMO_PATTERN =
            Pattern.compile("<div\\s+class=\"flag_box[^\"]*?\">.*?<span[^>]*>([^<]+)</span>", Pattern.DOTALL);

    public static void main(String[] args) {

        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1400,900");
        options.addArguments("--lang=ko-KR");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        // options.addArguments("--headless=new");

        WebDriver driver = new ChromeDriver(options);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.get("http://gs25.gsretail.com/gscvs/ko/products/event-goods");
            Thread.sleep(1500);

            // TOTAL 탭 클릭 (전체)
            clickTotalTab(driver);

            // 첫 페이지
            crawlOnePageAndInsert(conn, driver, 1);

            // 2페이지~
            for (int page = 2; page <= 2000; page++) {
                System.out.println("===== 페이지 " + page + "로 이동 시도 =====");
                boolean moved = goToPage(driver, page);
                if (!moved) {
                    System.out.println("더 이상 이동 불가 -> 종료");
                    break;
                }

                Thread.sleep(800);

                int inserted = crawlOnePageAndInsert(conn, driver, page);
                if (inserted == 0) {
                    System.out.println("상품 0건 -> 종료");
                    break;
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    // ======================
    // TOTAL 탭(전체) 눌러서 '전체' 상품 기준으로 보이게
    // ======================
    private static void clickTotalTab(WebDriver driver) throws InterruptedException {
        WebElement totalTab = driver.findElement(By.id("TOTAL"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", totalTab);
        Thread.sleep(1000);
    }

    // ======================
    // 페이지네이션: 원하는 페이지 번호 클릭 시도
    //    - 현재 블록에서 번호 직접 클릭
    //    - 없으면 next 눌러서 다음 블록 가서 재시도 (재귀)
    // ======================
    private static boolean goToPage(WebDriver driver, int targetPage) throws InterruptedException {

        String currentPageText = null;
        try {
            WebElement currentOn = driver.findElement(By.cssSelector(".paging .num a.on"));
            currentPageText = currentOn.getText().trim();
        } catch (NoSuchElementException ignore) {}

        if (currentPageText != null && currentPageText.equals(String.valueOf(targetPage))) {
            System.out.println("이미 페이지 " + targetPage + " 상태");
            return true;
        }

        List<WebElement> pageLinks = driver.findElements(By.cssSelector(".paging .num a"));
        for (WebElement link : pageLinks) {
            String txt = link.getText().trim();
            if (txt.equals(String.valueOf(targetPage))) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
                Thread.sleep(800);
                return true;
            }
        }

        // targetPage가 현재 블록에 없으면 next(>) 클릭해보고 다시 시도
        try {
            WebElement nextBtn = driver.findElement(By.cssSelector(".paging a.next"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextBtn);
            Thread.sleep(800);
        } catch (NoSuchElementException e) {
            System.out.println("next 버튼 없음 -> targetPage " + targetPage + " 이동 실패");
            return false;
        }

        return goToPage(driver, targetPage);
    }

    // ======================
    // 한 페이지 크롤 -> DB insert
    // ======================
    private static int crawlOnePageAndInsert(Connection conn, WebDriver driver, int pageNo) throws SQLException {

        // 현재 페이지의 상품 li들 모으기
        List<WebElement> prodLists = driver.findElements(By.cssSelector("ul.prod_list"));
        List<WebElement> itemNodes = null;

        if (!prodLists.isEmpty()) {
            WebElement firstList = prodLists.get(0);
            itemNodes = firstList.findElements(By.cssSelector(":scope > li"));
        }
        if (itemNodes == null || itemNodes.isEmpty()) {
            // 혹시 구조가 다르면 fallback
            itemNodes = driver.findElements(By.cssSelector(".prod_box"));
        }

        System.out.println("["+pageNo+"페이지] 상품 노드 수: " + itemNodes.size());

        int insertCount = 0;

        for (WebElement item : itemNodes) {

            // prod_box 엘리먼트 확보
            WebElement boxEl;
            if (item.getAttribute("class") != null && item.getAttribute("class").contains("prod_box")) {
                boxEl = item;
            } else {
                try {
                    boxEl = item.findElement(By.cssSelector(".prod_box"));
                } catch (NoSuchElementException e) {
                    continue;
                }
            }

            // prod_box 전체 HTML
            String html = boxEl.getAttribute("outerHTML");

            // 1) 상품명
            String productName = matchOne(NAME_PATTERN, html);

            // 2) 가격
            String rawPrice = matchOne(PRICE_PATTERN, html); // 예: "5,400"
            Integer price = null;
            if (rawPrice != null) {
                String digits = rawPrice.replaceAll("[^0-9]", "");
                if (!digits.isEmpty()) {
                    price = Integer.parseInt(digits);
                }
            }

            // 3) 행사타입 판정 로직 (여기가 핵심 수정)
            //    1차: flag_box의 class에서 직접 판별
            String promoTypeEnum = "NONE";
            try {
                WebElement flagBox = boxEl.findElement(By.cssSelector("div.flag_box"));
                String flagClass = flagBox.getAttribute("class"); // ex) "flag_box GIFT", "flag_box ONE_TO_ONE"
                promoTypeEnum = mapPromoTypeFromFlagClass(flagClass);
            } catch (NoSuchElementException ignore) {
                // flag_box 없으면 뒤에서 텍스트 기반 처리
            }

            //    2차 백업: flag_box로 못 잡은 경우 텍스트로 판정
            if ("NONE".equals(promoTypeEnum)) {
                // 정규식으로 "덤증정", "1+1" 이런 라벨 텍스트 추출 시도
                String promoText = matchOne(PROMO_PATTERN, html);

                // promoText가 비어있는데도 html 안에 "증정"/"덤" 단어가 들어있으면 GIFT 취급
                if ((promoText == null || promoText.isBlank())
                        && (html.contains("증정") || html.contains("덤"))) {
                    promoText = "증정";
                }

                promoTypeEnum = mapPromoTypeFromText(promoText);
            }

            // 4) 이미지 URL
            String imageUrl = null;
            try {
                WebElement imgEl = boxEl.findElement(By.cssSelector("p.img img"));
                imageUrl = imgEl.getAttribute("src");
            } catch (NoSuchElementException ignore) {}

            // 디버그 출력
            System.out.println("----");
            System.out.println("page        : " + pageNo);
            System.out.println("productName : " + productName);
            System.out.println("price       : " + price);
            System.out.println("imageUrl    : " + imageUrl);
            System.out.println("promoType   : " + promoTypeEnum);

            // 상품명 없으면 placeholder일 수 있으니 skip
            if (productName == null || productName.isBlank()) {
                continue;
            }

            // DB insert
            insertProduct(conn, "GS25", productName, price, imageUrl, promoTypeEnum);
            insertCount++;
        }

        System.out.println("["+pageNo+"페이지] insertCount = " + insertCount);
        return insertCount;
    }

    // ======================
    // 유틸: 정규식 첫 그룹만 추출
    // ======================
    private static String matchOne(Pattern p, String html) {
        Matcher m = p.matcher(html);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }

    // ======================
    // flag_box의 class → promo_type 매핑
    //
    // 예:
    //   class="flag_box ONE_TO_ONE" → "ONE_PLUS_ONE"
    //   class="flag_box TWO_TO_ONE" → "TWO_PLUS_ONE"
    //   class="flag_box GIFT"       → "GIFT"
    // ======================
    private static String mapPromoTypeFromFlagClass(String flagClass) {
        if (flagClass == null) return "NONE";

        String norm = flagClass.toUpperCase();
        if (norm.contains("ONE_TO_ONE")) return "ONE_PLUS_ONE";
        if (norm.contains("TWO_TO_ONE")) return "TWO_PLUS_ONE";
        if (norm.contains("GIFT"))       return "GIFT";

        return "NONE";
    }

    // ======================
    // promoText(텍스트 라벨) → promo_type 매핑 (백업용)
    //
    // 예: "1+1", "2+1", "덤증정", "사은품 증정"
    // ======================
    private static String mapPromoTypeFromText(String promoText) {
        if (promoText == null) return "NONE";

        // 모든 공백/개행 제거해서 비교를 단순화
        String norm = promoText
                .replaceAll("\\s+", "")
                .trim();

        if (norm.contains("1+1")) return "ONE_PLUS_ONE";
        if (norm.contains("2+1")) return "TWO_PLUS_ONE";

        // "덤증정", "증정", "사은품증정", "덤" 전부 GIFT
        if (norm.contains("증정") || norm.contains("덤") || norm.contains("사은품")) {
            return "GIFT";
        }

        return "NONE";
    }

    // ======================
    // DB insert
    // ======================
    private static void insertProduct(Connection conn,
                                      String sourceChain,
                                      String productName,
                                      Integer price,
                                      String imageUrl,
                                      String promoTypeEnum) throws SQLException {

        // 중복 방지하고 싶으면 DDL 한 번만 실행:
        // ALTER TABLE craw_product
        // ADD UNIQUE KEY uq_source_name_promo (source_chain, product_name, promo_type);
        //
        // 그리고 여기서 INSERT IGNORE 로 바꿔도 됨.

        String sql = "INSERT INTO craw_product " +
                "(source_chain, product_name, price, image_url, promo_type) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sourceChain); // "GS25"
            ps.setString(2, productName);

            if (price != null) {
                ps.setInt(3, price);
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }

            ps.setString(4, imageUrl);
            ps.setString(5, promoTypeEnum);

            ps.executeUpdate();
        }
    }
}
