package projecct.pyeonhang.crawling.scheduler;


import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import projecct.pyeonhang.crawling.service.CrawlerSchedulerService;

@Component
@RequiredArgsConstructor
public class CrawlerScheduler {

    private final CrawlerSchedulerService crawlerSchedulerService;


    // 매월 1일 새벽 3시 (서울 기준)
    @Scheduled(cron = "0 0 3 1 * *", zone = "Asia/Seoul")
    public void monthlyCrawlingJob() {
        System.out.println("매월 1일 편의점 3사 데이터 크롤링 시작");
        crawlerSchedulerService.crawlAllConvenienceStoresMonthly();
        System.out.println("크롤링 종료");
    }

}
