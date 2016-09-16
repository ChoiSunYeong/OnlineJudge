package kr.jadekim.oj.mainserver.controller.WebController;

import kr.jadekim.oj.mainserver.entity.CurrentUser;
import kr.jadekim.oj.mainserver.entity.ProblemSet;
import kr.jadekim.oj.mainserver.entity.User;
import kr.jadekim.oj.mainserver.service.ProblemSetService;
import kr.jadekim.oj.mainserver.service.UserService;
import kr.jadekim.oj.mainserver.util.Pagenation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by ohyongtaek on 2016. 9. 14..
 */
@Controller
@RequestMapping("/contest")
public class WebContestController {

    @Autowired
    ProblemSetService problemSetService;

    @Autowired
    UserService userService;

    @PreAuthorize("hasAuthority('USER')")
    @RequestMapping("create")
    public ModelAndView createContest(ModelAndView modelAndView) {
        modelAndView.setViewName("contestCreate");
        return modelAndView;
    }

    @PreAuthorize("hasAuthority('USER')")
    @RequestMapping(value = "create", method = RequestMethod.POST)
    public ModelAndView createContestPost(ModelAndView modelAndView, Authentication authentication, HttpServletRequest request) {

        User user = ((CurrentUser) authentication.getPrincipal()).getUser();
        User admin = user;
        String title = request.getParameter("contest_title");

        return modelAndView;
    }

    @PreAuthorize("hasAuthority('USER')")
    @RequestMapping("create/insert-admin")
    public ModelAndView insertAdmins(ModelAndView modelAndView, Authentication authentication, @PageableDefault(sort = { "id" }, size = 10) Pageable pageable) {

        try {
            List<User> users = userService.findAll(pageable).get();
            List<Map> addAdmins = new ArrayList<>();
            ArrayList<Integer> pages;
            for (User user : users) {
                Map<String, Object> map = new HashMap<>();
                map.put("name",user.getName());
                map.put("id",user.getId());
                addAdmins.add(map);
            }
            modelAndView.addObject("addAdmins",addAdmins);
            pages = Pagenation.generatePagenation(addAdmins.size(),pageable.getPageSize());
            modelAndView.addObject("pages", pages);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        modelAndView.setViewName("createInsertAdmin");
        return modelAndView;
    }

    @PreAuthorize("hasAuthroity('USER')")
    @RequestMapping("create/insert-set")
    public ModelAndView insertSet(ModelAndView modelAndView, Authentication authentication, Pageable pageable) {
        try {
            List<ProblemSet> problemSets = problemSetService.findAllProblemSets(pageable).get();
            List<Map> addProblemSets = new ArrayList<>();
            for (ProblemSet set : problemSets) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", set.getName());
                map.put("id", set.getId());
                map.put("count", set.getProblemList().size());
                if (set.isCanModify()) {
                    addProblemSets.add(map);
                }
            }
            ArrayList<Integer> pages = new ArrayList<>();
            modelAndView.addObject("addProblemsets", addProblemSets);
            modelAndView.addObject("pages", pages);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        modelAndView.setViewName("createInsertSet");
        return modelAndView;
    }
}
