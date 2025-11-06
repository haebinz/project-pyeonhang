package projecct.pyeonhang.crawling.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import projecct.pyeonhang.users.repository.UsersRepository;

@Controller
@RequestMapping("/crawl")
public class CrawlingController {
    private final UsersRepository usersRepository;

    public CrawlingController(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @GetMapping("/main")
    public ModelAndView main(){
        ModelAndView view = new ModelAndView();
        view.setViewName("views/crawling/crawlproduct");
        return view;
    }

    @GetMapping("/index")
    public ModelAndView main2(){
        ModelAndView view = new ModelAndView();
        view.setViewName("views/crawling/index");
        return view;
    }



}
