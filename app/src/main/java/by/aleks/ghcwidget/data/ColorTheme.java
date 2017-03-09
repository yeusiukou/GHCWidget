package by.aleks.ghcwidget.data;

import android.graphics.Color;

import java.util.HashMap;

/**
 * Created by Alex on 12/8/14.
 */
public class ColorTheme {

    public static String GITHUB    = "GitHub";
    public static String MODERN    = "Modern";
    public static String GRAY      = "Gray";
    public static String RED       = "Red";
    public static String BLUE      = "Blue";
    public static String PURPLE    = "Purple";
    public static String ORANGE    = "Orange";
    public static String HALLOWEEN = "Halloween";


    private String[] standard  = {"#ebedf0", "#c6e48b", "#7bc96f", "#239a3b", "#196127"};
    private String[] modern    = {"#afaca8", "#d6e685", "#8cc665", "#44a340", "#1e6823"};
    private String[] gray      = {"#eeeeee", "#bdbdbd", "#9e9e9e", "#616161", "#212121"};
    private String[] red       = {"#eeeeee", "#ff7171", "#ff0000", "#b70000", "#830000"};
    private String[] blue      = {"#eeeeee", "#6bcdff", "#00a1f3", "#0079b7", "#003958"};
    private String[] purple    = {"#eeeeee", "#d2ace6", "#aa66cc", "#660099", "#4f2266"};
    private String[] orange    = {"#eeeeee", "#ffcc80", "#ffa726", "#fb8c00", "#e65100"};
    private String[] halloween = {"#eeeeee", "#ffee4a", "#ffc501", "#fe9600", "#03001c"};

    private HashMap<String, String[]> themeMap;

    public ColorTheme(){
        themeMap = new HashMap<>();
        themeMap.put(ColorTheme.GITHUB,    standard);
        themeMap.put(ColorTheme.MODERN,    modern);
        themeMap.put(ColorTheme.GRAY,      gray);
        themeMap.put(ColorTheme.RED,       red);
        themeMap.put(ColorTheme.BLUE,      blue);
        themeMap.put(ColorTheme.PURPLE,    purple);
        themeMap.put(ColorTheme.ORANGE,    orange);
        themeMap.put(ColorTheme.HALLOWEEN, halloween);
    }

    public int getColor(String themeName, int level){
        return Color.parseColor(themeMap.get(themeName)[level]);
    }

    public static CharSequence[] getThemeNames(){
        return new CharSequence[]{ColorTheme.GITHUB, ColorTheme.MODERN, ColorTheme.GRAY, ColorTheme.RED, ColorTheme.BLUE, ColorTheme.PURPLE, ColorTheme.ORANGE, ColorTheme.HALLOWEEN};
    }

}
