package by.aleks.ghcwidget.data;

import android.graphics.Color;

/**
 * Created by Alex on 12/8/14.
 */
public class ColorTheme {

    public static enum ThemeName {
        GITHUB("GitHub"), MODERN("Modern");

        private String friendlyName;

        private ThemeName(String friendlyName){
            this.friendlyName = friendlyName;
        }

        @Override public String toString() {
            return friendlyName;
        }
    }

    private static String[] standard = {"#eeeeee", "#d6e685", "#8cc665", "#44a340", "#1e6823"};
    private static String[] modern = {"#9babb9", "#d6e685", "#8cc665", "#44a340", "#1e6823"};

    public static int getColor(ThemeName themeName, int level){
        switch(themeName){
            case GITHUB: return Color.parseColor(standard[level]);
            case MODERN: return Color.parseColor(modern[level]);
        }
        throw new IllegalArgumentException("Can't find the given theme");
    }

}
