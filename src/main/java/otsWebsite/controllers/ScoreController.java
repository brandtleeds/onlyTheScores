package otsWebsite.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import otsWebsite.model.BaseballData;
import otsWebsite.model.BaseballScores;
import otsWebsite.services.ScoreService;

import java.io.IOException;

@RestController
public class ScoreController {

    public ScoreController() throws IOException {
    }

    private ObjectMapper om = new ObjectMapper();

    @Autowired
    ScoreService scoreService;
    BaseballScores baseballScores;

    @GetMapping("/baseball")
    @ResponseBody
    public ResponseEntity<Object> getBaseballData() throws IOException{

        BaseballData scores = scoreService.getBaseballData();

        return new ResponseEntity<>(scores, HttpStatus.OK);
    }

    @Scheduled(cron = "0 */2 0-1,07-23 * 2-10 *", zone = "America/Los_Angeles")
    public void scheduledBaseballScoresCacheRefresher() throws IOException{

        scoreService.evictBaseballScoresCache();
        baseballScores = scoreService.getBaseballScoresJSON();
        System.out.println("Scores updated.");
    }

    int baseballScoresCacheInitializer = 0;
    @RequestMapping(value={"/", "/index"}, method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView getScores() throws IOException{


        if (baseballScoresCacheInitializer == 0) {
            baseballScores = scoreService.getBaseballScoresJSON();
            baseballScoresCacheInitializer = 1;
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("baseballScores", baseballScores);

        modelAndView.setViewName("index");
        return modelAndView;
    }
}
