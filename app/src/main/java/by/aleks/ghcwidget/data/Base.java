package by.aleks.ghcwidget.data;

import java.util.ArrayList;

/**
 * Created by Alex on 12/17/14.
 */
public interface Base {
    public void addDay(Day day);
    public void newWeek();
    public int commitsNumber();
    public int currentStreak();
    public ArrayList<ArrayList<Day>> getWeeks();
}
