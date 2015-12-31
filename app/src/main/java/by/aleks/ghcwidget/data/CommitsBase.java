package by.aleks.ghcwidget.data;

import java.util.ArrayList;

/**
 * Created by Alex on 12/8/14.
 */
public class CommitsBase {

    private ArrayList<Day> days = new ArrayList<>();
    private ArrayList< ArrayList<Day> > weeks = new ArrayList<>();
    private int currentWeek = -1;
    private Day currentDay;

    public void addDay(Day day){
        if (days == null)
            days = new ArrayList<Day>();

        // Decline the current week in the case it was created because od a new year.
        if (currentDay!=null && currentWeek > 0 && day.getYear() > currentDay.getYear()){
            if(weeks.get(currentWeek-1).size()<7){
                weeks.remove(currentWeek);
                currentWeek--;
            }
            //Skip the previous year days after a new year.
        } else if (currentDay!=null && day.getYear() < currentDay.getYear())
            return;

        weeks.get(currentWeek).add(day);
        currentDay = day;
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

    /** Returns a very first week in a month from the given range*/
    public int getFirstWeekOfMonth(int weeksNum){
        int firstWeekOfLast = -1;
        for(int i = weeks.size()-1; i > 0; i--){
            for(Day day : weeks.get(i)){
                if(day.isFirst()){
                    firstWeekOfLast = weeks.size()-1 - i;
                    break;
                }
            }
        }
        return firstWeekOfLast%4;
    }

    public ArrayList< ArrayList<Day> > getWeeks(){
        return weeks;
    }

}
