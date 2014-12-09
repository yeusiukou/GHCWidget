package by.aleks.ghcwidget.data;

import android.graphics.Color;

import java.util.HashMap;

/**
 * Created by Alex on 12/8/14.
 */
public class ColorTheme {

    public static String GITHUB = "GitHub";
    public static String MODERN = "Modern";


    private String[] standard = {"#eeeeee", "#d6e685", "#8cc665", "#44a340", "#1e6823"};
    private String[] modern = {"#afaca8", "#d6e685", "#8cc665", "#44a340", "#1e6823"};

    private HashMap<String, String[]> themeMap;

    public ColorTheme(){
        themeMap = new HashMap<>();
        themeMap.put(ColorTheme.GITHUB, standard);
        themeMap.put(ColorTheme.MODERN, modern);
    }

    public int getColor(String themeName, int level){
        return Color.parseColor(themeMap.get(themeName)[level]);
    }

    public static CharSequence[] getThemeNames(){
        return new CharSequence[]{ColorTheme.GITHUB, ColorTheme.MODERN};
    }

}
