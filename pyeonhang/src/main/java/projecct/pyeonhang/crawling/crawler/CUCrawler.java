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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class CUCrawler {


    private static final String WEB_DRIVER_ID   = "webdriver.chrome.driver";
    private static final String WEB_DRIVER_PATH = "C:/chromedriver-win64/chromedriver.exe";

    private static final String DB_URL      = "jdbc:mariadb://pyeonhang-db.cjg402amekn6.ap-southeast-2.rds.amazonaws.com/pyeonhang?useUnicode=true&characterEncoding=utf8";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "goqlsgoqls1";

    // ========= 분류용 키워드 / 패턴 =========
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

    // DRINK 후보라도 FOOD로 강제 재분류할 키워드(녹차+김 케이스 포함)
    private static final String[] FOOD_OVERRIDE_WHEN_DRINK = {
            "김","김밥김","도시락김","재래김","파래김","김자반","자반","녹차김"
    };

    // 용량 단위(ml/L) 패턴
    private static final Pattern VOLUME_PATTERN =
            Pattern.compile(".*\\b\\d+(?:\\.\\d+)?\\s?(?:ml|mL|ML|l|L)\\b.*");

    public void crawlMonthlyEvents() {

        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1400,900");
        options.addArguments("--lang=ko-KR");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        // options.addArguments("--headless=new"); // 서버/배치 시 활성화 가능

        WebDriver driver = new ChromeDriver(options);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            // 1) CU 행사 페이지 접속
            String url = "https://cu.bgfretail.com/event/plus.do?category=event&depth2=1&sf=N";
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.get(url);
            Thread.sleep(1500);

            // 2) "더보기" 반복 클릭 → 전체 상품 DOM에 로드
            loadAllProductsByMoreButton(driver);

            // 3) 모든 상품을 스크랩해서 DB 저장 (분류 로직 포함)
            int inserted = scrapeAllProductsAndInsert(conn, driver);

            System.out.println("최종 insert된 상품 수: " + inserted);

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    // "더보기" 버튼 무한 클릭
    private static void loadAllProductsByMoreButton(WebDriver driver) throws InterruptedException {
        int safeGuard = 0;
        while (true) {
            try {
                WebElement moreBtn = driver.findElement(By.cssSelector(".prodListBtn-w a[href^='javascript:nextPage']"));
                if (!moreBtn.isDisplayed() || !moreBtn.isEnabled()) {
                    System.out.println("더보기 버튼 더 이상 표시 안됨 -> 종료");
                    break;
                }
                try {
                    moreBtn.click();
                } catch (ElementClickInterceptedException e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", moreBtn);
                }
                Thread.sleep(1000);
                if (++safeGuard > 200) {
                    System.out.println("안전장치 트립: 더보기 200회 초과 -> 종료");
                    break;
                }
            } catch (NoSuchElementException noMore) {
                System.out.println("더보기 버튼 못 찾음 -> 끝");
                break;
            }
        }
    }

    // 페이지 내 모든 상품을 순회 & 저장 (분류 포함)
    private static int scrapeAllProductsAndInsert(Connection conn, WebDriver driver) throws SQLException {

        List<WebElement> productLis = driver.findElements(By.cssSelector("li.prod_list"));
        System.out.println("총 상품 li 개수: " + productLis.size());

        int insertedCount = 0;

        for (WebElement li : productLis) {
            try {
                // 이미지
                String imageUrl = null;
                try {
                    WebElement imgEl = li.findElement(By.cssSelector(".prod_img img"));
                    imageUrl = imgEl.getAttribute("src");
                    if (imageUrl != null && imageUrl.startsWith("//")) {
                        imageUrl = "https:" + imageUrl;
                    }
                } catch (NoSuchElementException ignore) {}

                // 상품명
                String productName = null;
                try {
                    WebElement nameEl = li.findElement(By.cssSelector(".prod_text .name p"));
                    productName = nameEl.getText().trim();
                } catch (NoSuchElementException ignore) {}

                // 가격
                Integer price = null;
                try {
                    WebElement priceStrong = li.findElement(By.cssSelector(".prod_text .price strong"));
                    String priceText = priceStrong.getText().trim();
                    String digitsOnly = priceText.replaceAll("[^0-9]", "");
                    if (!digitsOnly.isEmpty()) {
                        price = Integer.parseInt(digitsOnly);
                    }
                } catch (NoSuchElementException ignore) {}

                // 행사 타입 (plus1/plus2 + 텍스트 백업)
                String promoTypeEnum = mapPromoTypeFromCuNode(li);

                // 체인
                String sourceChain = "CU";

                // product_type 자동 분류 (GS와 동일 정책)
                String productTypeEnum = classifyProductType(productName);
                if ("NONE".equals(productTypeEnum)) {
                    // 최종 저장 정책: NONE → FOOD
                    productTypeEnum = "FOOD";
                }

                // 디버그 출력
                System.out.println("----");
                System.out.println("sourceChain : " + sourceChain);
                System.out.println("productName : " + productName);
                System.out.println("price       : " + price);
                System.out.println("imageUrl    : " + imageUrl);
                System.out.println("promoType   : " + promoTypeEnum);
                System.out.println("prodType    : " + productTypeEnum);

                // 빈 항목 skip
                if (productName == null || productName.isBlank()) {
                    continue;
                }

                // DB 저장 (product_type 포함)
                insertProduct(conn, sourceChain, productName, price, imageUrl, promoTypeEnum, productTypeEnum);
                insertedCount++;

            } catch (Exception rowEx) {
                rowEx.printStackTrace();
            }
        }
        return insertedCount;
    }

    // CU 노드에서 행사 타입 매핑
    private static String mapPromoTypeFromCuNode(WebElement li) {
        String promoType = "NONE";
        try {
            List<WebElement> badgeSpans = li.findElements(By.cssSelector(".badge span"));
            List<String> clsList = new ArrayList<>();
            for (WebElement span : badgeSpans) {
                String cls = span.getAttribute("class"); // plus1 / plus2 / 기타
                if (cls != null) clsList.add(cls);
            }
            if (clsList.stream().anyMatch(c -> c.contains("plus2"))) {
                promoType = "TWO_PLUS_ONE";
            } else if (clsList.stream().anyMatch(c -> c.contains("plus1"))) {
                promoType = "ONE_PLUS_ONE";
            }

            // 텍스트 백업: 증정/덤/사은품 → GIFT
            String allText = li.getText().replaceAll("\\s+","").trim();
            if ("NONE".equals(promoType)) {
                if (allText.contains("증정") || allText.contains("덤") || allText.contains("사은품")) {
                    promoType = "GIFT";
                }
            }
        } catch (Exception ignore) {}
        return promoType;
    }

    // 문자열 키워드 포함 여부(대소문자 무시)
    private static boolean containsAny(String text, String[] keywords) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        for (String kw : keywords) {
            if (lower.contains(kw.toLowerCase())) return true;
        }
        return false;
    }

    // 상품 타입 분류 로직 (GS와 동일 우선순위)
    private static String classifyProductType(String productName) {
        if (productName == null) return "NONE";

        String kor = productName;
        String lower = productName.toLowerCase();

        // 1) DRINK 후보: 용량단위(ml/L) 또는 DRINK 키워드
        boolean hasVolumeUnit = VOLUME_PATTERN.matcher(lower).matches();
        boolean hasDrinkWord  = containsAny(kor, DRINK_KEYWORDS);
        boolean drinkCandidate = hasVolumeUnit || hasDrinkWord;

        if (drinkCandidate) {
            // LIFE 키워드가 끼면 LIFE
            if (containsAny(kor, LIFE_KEYWORDS)) {
                return "LIFE";
            }
            // 김/자반/김밥김 등 있으면 FOOD (녹차+김 대응)
            if (containsAny(kor, FOOD_OVERRIDE_WHEN_DRINK)) {
                return "FOOD";
            }
            // SNACK 성격이 강하면 SNACK
            if (containsAny(kor, FORCE_SNACK_KEYWORDS) ||
                    containsAny(kor, SNACK_KEYWORDS) ||
                    kor.endsWith("바") || kor.endsWith("콘")) {
                return "SNACK";
            }
            // 그대로 DRINK
            return "DRINK";
        }

        // 2) FOOD 키워드
        if (containsAny(kor, FOOD_KEYWORDS)) {
            return "FOOD";
        }

        // 3) SNACK (강제 → 일반 → 접미사 바/콘)
        if (containsAny(kor, FORCE_SNACK_KEYWORDS) ||
                containsAny(kor, SNACK_KEYWORDS) ||
                kor.endsWith("바") || kor.endsWith("콘")) {
            return "SNACK";
        }

        // 4) LIFE
        if (containsAny(kor, LIFE_KEYWORDS)) {
            return "LIFE";
        }

        // 5) 미분류
        return "NONE";
    }

    // DB insert (product_type 포함)
    private static void insertProduct(Connection conn,
                                      String sourceChain,
                                      String productName,
                                      Integer price,
                                      String imageUrl,
                                      String promoTypeEnum,
                                      String productTypeEnum) throws SQLException {

        // 중복 방지 필요 시:
        // ALTER TABLE craw_product
        //   ADD UNIQUE KEY uq_chain_name_promo (source_chain, product_name, promo_type);

        String sql = "INSERT INTO craw_product " +
                "(source_chain, product_name, price, image_url, promo_type, product_type) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sourceChain);       // "CU"
            ps.setString(2, productName);       // 상품명

            if (price != null) {
                ps.setInt(3, price);
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }

            ps.setString(4, imageUrl);          // 이미지 URL
            ps.setString(5, promoTypeEnum);     // ONE_PLUS_ONE / TWO_PLUS_ONE / GIFT / NONE
            ps.setString(6, productTypeEnum);   // DRINK / FOOD / SNACK / LIFE

            ps.executeUpdate();
        }
    }
}
