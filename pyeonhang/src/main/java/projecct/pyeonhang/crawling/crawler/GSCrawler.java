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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GSCrawler {
    private static final String WEB_DRIVER_ID   = "webdriver.chrome.driver";
    private static final String WEB_DRIVER_PATH = "C:/chromedriver-win64/chromedriver.exe";

    private static final String DB_URL      = "jdbc:mariadb://pyeonhang-db.cjg402amekn6.ap-southeast-2.rds.amazonaws.com/pyeonhang?useUnicode=true&characterEncoding=utf8";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "goqlsgoqls1";

    // 정규식 패턴
    private static final Pattern NAME_PATTERN  =
            Pattern.compile("<p\\s+class=\"tit\">([^<]+)</p>");
    private static final Pattern PRICE_PATTERN =
            Pattern.compile("<span\\s+class=\"cost\">\\s*([^<]+?)\\s*<span>");
    private static final Pattern PROMO_PATTERN =
            Pattern.compile(
                    "<div\\s+class=\"flag_box[^\"]*?\">.*?<span[^>]*>([^<]+)</span>",
                    Pattern.DOTALL
            );

    // ========= 분류용 키워드 =========
    // LIFE (생활/위생/세제/구강용품 등)
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
            "덴탈","가그린","순면","중형","대형","크리넥스","애니데이","키친타월","탐폰","좋은느낌","대형","화이트","솔루엠","시크릿데이"
    };

    // DRINK(음료) 후보 판정용
    private static final String[] DRINK_KEYWORDS = {
            "음료","드링크","주스","에이드","콜라","사이다",
            "티","차","녹차","보리차","밀크티","홍차","우롱차",
            "커피","라떼","우유","요구르트",
            "막걸리","맥주","소주","와인","이온","스포츠",
            "워터","물","생수","닥터페퍼","코카","에반게리온","스파클링","초록매실","슈가로로","모닝케어","오로나민","아이스티","헛개수",
            "컨디션","할리스","드링킹","얼라이브","홍차","보성","아이스티","뿌요","워터","종근당","남양","핫초코","레몬즙","율무차","담터",
            "아카페라"
    };

    // FOOD(식사/반찬/즉석식품 등)
    private static final String[] FOOD_KEYWORDS = {
            "도시락","김밥","주먹밥","삼각김밥","버거","햄버거","핫도그","샌드위치","토스트",
            "라면","컵라면","우동","파스타","스파게티","짜장","카레","볶음밥","볶음","제육",
            "치킨","탕","국","찌개","죽","만두","호빵","육회","자반","반찬","안주","빵","고등어","갈치","자반","오징어","오다리","먹태",
            "황태","미이랑","곱창","떡볶이","보쌈","소스","김밥김","스지","도가니","곱창김","추어탕","부리또","김치","미트볼","떡갈비","수프",
            "백숙","양념","육수","후랑크","닭가슴살","스테이크","뉴트리","간장","차돌","곡물","밥"
    };

    // SNACK (간식+아이스크림 포함) 강제 키워드
    private static final String[] FORCE_SNACK_KEYWORDS = {
            "프로틴바","에너지바","시리얼바","씨리얼바","곡물바","견과바",
            "스크류바","죠스바","수박바","메로나","월드콘","브라보콘",
            "붕어싸만코","빠삐코","폴라포","하겐다즈","매그넘","넛츠바","오트바","파인트","나뚜루","카라멜바","피스타치오바","오!그래놀라",
            "단백질바","레귤러바"
    };

    // SNACK 일반 키워드
    private static final String[] SNACK_KEYWORDS = {
            "과자","스낵","칩","쿠키","비스킷","크래커","초코","초콜릿","젤리","구미",
            "캔디","사탕","껌","빼빼로","양갱","넛츠","견과","바베큐맛","바베큐",
            "빙수","콘 아이스크림","소프트콘","팝콘","나쵸","나초","크리스피롤","프레첼","고구마","망고","츄파춥스","마칩","짱셔요","껌",
            "요거트","아몬드","하리보","머거본","스틱","생초코","오레오","아이셔","톡핑","믹스넛","웨하스","약과","떡","마카다미아","소이조이","샌드",
            "청정)","군밤","맛밤","포스트","켈로그","카스테라","프링글스","치토스","꼬깔콘","감자칩","빠다","제크","크런키","이클립스",
            "몰티져스","마즈)","밀카","웨이퍼","위스트","트롤리","린도","멘토스","네슬레","와사비","포테","쫄병스낵","짬뽕칩","마요칩","콰트로",
            "천하장사50","라즈","피니)","미성","멜로팝","샤오커","코피코","크라운","서주","해태","마라맛","곤약","엠지","밀카","뿌셔뿌셔","선우","배스킨"
    };

    // ✅ DRINK 후보라도 FOOD로 강제 재분류할 키워드 (요청: 녹차 + 김 → FOOD)
    //    - 김/도시락김/파래김/재래김/김자반/자반 등 포함 시 DRINK 해제하고 FOOD로
    private static final String[] FOOD_OVERRIDE_WHEN_DRINK = {
            "김","김밥김","도시락김","재래김","파래김","김자반","자반","녹차김"
    };

    // 용량 단위(ml/L) 판정 정규식 (공백/대소문자/소수점 허용)
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
            driver.get("http://gs25.gsretail.com/gscvs/ko/products/event-goods");
            Thread.sleep(1500);

            // TOTAL 탭 클릭
            clickTotalTab(driver);

            // 첫 페이지
            crawlOnePageAndInsert(conn, driver, 1);

            // 이후 페이지들
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
    // TOTAL 탭(전체) 클릭
    // ======================
    private static void clickTotalTab(WebDriver driver) throws InterruptedException {
        WebElement totalTab = driver.findElement(By.id("TOTAL"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", totalTab);
        Thread.sleep(1000);
    }

    // ======================
    // 페이지네이션 이동
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

        // targetPage가 현재 블록에 없으면 next(>) 클릭 후 재시도
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
            // 혹시 구조 바뀌었을 때 fallback
            itemNodes = driver.findElements(By.cssSelector(".prod_box"));
        }

        System.out.println("[" + pageNo + "페이지] 상품 노드 수: " + itemNodes.size());

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

            // 3) 행사타입 판정
            String promoTypeEnum = "NONE";
            try {
                WebElement flagBox = boxEl.findElement(By.cssSelector("div.flag_box"));
                String flagClass = flagBox.getAttribute("class"); // ex) "flag_box GIFT", "flag_box ONE_TO_ONE"
                promoTypeEnum = mapPromoTypeFromFlagClass(flagClass);
            } catch (NoSuchElementException ignore) {
                // flag_box 없으면 아래서 텍스트 기반 처리
            }

            if ("NONE".equals(promoTypeEnum)) {
                String promoText = matchOne(PROMO_PATTERN, html);

                // "증정"/"덤" 단어 감지 백업
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

            // 5) product_type 자동 분류
            String productTypeEnum = classifyProductType(productName);

            // NONE -> FOOD 로 강제 치환 (최종 저장 정책)
            if ("NONE".equals(productTypeEnum)) {
                productTypeEnum = "FOOD";
            }

            // 디버그 출력
            System.out.println("----");
            System.out.println("page        : " + pageNo);
            System.out.println("productName : " + productName);
            System.out.println("price       : " + price);
            System.out.println("imageUrl    : " + imageUrl);
            System.out.println("promoType   : " + promoTypeEnum);
            System.out.println("prodType    : " + productTypeEnum);

            // 상품명 없으면 placeholder일 수 있으니 skip
            if (productName == null || productName.isBlank()) {
                continue;
            }

            // DB insert
            insertProduct(conn, "GS25", productName, price, imageUrl, promoTypeEnum, productTypeEnum);
            insertCount++;
        }

        System.out.println("[" + pageNo + "페이지] insertCount = " + insertCount);
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
    // flag_box class → promo_type 매핑
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
    // promoText → promo_type 매핑 (백업)
    // ======================
    private static String mapPromoTypeFromText(String promoText) {
        if (promoText == null) return "NONE";

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
    // 문자열에 keywords 배열 중 하나라도 포함돼 있는지 (대소문자 무시)
    // ======================
    private static boolean containsAny(String text, String[] keywords) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        for (String kw : keywords) {
            if (lower.contains(kw.toLowerCase())) return true;
        }
        return false;
    }

    // ======================
    // product_type 자동 분류 (우선순위 로직 강화)
    //
    // 1) DRINK 후보:
    //    - 용량 단위(ml/L) 있거나 DRINK 키워드 포함
    //    - 단, LIFE 키워드가 있으면 LIFE로 강제
    //    - 단, FOOD_OVERRIDE_WHEN_DRINK(김/자반/녹차김 등) 있으면 FOOD로 강제
    //    - 단, SNACK 키워드(바/콘/아이스 등) 있으면 SNACK 우선
    //
    // 2) DRINK 후보가 아니면:
    //    - FOOD 키워드 → FOOD
    //    - SNACK 강제/일반 키워드 or 이름패턴(끝이 '바','콘') → SNACK
    //    - LIFE 키워드 → LIFE
    //    - 나머지 → NONE (저장 직전에 FOOD로 치환)
    //
    // 🔸 요청 케이스: "녹차" 포함 + "김" 포함 → FOOD
    //     => DRINK 후보라도 FOOD_OVERRIDE_WHEN_DRINK가 최우선으로 덮어씀
    // ======================
    private static String classifyProductType(String productName) {
        if (productName == null) return "NONE";

        final String kor   = productName;
        final String lower = productName.toLowerCase();

        // 1) DRINK 후보 판정
        boolean hasVolumeUnit = VOLUME_PATTERN.matcher(lower).matches();
        boolean hasDrinkWord  = containsAny(kor, DRINK_KEYWORDS);
        boolean drinkCandidate = hasVolumeUnit || hasDrinkWord;

        if (drinkCandidate) {
            // LIFE가 끼어 있으면 DRINK 해제하고 LIFE
            if (containsAny(kor, LIFE_KEYWORDS)) {
                return "LIFE";
            }
            // ✅ 김/자반 등 FOOD 재료가 끼면 DRINK 해제하고 FOOD (녹차+김 대응)
            if (containsAny(kor, FOOD_OVERRIDE_WHEN_DRINK)) {
                return "FOOD";
            }
            // SNACK 요소가 강하면 SNACK
            if (containsAny(kor, FORCE_SNACK_KEYWORDS) ||
                    containsAny(kor, SNACK_KEYWORDS) ||
                    kor.endsWith("바") || kor.endsWith("콘")) {
                return "SNACK";
            }
            // 그대로 DRINK
            return "DRINK";
        }

        // 3) SNACK (강제 키워드 먼저)
        if (containsAny(kor, FORCE_SNACK_KEYWORDS) ||
                containsAny(kor, SNACK_KEYWORDS) ||
                kor.endsWith("바") || kor.endsWith("콘")) {
            return "SNACK";
        }

        // 4) LIFE
        if (containsAny(kor, LIFE_KEYWORDS)) {
            return "LIFE";
        }

        // 5) 디폴트
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
                                      String promoTypeEnum,
                                      String productTypeEnum) throws SQLException {

        String sql = "INSERT INTO craw_product " +
                "(source_chain, product_name, price, image_url, promo_type, product_type) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sourceChain); // 예: "GS25"
            ps.setString(2, productName);

            if (price != null) {
                ps.setInt(3, price);
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }

            ps.setString(4, imageUrl);           // image_url
            ps.setString(5, promoTypeEnum);      // promo_type
            ps.setString(6, productTypeEnum);    // product_type (NONE 치환된 상태)

            ps.executeUpdate();
        }
    }
}
