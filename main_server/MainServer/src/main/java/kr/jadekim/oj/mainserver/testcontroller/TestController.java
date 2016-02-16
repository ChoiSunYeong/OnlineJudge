package kr.jadekim.oj.mainserver.testcontroller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kr.jadekim.oj.mainserver.entity.Answer;
import kr.jadekim.oj.mainserver.entity.GradeResult;
import kr.jadekim.oj.mainserver.entity.Problem;
import kr.jadekim.oj.mainserver.entity.User;
import kr.jadekim.oj.mainserver.repository.AnswerRepository;
import kr.jadekim.oj.mainserver.repository.GradeResultRepository;
import kr.jadekim.oj.mainserver.repository.ProblemRepository;
import kr.jadekim.oj.mainserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

/**
 * Created by ohyongtaek on 2016. 2. 15..
 */

@Controller
@RequestMapping("/")
public class TestController {

    Gson gson = new GsonBuilder().create();

    @Autowired
    ProblemRepository problemRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AnswerRepository answerRepository;

    @Autowired
    GradeResultRepository gradeResultRepository;

    @RequestMapping("/test")
    public @ResponseBody String createAnswer(){
        User user = userRepository.findAll().get(0);
        Date date = new Date();
        Problem problem = new Problem();
        problemRepository.save(problem);
        Answer answer = new Answer(user,"asdf",date,problem);
        GradeResult gradeResult = new GradeResult();
        gradeResult.setSuccess(true);
        gradeResultRepository.save(gradeResult);
        answer.setResult(gradeResult);
        answerRepository.save(answer);
        user.addAnswer(answer);
        userRepository.save(user);


        int count = answerRepository.countBySuccessAndProblemId(1);
        return count+"";
    }

    @RequestMapping("/query")
    public @ResponseBody  String getUser(){
        List<User> users = userRepository.findAll();
        for(User u : users){
            for(Answer a : u.getAnswers()){
                System.out.println(a.getResult().isSuccess());
            }
        }

        return "wait";
    }

    @RequestMapping
    public ModelAndView list(){
        ArrayList<Map> messages = new ArrayList<>();
        Iterable<Problem> problems = problemRepository.findAll();
        User user = userRepository.findAll().get(0);
        for(Problem p : problems){
            Map<String,Object> map = new HashMap<>();
            int success_count = answerRepository.countBySuccessAndProblemId(p.getId());
            int total_count = answerRepository.countByProblemId(p.getId());
            double rate = success_count/total_count *100;
            boolean isSuccess = answerRepository.isSuccessByUserId(user.getId(),p.getId());
            map.put("id",p.getId());
            map.put("name",p.getName());
            map.put("count",success_count);
            map.put("rate",rate);
            map.put("result",isSuccess);
        }
        return new ModelAndView("problem","problems",problems);
    }

}
