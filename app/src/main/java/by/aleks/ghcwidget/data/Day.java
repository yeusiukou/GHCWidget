package by.aleks.ghcwidget.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Alex on 12/8/14.
 */
public class Day {

    private int level;
    private int commitsNumber;
    private Calendar calendar = Calendar.getInstance();

    public Day(Date date, int commitsNumber, int level) {
        this.calendar.setTime(date);
        this.commitsNumber = commitsNumber;
        this.level = level;
    }

    public String toString() {
        return "Date:" + calendar.toString() + " Commits:" + commitsNumber + " Level:" + level;
    }

    public int getLevel() {
        return level;
    }

    public int getCommitsNumber() {
        return commitsNumber;
    }

    public void setCommitsNumber(int commitsNumber) {
        this.commitsNumber = commitsNumber;
    }

    public String getMonthName(){
        return new SimpleDateFormat("MMM").format(calendar.getTime());
    }
    public int getYear(){ return Integer.parseInt(new SimpleDateFormat("yyyy").format(calendar.getTime())); }

    public boolean isFirst(){
        //return firstDayInMonth;
        return calendar.get(Calendar.DAY_OF_MONTH)==1;
    }

}
