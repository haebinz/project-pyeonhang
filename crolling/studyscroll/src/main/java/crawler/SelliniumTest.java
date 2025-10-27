package crawler;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;

public class SelliniumTest {

    private WebDriver driver;
    private WebElement element;

    //1.드라이버 설치경로
    public static String WEB_DRIVER_ID = "webdriver.chrome.driver";
    public static String WEB_DRIVER_PATH = "C:/chromedriver-win64/chromedriver.exe";

    // 2) 크롬드라이버 경로 지정 (수동 설치 방식)
    public static void main(String[] args) {
        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);
        WebDriver driver = new ChromeDriver();

        try{
            String url4 = "https://cu.bgfretail.com/event/plus.do?category=event&depth2=1&sf=N";
            driver.get(url4);

            String title=driver.getTitle();

            List<WebElement> items = driver.findElements(By.className("prod_list"));

            for (WebElement el : items) {
                System.out.println(el.getText());
            }
            System.out.println(items.size());


            System.out.println("사이트 타이틀 가져오기\t"+title);


        }catch(Exception e){
            e.printStackTrace();
        }

    }
}

