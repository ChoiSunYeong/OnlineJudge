package kr.jadekim.oj.mainserver.controller.WebController;

import kr.jadekim.oj.mainserver.entity.*;
import kr.jadekim.oj.mainserver.entity.Contest;
import kr.jadekim.oj.mainserver.entity.CurrentUser;
import kr.jadekim.oj.mainserver.entity.Problem;
import kr.jadekim.oj.mainserver.entity.User;
import kr.jadekim.oj.mainserver.repository.ContestRepository;
import kr.jadekim.oj.mainserver.service.ContestService;
import kr.jadekim.oj.mainserver.service.ProblemService;
import kr.jadekim.oj.mainserver.util.Pagenation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by vmffkxlgnqh1 on 2016. 9. 22..
 */

@Controller
@PreAuthorize("hasAuthority('USER')")
@RequestMapping("/contest")
public class WebContestListController {

    @Autowired
    ContestService contestService;

    @Autowired
    ContestRepository contestRepository;
    @RequestMapping("/test")
    public ModelAndView testCreateContest(ModelAndView modelAndView, Authentication authentication){
        CurrentUser currentUser = null;
        if (authentication != null) {
            currentUser = (CurrentUser) authentication.getPrincipal();
        }
        User loginUser = null;
        if (currentUser != null) {
            loginUser = currentUser.getUser();
        }
        Date startTime = new Date();
        Date endTime = new Date();
        Contest contest = new Contest(startTime, endTime,"test");
        contestRepository.save(contest);

        modelAndView.setViewName("redirect:/contestList");
        return modelAndView;
    }

    @RequestMapping(value = "/list")
    public ModelAndView ContestList(ModelAndView modelAndView, @PageableDefault(size = 10) Pageable pageable, Authentication authentication){
        CurrentUser currentUser = null;
        if (authentication != null) {
            currentUser = (CurrentUser) authentication.getPrincipal();
        }
        Iterable<Contest> contests = null;
        User user = null;
        try {
            contests = contestService.findAllContest(pageable).get();
            if(currentUser!=null) {
                user = currentUser.getUser();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        ArrayList<Map> messages = makeMassages(contests,user);
        ArrayList<Integer> pages = Pagenation.generatePagenation(messages.size(),pageable.getPageSize());

        modelAndView.addObject("messages",messages);
        modelAndView.addObject("pages",pages);
        modelAndView.setViewName("contestList");
        return modelAndView;
    }

    public ArrayList<Map> makeMassages(Iterable<Contest> contests, User user){
        ArrayList<Map> massages = new ArrayList<>();
        for(Contest c:contests){
            Map<String,Object> map = new HashMap<>();
            Boolean isjoin = false;
            List<User> users = c.getExaminers();
            if(users != null) {
                for (int i = 0; i < users.size(); i++) {
                    if (users.get(i) == user) {
                        isjoin = true;
                        break;
                    }
                }
            }
            map.put("id",c.getId());
            map.put("isjoin",isjoin);
            map.put("name",c.getName());
            User winner = c.getWinner();
            User subWinner = c.getSubwinner();
            if(winner != null && subWinner != null){
                map.put("winner",c.getWinner().getName());
                map.put("subwinner",c.getSubwinner().getName());
            }
            else{
                map.put("winner","");
                map.put("subwinner","");
            }
            User admin = c.getAdmin();
            if(admin != null){
                map.put("admin",c.getAdmin().getName());
            }
            else{
                map.put("admin","");
            }
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date today = new Date();
            Date startTime = c.getStartTime();
            Date endTime = c.getEndTime();
            if(startTime != null && endTime != null &&today.before(endTime)&&today.after(startTime)){
                map.put("isgoing",1);
                map.put("startTime",format.format(startTime));
            }
            else if(startTime!=null && endTime!=null && today.before(startTime)){
                map.put("isgoing",2);
                map.put("startTime",format.format(startTime));
            }
            else{
                map.put("isgoing",3);
                map.put("startTime",format.format(startTime));
            }
            massages.add(map);
        }
        return massages;
    }
    @RequestMapping("info/{id}")
    public ModelAndView Contest(ModelAndView modelAndView,@PathVariable("id") int contest_id, Authentication authentication){
        Map<String,Object> contestinfo = new HashMap<>();
        Contest contest = null;
        try {
            contest = contestService.getContest(contest_id).get();
            if(contest != null) {
                contestinfo.put("id", contest.getId());
                contestinfo.put("name", contest.getName());
                Date startTime = contest.getStartTime();
                Date endTime = contest.getEndTime();
                SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm");
                contestinfo.put("start", format.format(startTime));
                contestinfo.put("end", format.format(endTime));
                User admin = contest.getAdmin();
                if(admin != null){
                    contestinfo.put("admin", admin.getName());
                }
                contestinfo.put("deputy", contest.getDeputies());
                contestinfo.put("participant", contest.getExaminers());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        ArrayList<Map> probleminfo = null;
        if(contest !=null){
            probleminfo = new ArrayList<>();
            ProblemSet problemset = contest.getProblemSet();
            List<Problem> problems = new ArrayList<>();
            if(problemset != null){
                 problems = problemset.getProblemList();

            }
            for(int i=0;i<problems.size();i++){
                Map<String,Object> problem = new HashMap<>();
                Problem prob = problems.get(i);
                problem.put("id",prob.getId());
                problem.put("name",prob.getName());
                probleminfo.add(problem);
            }
        }
        modelAndView.addObject("message",contestinfo);
        modelAndView.addObject("problems",probleminfo);
        modelAndView.setViewName("contestInfo");
        return modelAndView;
    }


    @RequestMapping("/join/{id}")
    public ModelAndView JoinContest(ModelAndView modelAndView,@PathVariable("id") int contest_id, Authentication authentication){


        ArrayList<Map> messages = new ArrayList<>();
        modelAndView.addObject("messages",messages);
        modelAndView.setViewName("contestList");
        return modelAndView;
    }
}
