package projecct.pyeonhang.crawling.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import projecct.pyeonhang.crawling.crawler.CUCrawler;
import projecct.pyeonhang.crawling.crawler.GSCrawler;
import projecct.pyeonhang.crawling.crawler.SEVCrwaler;

@Service
@RequiredArgsConstructor
public class CrawlerSchedulerService {

    private final CUCrawler cuCrawler;
    private final GSCrawler gsCrawler;
    private final SEVCrwaler sevCrwaler;


    public void crawlAllConvenienceStoresMonthly() {

        // GS
        try {
            System.out.println("[Crawler] GS25 시작");
            gsCrawler.crawlMonthlyEvents();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // CU
        try {
            System.out.println("[Crawler] CU 시작");
            cuCrawler.crawlMonthlyEvents();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // SEV
        try {
            System.out.println("[Crawler] SEV 시작");
            sevCrwaler.crawlMonthlyEvents();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
