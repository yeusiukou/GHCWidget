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

    public Day(Date date, int commitsNumber, String color){
        this.calendar.setTime(date);
        this.commitsNumber = commitsNumber;
        this.level = setLevel(color);
    }

    public String toString(){
        return "Date:"+calendar.toString()+" Commits:"+commitsNumber+" Level:"+level;
    }

    private int setLevel(String color){

        // normal color scheme

        if(color.equals("#eeeeee"))
            return 0;
        if(color.equals("#d6e685"))
            return 1;
        if(color.equals("#8cc665"))
            return 2;
        if(color.equals("#44a340"))
            return 3;
        if(color.equals("#1e6823"))
            return 4;

        // spooky color scheme (only on halloween)

        if(color.equals("#eeeeee"))
            return 0;
        if(color.equals("#ffee4a"))
            return 1;
        if(color.equals("#ffc501"))
            return 2;
        if(color.equals("#fe9600"))
            return 3;
        if(color.equals("#03001c"))
            return 4;

        throw new IllegalArgumentException("Can't find the color!");
    }

    public int getLevel(){
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
