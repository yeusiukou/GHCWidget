package by.aleks.ghcwidget.data;

import java.util.ArrayList;

/**
 * Created by Alex on 12/8/14.
 */
public class CommitsBase implements Base{

    private ArrayList<Day> days = new ArrayList<>();
    private ArrayList< ArrayList<Day> > weeks = new ArrayList<>();
    private int currentWeek = -1;

    public void addDay(Day day){
        if (days == null)
            days = new ArrayList<Day>();

        weeks.get(currentWeek).add(day);
    }

    public void newWeek(){
        currentWeek++;
        weeks.add(new ArrayList<Day>());
    }

    public int commitsNumber(){
        int commitsCounter = 0;
        for(ArrayList<Day> days : weeks){
            for(Day day : days){
                commitsCounter += day.getCommitsNumber();
            }
        }
        return commitsCounter;
    }

    public int currentStreak() {
        int streakCounter = 0;
        for(ArrayList<Day> days : weeks){
            for(Day day : days){
                if(day.getCommitsNumber() != 0)
                    streakCounter++;
                else if (weeks.size()-1 != weeks.indexOf(days) || days.size()-1 != days.indexOf(day) ){
                    streakCounter = 0;
                }
            }
        }
        return streakCounter;
    }

    public ArrayList< ArrayList<Day> > getWeeks(){
        return weeks;
    }

}
