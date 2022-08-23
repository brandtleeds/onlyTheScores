package otsWebsite.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import otsWebsite.model.BaseballData;
import otsWebsite.model.BaseballScores;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service("scoreService")
public class ScoreService {

    @Autowired
    RestTemplate restTemplate;

    private ObjectMapper om = new ObjectMapper();

    public BaseballData getBaseballData() throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Key", "b4b69381a2msh30bbf835b4fac46p132a86jsn9db4a51f6326");
        headers.set("X-RapidAPI-Host", "api-baseball.p.rapidapi.com");

        LocalDate dateObj = LocalDate.now(ZoneId.of("America/Los_Angeles"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = dateObj.format(formatter);

        String apiCall = "https://api-baseball.p.rapidapi.com/games?date=" + date + "&league=1&season=2022&timezone=America/Los_Angeles";

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(apiCall, HttpMethod.GET, requestEntity, String.class);

        BaseballData games = om.readValue(response.getBody(), BaseballData.class);

        return games;
    }

    @CacheEvict(value = "baseballScoresCache", allEntries = true)
    public void evictBaseballScoresCache() {}

    @Cacheable("baseballScoresCache")
    public BaseballScores getBaseballScoresJSON() throws IOException {

        BaseballData data = getBaseballData();
        JSONObject jo = new JSONObject(data);

        int numberOfGamesToday = jo.getInt("results");
        String bbJsonString = "{\"numberofgamestoday\":\"" + numberOfGamesToday + "\",\"games\":[";


        for(int i = 0; i < numberOfGamesToday; i++) {

            bbJsonString += "{\"awayteam\":\"" + jo.getJSONArray("response").getJSONObject(i).getJSONObject("teams").getJSONObject("away").getString("name").replaceAll("\\.", ". ") + "\",";
            bbJsonString += "\"awayscore\":\"" + jo.getJSONArray("response").getJSONObject(i).getJSONObject("scores").getJSONObject("away").optInt("total") + "\",";
            String awayTeamNameNoSpaces = jo.getJSONArray("response").getJSONObject(i).getJSONObject("teams").getJSONObject("away").getString("name").replaceAll("\\s", "").replaceAll("\\.", "");
            bbJsonString += "\"awaylogo\":\"logo_" + awayTeamNameNoSpaces + "\",";

            bbJsonString += "\"hometeam\":\"" + jo.getJSONArray("response").getJSONObject(i).getJSONObject("teams").getJSONObject("home").getString("name").replaceAll("\\.", ". ") + "\",";
            bbJsonString += "\"homescore\":\"" + jo.getJSONArray("response").getJSONObject(i).getJSONObject("scores").getJSONObject("home").optInt("total") + "\",";
            String homeTeamNameNoSpaces = jo.getJSONArray("response").getJSONObject(i).getJSONObject("teams").getJSONObject("home").getString("name").replaceAll("\\s", "").replaceAll("\\.", "");
            bbJsonString += "\"homelogo\":\"logo_" + homeTeamNameNoSpaces + "\",";

            String gameStatusFromData = jo.getJSONArray("response").getJSONObject(i).getJSONObject("status").getString("long");
            String timeStartMilitary = jo.getJSONArray("response").getJSONObject(i).getString("time");

            String timeStartMilitaryHourString = timeStartMilitary.substring(0, timeStartMilitary.indexOf(":"));
            String timeStartMinuteString = timeStartMilitary.substring(timeStartMilitary.indexOf(":") + 1);
            int timeStartMilitaryHour = Integer.parseInt(timeStartMilitaryHourString) + 3;
            int timeStartHour = timeStartMilitaryHour;

            if (timeStartMilitaryHour > 12){
                timeStartHour = timeStartMilitaryHour - 12;
            }

            String timeStart = String.valueOf(timeStartHour) + ":" + String.valueOf(timeStartMinuteString) + " ET";

            String isHomeInningNull = "", currentInning = "", gameStatus;
            int awayInning = 1;

            for (int j = 1; j <= 11; j++) {

                try {

                    if (j == 10) {
                        int awayInningNullCheck = jo.getJSONArray("response").getJSONObject(i).getJSONObject("scores").getJSONObject("away").getJSONObject("innings").getInt("extra");
                    } else if (j == 11) {
                        currentInning = "extra";
                    } else {
                        int awayInningNullCheck = jo.getJSONArray("response").getJSONObject(i).getJSONObject("scores").getJSONObject("away").getJSONObject("innings").getInt("" + j + "");
                    }

                } catch (Exception e) {

                    if (j == 1) {
                        awayInning = 0;
                    } else {

                        awayInning = j - 1;
                    }

                    if (awayInning == 0) {
                        currentInning = "";
                    } else {
                        currentInning = "" + awayInning + "";
                    }
                    break;
                }

            }

            try {
                int homeInningNullCheck = jo.getJSONArray("response").getJSONObject(i).getJSONObject("scores").getJSONObject("home").getJSONObject("innings").getInt(currentInning);
            } catch (Exception e) {

                isHomeInningNull = "null";
            }

            if (gameStatusFromData.equals("Finished")) {
                gameStatus = "Final";
                currentInning = "";
            } else if (awayInning == 0) {
                gameStatus = timeStart;
            } else if (isHomeInningNull.equals("null")) {
                gameStatus = "Top ";
            } else {
                gameStatus = "Bot ";
            }

            if (i == numberOfGamesToday - 1) {

                if (currentInning.equals("null")) {

                    bbJsonString += "\"status\":\"" + gameStatus + "\"}]}";

                } else if (currentInning.equals("extra")){

                    gameStatus = "ExtraInnings";
                    bbJsonString += "\"status\":\"" + gameStatus + "\"}]}";

                } else {
                    bbJsonString += "\"status\":\"" + gameStatus + currentInning + "\"}]}";
                }
                break;
            }

            if (currentInning.equals("null")) {

                bbJsonString += "\"status\":\"" + gameStatus + "\"},";

            } else if (currentInning.equals("extra")){

                gameStatus = "ExtraInnings";
                bbJsonString += "\"status\":\"" + gameStatus + "\"},";

            } else {
                bbJsonString += "\"status\":\"" + gameStatus + currentInning + "\"},";
            }

        }

        BaseballScores scores = om.readValue(bbJsonString, BaseballScores.class);
        return scores;
    }

}