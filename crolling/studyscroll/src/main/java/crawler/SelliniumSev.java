package crawler;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;

public class SelliniumSev {


    // ====== ChromeDriver / DB 설정 ======
    private static final String WEB_DRIVER_ID   = "webdriver.chrome.driver";
    private static final String WEB_DRIVER_PATH = "C:/chromedriver-win64/chromedriver.exe";

    private static final String DB_URL      = "jdbc:mariadb://localhost:3306/cp_db?useUnicode=true&characterEncoding=utf8";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "1234";

    private static final String BASE_URL    = "https://www.7-eleven.co.kr"; // 상대경로 이미지용


    public static void main(String[] args) {

        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1400,900");
        options.addArguments("--lang=ko-KR");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        // options.addArguments("--headless=new"); // 서버/배치 환경이면 켜도 됨

        WebDriver driver = new ChromeDriver(options);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            // 1) 세븐일레븐 행사 페이지 진입
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.get("https://www.7-eleven.co.kr/product/presentList.asp");
            Thread.sleep(1500);

            // 2) 각 탭 순회
            // ⚠️ 탭 버튼 셀렉터는 실제 html 보고 맞춰야 한다.
            // 보통은 <a href="javascript:fncTab('1');">1+1 행사</a> 이런 식이라면 아래가 맞다.

            scrapeOneTabAndInsertAll(
                    conn,
                    driver,
                    By.cssSelector("a[href*=\"fncTab('1')\"]"), // 1+1
                    "ONE_PLUS_ONE"
            );

            scrapeOneTabAndInsertAll(
                    conn,
                    driver,
                    By.cssSelector("a[href*=\"fncTab('2')\"]"), // 2+1
                    "TWO_PLUS_ONE"
            );

            scrapeOneTabAndInsertAll(
                    conn,
                    driver,
                    By.cssSelector("a[href*=\"fncTab('3')\"]"), // 증정행사
                    "GIFT"
            );

            scrapeOneTabAndInsertAll(
                    conn,
                    driver,
                    By.cssSelector("a[href*=\"fncTab('4')\"]"), // 할인행사
                    "NONE"
            );

            System.out.println("세븐일레븐 전체 탭 크롤 완료.");

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    // ====== 탭 전체 처리: 클릭 -> MORE로 전체 펼치기 -> 그 결과만 DB에 INSERT ======
    private static void scrapeOneTabAndInsertAll(Connection conn,
                                                 WebDriver driver,
                                                 By tabSelector,
                                                 String promoTypeForThisTab) throws Exception {

        System.out.println("=== 탭 시작: " + promoTypeForThisTab + " ===");

        // 1) 해당 탭 클릭
        clickTab(driver, tabSelector);

        // 2) MORE 눌러서 전체 로딩 끝까지 (정말 끝일 때까지 반복)
        expandAllWithMore(driver);

        // 3) 전체 상품을 파싱해서 DB에 넣는다
        int insertedCount = extractAndInsertProductsFromCurrentTab(conn, driver, promoTypeForThisTab);

        System.out.println("=== 탭 종료: " + promoTypeForThisTab + " / inserted rows: " + insertedCount + " ===");
    }

    // ====== 탭 클릭 (stale-safe) ======
    private static void clickTab(WebDriver driver, By tabSelector) throws InterruptedException {
        try {
            WebElement tabBtn = driver.findElement(tabSelector);

            try {
                tabBtn.click();
            } catch (ElementClickInterceptedException e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", tabBtn);
            } catch (StaleElementReferenceException e) {
                // stale이면 다시 찾아서 JS로 클릭
                WebElement tabBtn2 = driver.findElement(tabSelector);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", tabBtn2);
            }

            // 탭 바꾼 직후 리스트 초기 로딩 시간
            Thread.sleep(800);

        } catch (NoSuchElementException e) {
            System.out.println("[WARN] 탭 못 찾음: " + tabSelector.toString());
        }
    }

    // ====== MORE 반복: 끝이라고 확신할 때까지 눌러서 전체 상품을 DOM에 다 붙인다 ======
    private static void expandAllWithMore(WebDriver driver) throws InterruptedException {

        int safetyCount = 0;

        while (true) {
            // 1) 현재까지 로드된 상품 수
            int beforeCount = getCurrentProductCount(driver);

            // 2) MORE 버튼 찾기
            List<WebElement> candidates = driver.findElements(
                    By.cssSelector("a[href^='javascript:fncMore'], a[href^=\"javascript: fncMore\"]")
            );

            if (candidates.isEmpty()) {
                System.out.println("MORE 버튼 없음 -> 정말 끝");
                break;
            }

            WebElement moreBtn = candidates.get(0);

            // 3) 클릭 (stale-safe)
            boolean clicked = false;
            try {
                moreBtn.click();
                clicked = true;
            } catch (ElementClickInterceptedException e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", moreBtn);
                clicked = true;
            } catch (StaleElementReferenceException e) {
                // 버튼이 갱신되는 타이밍이면 다시 루프에서 재탐색
                continue;
            }

            if (!clicked) {
                // 클릭 자체가 불가능하면 더 못 불러온다고 판단
                System.out.println("MORE 클릭 실패 -> 종료 추정");
                break;
            }

            // 4) ajax append 기다리기 (조금 여유롭게)
            Thread.sleep(1500);

            // 5) 다시 상품 수 세기
            int afterCount = getCurrentProductCount(driver);
            System.out.println("MORE 클릭 후 상품 수: " + beforeCount + " -> " + afterCount);

            if (afterCount <= beforeCount) {
                // 증가가 없으면 "진짜 끝인지" 한 번 더 확인

                // MORE 버튼이 사라졌는지 다시 본다
                List<WebElement> again = driver.findElements(
                        By.cssSelector("a[href^='javascript:fncMore'], a[href^=\"javascript: fncMore\"]")
                );

                if (again.isEmpty()) {
                    System.out.println("MORE 버튼 사라짐 -> 최종 종료");
                    break;
                } else {
                    // 버튼은 남아있지만 더 이상 상품이 늘지 않음
                    System.out.println("추가 아이템 없음 + 버튼은 남아있지만 반응 없음 -> 종료");
                    break;
                }
            }

            // 안전장치: 혹시 무한루프 못 빠질 때
            safetyCount++;
            if (safetyCount > 500) {
                System.out.println("MORE 무한 루프 의심 -> 강제 종료");
                break;
            }
        }
    }

    // ====== 전체 상품을 파싱해서 DB에 저장 (stale-safe) ======
    private static int extractAndInsertProductsFromCurrentTab(Connection conn,
                                                              WebDriver driver,
                                                              String promoTypeFallback) throws SQLException {

        // 먼저 li 개수 snapshot만 잡는다
        List<WebElement> initialList;
        try {
            initialList = driver.findElements(By.cssSelector(".img_list li")); // 필요시 수정
            if (initialList.isEmpty()) {
                initialList = driver.findElements(By.cssSelector("li"));
            }
        } catch (NoSuchElementException e) {
            initialList = driver.findElements(By.cssSelector("li"));
        }

        int liCount = initialList.size();
        System.out.println("현재 탭(" + promoTypeFallback + ") li 개수(초기 스냅샷): " + liCount);

        int insertedCount = 0;

        // 인덱스로 돌면서, 매번 새로 li를 다시 잡는다 (stale 방어)
        for (int idx = 0; idx < liCount; idx++) {
            try {
                WebElement li;

                try {
                    // 최신 DOM에서 다시 li 목록을 가져온다
                    List<WebElement> currentList = driver.findElements(By.cssSelector(".img_list li"));
                    if (currentList.isEmpty()) {
                        currentList = driver.findElements(By.cssSelector("li"));
                    }

                    if (idx >= currentList.size()) {
                        break; // 더 이상 해당 인덱스 없음
                    }

                    li = currentList.get(idx);

                } catch (StaleElementReferenceException se) {
                    // DOM 바뀌는 타이밍에 걸리면 이 아이템은 스킵
                    continue;
                }

                // ===== 데이터 추출 =====

                // 이미지 URL
                String imageUrl = null;
                try {
                    WebElement imgEl = li.findElement(By.cssSelector(".pic_product img"));
                    imageUrl = imgEl.getAttribute("src");
                    if (imageUrl != null && imageUrl.startsWith("/")) {
                        imageUrl = BASE_URL + imageUrl;
                    }
                } catch (NoSuchElementException ignore) {}

                // 상품명 (fallback 포함)
                String productName = null;
                try {
                    WebElement nameEl = li.findElement(By.cssSelector(".pic_product .infowrap .name"));
                    productName = nameEl.getText().trim();
                } catch (NoSuchElementException ignore) {

                    // fallback #1: 이미지 alt
                    try {
                        WebElement imgEl = li.findElement(By.cssSelector(".pic_product img"));
                        String altName = imgEl.getAttribute("alt");
                        if (altName != null && !altName.isBlank()) {
                            productName = altName.trim();
                        }
                    } catch (NoSuchElementException ignore2) {}

                    // fallback #2: 예전 구조 (.product_content .tit_product)
                    if (productName == null || productName.isBlank()) {
                        try {
                            WebElement legacyNameEl = li.findElement(By.cssSelector(".product_content .tit_product"));
                            productName = legacyNameEl.getText().trim();
                        } catch (NoSuchElementException ignore3) {}
                    }
                }

                // 가격 (fallback 포함)
                Integer price = null;
                try {
                    WebElement priceEl = li.findElement(By.cssSelector(".pic_product .infowrap .price span"));
                    String rawPrice = priceEl.getText().trim(); // 예: "2,100"
                    String digits   = rawPrice.replaceAll("[^0-9]", "");
                    if (!digits.isEmpty()) {
                        price = Integer.parseInt(digits);
                    }
                } catch (NoSuchElementException ignore) {
                    try {
                        WebElement legacyPriceEl = li.findElement(By.cssSelector(".product_content .price_list span"));
                        String rawPrice = legacyPriceEl.getText().trim();
                        String digits   = rawPrice.replaceAll("[^0-9]", "");
                        if (!digits.isEmpty()) {
                            price = Integer.parseInt(digits);
                        }
                    } catch (NoSuchElementException ignore2) {}
                }

                // promo_type 결정
                String finalPromoType = promoTypeFallback; // 기본은 현재 탭이 갖는 타입

                // 1+1 / 2+1 판별: tag_list_01 li 의 class
                try {
                    List<WebElement> promoEls = li.findElements(By.cssSelector(".tag_list_01 li"));
                    for (WebElement pEl : promoEls) {
                        String cls = pEl.getAttribute("class"); // ex) "ico_tag_06", "ico_tag_07"
                        if (cls != null) {
                            if (cls.contains("ico_tag_06")) {
                                finalPromoType = "ONE_PLUS_ONE";
                            } else if (cls.contains("ico_tag_07")) {
                                finalPromoType = "TWO_PLUS_ONE";
                            }
                        }
                    }
                } catch (NoSuchElementException ignore) {}

                // 증정행사: .ico_present 가 있으면 GIFT로 덮어쓰기
                try {
                    WebElement presentEl = li.findElement(By.cssSelector(".ico_present"));
                    if (presentEl != null) {
                        finalPromoType = "GIFT";
                    }
                } catch (NoSuchElementException ignore) {}

                // 브랜드
                String sourceChain = "SEV";

                // 상품명 없으면 skip (빈 카드나 placeholder일 수 있음)
                if (productName == null || productName.isBlank()) {
                    continue;
                }

                // 디버그 출력
                System.out.println("---- idx " + idx);
                System.out.println("sourceChain : " + sourceChain);
                System.out.println("productName : " + productName);
                System.out.println("price       : " + price);
                System.out.println("imageUrl    : " + imageUrl);
                System.out.println("promoType   : " + finalPromoType);

                // DB INSERT (중복은 UNIQUE KEY + INSERT IGNORE 로 방지)
                insertProduct(conn, sourceChain, productName, price, imageUrl, finalPromoType);
                insertedCount++;

            } catch (StaleElementReferenceException rowStale) {
                // 이 아이템은 날아갔으면 그냥 넘어간다
                continue;
            } catch (Exception rowEx) {
                // 예상 못한 오류여도 전체 크롤은 계속 돈다
                rowEx.printStackTrace();
            }
        }

        return insertedCount;
    }

    // ====== 현재까지 로드된 상품 개수 (MORE 루프에서 증가 체크용) ======
    private static int getCurrentProductCount(WebDriver driver) {
        try {
            List<WebElement> listNow = driver.findElements(By.cssSelector(".img_list li"));
            if (!listNow.isEmpty()) {
                return listNow.size();
            }
        } catch (Exception ignore) {}

        // fallback: .pic_product 가진 li 세기
        try {
            List<WebElement> listFallback = driver.findElements(By.cssSelector("li .pic_product"));
            return listFallback.size();
        } catch (Exception ignore) {}

        // 최후 fallback
        return driver.findElements(By.cssSelector("li")).size();
    }

    // ====== DB INSERT (INSERT IGNORE 사용) ======
    private static void insertProduct(Connection conn,
                                      String sourceChain,
                                      String productName,
                                      Integer price,
                                      String imageUrl,
                                      String promoTypeEnum) throws SQLException {

        // 이 부분을 쓰려면 사전에 한 번만 아래 쿼리를 DB에 실행해두는 걸 추천:
        //
        // ALTER TABLE craw_product
        // ADD UNIQUE KEY uq_chain_name_promo (
        //     source_chain,
        //     product_name,
        //     promo_type
        // );
        //
        // 이렇게 해두면 같은 편의점/같은 상품명/같은 행사타입이면 중복으로 안 들어간다.

        String sql = "INSERT IGNORE INTO craw_product " +
                "(source_chain, product_name, price, image_url, promo_type) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sourceChain);    // "SEV"
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
