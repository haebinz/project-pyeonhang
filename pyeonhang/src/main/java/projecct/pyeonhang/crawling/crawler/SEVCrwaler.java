package projecct.pyeonhang.crawling.crawler;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;


@Component
public class SEVCrwaler {
    // ===== ChromeDriver / DB =====
    private static final String WEB_DRIVER_ID   = "webdriver.chrome.driver";
    private static final String WEB_DRIVER_PATH = "C:/chromedriver-win64/chromedriver.exe";

    // 네가 쓰던 cp_db 그대로 둠 (원하면 rc_db로 변경 가능)
    private static final String DB_URL      = "jdbc:mariadb://pyeonhang-db.cjg402amekn6.ap-southeast-2.rds.amazonaws.com/pyeonhang?useUnicode=true&characterEncoding=utf8";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "goqlsgoqls1";

    private static final String BASE_URL    = "https://www.7-eleven.co.kr";

    // ===== 분류 키워드 / 패턴 (CU/GS와 동일 정책) =====
    private static final String[] LIFE_KEYWORDS = {
            "페브리즈","탈취","탈취제","섬유유연제","유연제","섬유유연","세제","세정","세척",
            "디퓨저","방향제","제습","제습제","섬유탈취","탈취스프레이",
            "물티슈","티슈","휴지","주방티슈","주방행주","행주",
            "마스크","생리대","파스","밴드",
            "면도","면도기","면도날",
            "칫솔","치약","가글","구강",
            "비누","샴푸","린스","바디워시","클렌징","클렌징폼",
            "피죤","유한","다우니","아우라","제균","항균","살균",
            "주방","스타킹","위생장갑","고무장갑","수세미","샤프란","벡셀","냄새","알카라인","AA","포밍","클렌","뉴트로지나","리스테린","존슨즈",
            "양말","남성","여성","에너자이저","3겹","성분","타월","잘풀리는","스너글","바세린","니베아","네오젠","스킨","깨끗한나라","순수한면","건강한",
            "덴탈","가그린","순면","중형","대형","크리넥스","애니데이","키친타월","탐폰","좋은느낌","대형","화이트","솔루엠","시크릿데이",
            "스프레이","왁스","바디워시"
    };

    private static final String[] DRINK_KEYWORDS = {
            "음료","드링크","주스","에이드","콜라","사이다",
            "티","차","녹차","보리차","밀크티","홍차","우롱차",
            "커피","라떼","우유","요구르트",
            "막걸리","맥주","소주","와인","이온","스포츠",
            "워터","물","생수","닥터페퍼","코카","에반게리온","스파클링","초록매실","슈가로로","모닝케어","오로나민","아이스티","헛개수",
            "컨디션","할리스","드링킹","얼라이브","홍차","보성","아이스티","뿌요","워터","종근당","남양","핫초코","레몬즙","율무차","담터",
            "아카페라","웅진","핫식스"
    };

    private static final String[] FOOD_KEYWORDS = {
            "도시락","김밥","주먹밥","삼각김밥","버거","햄버거","핫도그","샌드위치","토스트",
            "라면","컵라면","우동","파스타","스파게티","짜장","카레","볶음밥","볶음","제육",
            "치킨","탕","국","찌개","죽","만두","호빵","육회","자반","반찬","안주","빵","고등어","갈치","자반","오징어","오다리","먹태",
            "황태","미이랑","곱창","떡볶이","보쌈","소스","김밥김","스지","도가니","곱창김","추어탕","부리또","김치","미트볼","떡갈비","수프",
            "백숙","양념","육수","후랑크","닭가슴살","스테이크","뉴트리","간장","차돌","곡물","밥"
    };

    private static final String[] FORCE_SNACK_KEYWORDS = {
            "프로틴바","에너지바","시리얼바","씨리얼바","곡물바","견과바",
            "스크류바","죠스바","수박바","메로나","월드콘","브라보콘",
            "붕어싸만코","빠삐코","폴라포","하겐다즈","매그넘","넛츠바","오트바","파인트","나뚜루","카라멜바","피스타치오바","오!그래놀라",
            "단백질바","레귤러바"
    };

    private static final String[] SNACK_KEYWORDS = {
            "과자","스낵","칩","쿠키","비스킷","크래커","초코","초콜릿","젤리","구미",
            "캔디","사탕","껌","빼빼로","양갱","넛츠","견과","바베큐맛","바베큐",
            "빙수","콘 아이스크림","소프트콘","팝콘","나쵸","나초","크리스피롤","프레첼","고구마","망고","츄파춥스","마칩","짱셔요","껌",
            "요거트","아몬드","하리보","머거본","스틱","생초코","오레오","아이셔","톡핑","믹스넛","웨하스","약과","떡","마카다미아","소이조이","샌드",
            "청정)","군밤","맛밤","포스트","켈로그","카스테라","프링글스","치토스","꼬깔콘","감자칩","빠다","제크","크런키","이클립스",
            "몰티져스","마즈)","밀카","웨이퍼","위스트","트롤리","린도","멘토스","네슬레","와사비","포테","쫄병스낵","짬뽕칩","마요칩","콰트로",
            "천하장사50","라즈","피니)","미성","멜로팝","샤오커","코피코","크라운","서주","해태","마라맛","곤약","엠지","밀카","뿌셔뿌셔","선우","배스킨",
            "리뉴"
    };

    // DRINK 후보라도 FOOD로 강제(녹차+김 등)
    private static final String[] FOOD_OVERRIDE_WHEN_DRINK = {
            "김","김밥김","도시락김","재래김","파래김","김자반","자반","녹차김"
    };

    // 용량 단위(ml/L)
    private static final Pattern VOLUME_PATTERN =
            Pattern.compile(".*\\b\\d+(?:\\.\\d+)?\\s?(?:ml|mL|ML|l|L)\\b.*");

    public void crawlMonthlyEvents() {

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
            driver.get("https://www.7-eleven.co.kr/product/presentList.asp");
            Thread.sleep(1500);

            // 탭별 순회 (기본값 → 아이콘/텍스트로 보정함)
            scrapeOneTabAndInsertAll(conn, driver, By.cssSelector("a[href*=\"fncTab('1')\"]"), "ONE_PLUS_ONE");
            scrapeOneTabAndInsertAll(conn, driver, By.cssSelector("a[href*=\"fncTab('2')\"]"), "TWO_PLUS_ONE");
            scrapeOneTabAndInsertAll(conn, driver, By.cssSelector("a[href*=\"fncTab('3')\"]"), "GIFT");
            scrapeOneTabAndInsertAll(conn, driver, By.cssSelector("a[href*=\"fncTab('4')\"]"), "NONE");

            System.out.println("세븐일레븐 전체 탭 크롤 완료.");

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    // ===== 탭 처리: 클릭 -> MORE -> 파싱/저장 =====
    private static void scrapeOneTabAndInsertAll(Connection conn,
                                                 WebDriver driver,
                                                 By tabSelector,
                                                 String promoTypeForThisTab) throws Exception {
        System.out.println("=== 탭 시작: " + promoTypeForThisTab + " ===");
        clickTab(driver, tabSelector);
        expandAllWithMore(driver);
        int insertedCount = extractAndInsertProductsFromCurrentTab(conn, driver, promoTypeForThisTab);
        System.out.println("=== 탭 종료: " + promoTypeForThisTab + " / inserted rows: " + insertedCount + " ===");
    }

    private static void clickTab(WebDriver driver, By tabSelector) throws InterruptedException {
        try {
            WebElement tabBtn = driver.findElement(tabSelector);
            try {
                tabBtn.click();
            } catch (ElementClickInterceptedException e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", tabBtn);
            } catch (StaleElementReferenceException e) {
                WebElement tabBtn2 = driver.findElement(tabSelector);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", tabBtn2);
            }
            Thread.sleep(800);
        } catch (NoSuchElementException e) {
            System.out.println("[WARN] 탭 못 찾음: " + tabSelector);
        }
    }

    private static void expandAllWithMore(WebDriver driver) throws InterruptedException {
        int safetyCount = 0;
        while (true) {
            int beforeCount = getCurrentProductCount(driver);

            List<WebElement> candidates = driver.findElements(
                    By.cssSelector("a[href^='javascript:fncMore'], a[href^=\"javascript: fncMore\"]")
            );
            if (candidates.isEmpty()) {
                System.out.println("MORE 버튼 없음 -> 끝");
                break;
            }

            WebElement moreBtn = candidates.get(0);
            boolean clicked = false;
            try {
                moreBtn.click();
                clicked = true;
            } catch (ElementClickInterceptedException e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", moreBtn);
                clicked = true;
            } catch (StaleElementReferenceException e) {
                continue;
            }
            if (!clicked) {
                System.out.println("MORE 클릭 실패 -> 종료");
                break;
            }

            Thread.sleep(1500);

            int afterCount = getCurrentProductCount(driver);
            System.out.println("MORE 후 상품 수: " + beforeCount + " -> " + afterCount);

            if (afterCount <= beforeCount) {
                List<WebElement> again = driver.findElements(
                        By.cssSelector("a[href^='javascript:fncMore'], a[href^=\"javascript: fncMore\"]")
                );
                if (again.isEmpty()) {
                    System.out.println("MORE 사라짐 -> 종료");
                    break;
                } else {
                    System.out.println("증가 없음 + 버튼 남음 -> 종료");
                    break;
                }
            }

            if (++safetyCount > 500) {
                System.out.println("MORE 무한 루프 의심 -> 강제 종료");
                break;
            }
        }
    }

    // ===== 파싱 & 저장 (분류 포함) =====
    private static int extractAndInsertProductsFromCurrentTab(Connection conn,
                                                              WebDriver driver,
                                                              String promoTypeFallback) throws SQLException {

        List<WebElement> initialList;
        try {
            initialList = driver.findElements(By.cssSelector(".img_list li"));
            if (initialList.isEmpty()) initialList = driver.findElements(By.cssSelector("li"));
        } catch (NoSuchElementException e) {
            initialList = driver.findElements(By.cssSelector("li"));
        }

        int liCount = initialList.size();
        System.out.println("현재 탭(" + promoTypeFallback + ") li 개수: " + liCount);

        int insertedCount = 0;

        for (int idx = 0; idx < liCount; idx++) {
            try {
                WebElement li;
                try {
                    List<WebElement> currentList = driver.findElements(By.cssSelector(".img_list li"));
                    if (currentList.isEmpty()) currentList = driver.findElements(By.cssSelector("li"));
                    if (idx >= currentList.size()) break;
                    li = currentList.get(idx);
                } catch (StaleElementReferenceException se) {
                    continue;
                }

                String imageUrl = null;
                try {
                    WebElement imgEl = li.findElement(By.cssSelector(".pic_product img"));
                    imageUrl = imgEl.getAttribute("src");
                    if (imageUrl != null && imageUrl.startsWith("/")) {
                        imageUrl = BASE_URL + imageUrl;
                    }
                } catch (NoSuchElementException ignore) {}

                String productName = null;
                try {
                    WebElement nameEl = li.findElement(By.cssSelector(".pic_product .infowrap .name"));
                    productName = nameEl.getText().trim();
                } catch (NoSuchElementException ignore) {
                    try {
                        WebElement imgEl = li.findElement(By.cssSelector(".pic_product img"));
                        String altName = imgEl.getAttribute("alt");
                        if (altName != null && !altName.isBlank()) productName = altName.trim();
                    } catch (NoSuchElementException ignore2) {}
                    if (productName == null || productName.isBlank()) {
                        try {
                            WebElement legacyNameEl = li.findElement(By.cssSelector(".product_content .tit_product"));
                            productName = legacyNameEl.getText().trim();
                        } catch (NoSuchElementException ignore3) {}
                    }
                }

                Integer price = null;
                try {
                    WebElement priceEl = li.findElement(By.cssSelector(".pic_product .infowrap .price span"));
                    String rawPrice = priceEl.getText().trim();
                    String digits   = rawPrice.replaceAll("[^0-9]", "");
                    if (!digits.isEmpty()) price = Integer.parseInt(digits);
                } catch (NoSuchElementException ignore) {
                    try {
                        WebElement legacyPriceEl = li.findElement(By.cssSelector(".product_content .price_list span"));
                        String rawPrice = legacyPriceEl.getText().trim();
                        String digits   = rawPrice.replaceAll("[^0-9]", "");
                        if (!digits.isEmpty()) price = Integer.parseInt(digits);
                    } catch (NoSuchElementException ignore2) {}
                }

                String finalPromoType = promoTypeFallback;
                try {
                    List<WebElement> promoEls = li.findElements(By.cssSelector(".tag_list_01 li"));
                    for (WebElement pEl : promoEls) {
                        String cls = pEl.getAttribute("class");
                        if (cls != null) {
                            if (cls.contains("ico_tag_06")) finalPromoType = "ONE_PLUS_ONE";
                            else if (cls.contains("ico_tag_07")) finalPromoType = "TWO_PLUS_ONE";
                        }
                    }
                } catch (NoSuchElementException ignore) {}
                try {
                    WebElement presentEl = li.findElement(By.cssSelector(".ico_present"));
                    if (presentEl != null) finalPromoType = "GIFT";
                } catch (NoSuchElementException ignore) {}
                String allText = li.getText().replaceAll("\\s+","").trim();
                if ("NONE".equals(finalPromoType) &&
                        (allText.contains("증정") || allText.contains("덤") || allText.contains("사은품"))) {
                    finalPromoType = "GIFT";
                }

                String sourceChain = "SEV";

                // === 분류 로직 ===
                String productTypeEnum = classifyProductType(productName);
                if ("NONE".equals(productTypeEnum)) productTypeEnum = "FOOD";

                if (productName == null || productName.isBlank()) continue;

                System.out.println("---- idx " + idx);
                System.out.println("sourceChain : " + sourceChain);
                System.out.println("productName : " + productName);
                System.out.println("price       : " + price);
                System.out.println("imageUrl    : " + imageUrl);
                System.out.println("promoType   : " + finalPromoType);
                System.out.println("prodType    : " + productTypeEnum);

                insertProduct(conn, sourceChain, productName, price, imageUrl, finalPromoType, productTypeEnum);
                insertedCount++;

            } catch (StaleElementReferenceException rowStale) {
                continue;
            } catch (Exception rowEx) {
                rowEx.printStackTrace();
            }
        }
        return insertedCount;
    }

    // ===== 유틸 =====
    private static boolean containsAny(String text, String[] keywords) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        for (String kw : keywords) {
            if (lower.contains(kw.toLowerCase())) return true;
        }
        return false;
    }

    private static String classifyProductType(String productName) {
        if (productName == null) return "NONE";
        String kor = productName;
        String lower = productName.toLowerCase();

        boolean hasVolumeUnit = VOLUME_PATTERN.matcher(lower).matches();
        boolean hasDrinkWord  = containsAny(kor, DRINK_KEYWORDS);
        boolean drinkCandidate = hasVolumeUnit || hasDrinkWord;

        if (drinkCandidate) {
            if (containsAny(kor, LIFE_KEYWORDS)) return "LIFE";
            if (containsAny(kor, FOOD_OVERRIDE_WHEN_DRINK)) return "FOOD";
            if (containsAny(kor, FORCE_SNACK_KEYWORDS) || containsAny(kor, SNACK_KEYWORDS)
                    || kor.endsWith("바") || kor.endsWith("콘")) return "SNACK";
            return "DRINK";
        }
        if (containsAny(kor, FOOD_KEYWORDS)) return "FOOD";
        if (containsAny(kor, FORCE_SNACK_KEYWORDS) || containsAny(kor, SNACK_KEYWORDS)
                || kor.endsWith("바") || kor.endsWith("콘")) return "SNACK";
        if (containsAny(kor, LIFE_KEYWORDS)) return "LIFE";
        return "NONE";
    }

    private static int getCurrentProductCount(WebDriver driver) {
        try {
            List<WebElement> listNow = driver.findElements(By.cssSelector(".img_list li"));
            if (!listNow.isEmpty()) return listNow.size();
        } catch (Exception ignore) {}
        try {
            List<WebElement> listFallback = driver.findElements(By.cssSelector("li .pic_product"));
            return listFallback.size();
        } catch (Exception ignore) {}
        return driver.findElements(By.cssSelector("li")).size();
    }

    // ===== DB INSERT (INSERT IGNORE + product_type) =====
    private static void insertProduct(Connection conn,
                                      String sourceChain,
                                      String productName,
                                      Integer price,
                                      String imageUrl,
                                      String promoTypeEnum,
                                      String productTypeEnum) throws SQLException {

        // 권장: 사전 1회 실행
        // ALTER TABLE craw_product
        //   ADD UNIQUE KEY uq_chain_name_promo (source_chain, product_name, promo_type);

        String sql = "INSERT IGNORE INTO craw_product " +
                "(source_chain, product_name, price, image_url, promo_type, product_type) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sourceChain); // "SEV"
            ps.setString(2, productName);
            if (price != null) ps.setInt(3, price);
            else ps.setNull(3, java.sql.Types.INTEGER);
            ps.setString(4, imageUrl);
            ps.setString(5, promoTypeEnum);
            ps.setString(6, productTypeEnum);
            ps.executeUpdate();
        }
    }
}
