package projecct.pyeonhang.crawling.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/crawl")
public class CrawlingController {
    @GetMapping("/main")
    public ModelAndView main(){
        ModelAndView view = new ModelAndView();
        view.setViewName("views/crawling/crawlproduct");
        return view;
    }
}
