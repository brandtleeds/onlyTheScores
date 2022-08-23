package otsWebsite.model;

public class BaseballScores {

    int numberofgamestoday;
    Object[] games;

    public BaseballScores(Object[] games) {
        this.games = games;
    }

    public BaseballScores() {
    }

    public int getNumberofgamestoday() {
        return numberofgamestoday;
    }

    public void setNumberofgamestoday(int numberofgamestoday) {
        this.numberofgamestoday = numberofgamestoday;
    }

    public Object[] getGames() {
        return games;
    }

    public void setGames(Object[] games) {
        this.games = games;
    }
}
