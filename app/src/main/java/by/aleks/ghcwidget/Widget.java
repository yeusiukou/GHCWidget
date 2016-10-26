package by.aleks.ghcwidget;

        import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RemoteViews;

import java.util.ArrayList;

import by.aleks.ghcwidget.api.GitHubAPITask;
import by.aleks.ghcwidget.data.ColorTheme;
import by.aleks.ghcwidget.data.CommitsBase;
import by.aleks.ghcwidget.data.Day;

public class Widget extends AppWidgetProvider {

    public static final int STATUS_OFFLINE = 0;
    public static final int STATUS_NOTFOUND = 1;
    public static final int STATUS_ONLINE = 2;

    private static final String TAG = "GHCWidget";
    private RemoteViews remoteViews;
    private CommitsBase base;
    private int status = STATUS_ONLINE;
    private int[] appWidgetIds;
    private boolean resized = false;
    private boolean online;
    private Context context;
    public static final String LOAD_DATA_KEY = "load_data";

    //Parameters
    private String username;
    private int months;
    private String theme;
    private boolean startOnMonday;
    private boolean showDaysLabel;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if(this.appWidgetIds==null)
            this.appWidgetIds = appWidgetIds;
        updateWidget(context);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action != null) {
            if (action.equals(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE) ||
                    action.equals(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_ENABLED)) {

                online = intent.getBooleanExtra(LOAD_DATA_KEY, true); //Set the flag of online/caching mode
                AppWidgetManager appWM = AppWidgetManager.getInstance(context);
                if(this.appWidgetIds==null)
                    this.appWidgetIds = appWM.getAppWidgetIds(intent.getComponent());

                updateWidget(context);
            }

            super.onReceive(context, intent);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context,
                                          AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {

        resized = true;
        updateWidget(context);
        setClickIntent(context, appWidgetId);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    /**
     * Determine appropriate view based on width provided.
     *
     * @param minWidth widget width
     * @param minHeight widget height
     * @return RemoteViews
     */
    private RemoteViews getRemoteViews(Context context, int minWidth,
                                       int minHeight) {
        // First find out rows and columns based on width provided.
        int rows = getCellsForSize(minHeight);
        int columns = getCellsForSize(minWidth);
        if(resized){
            adjustMonthsNum(context, columns, rows);
            resized = false;
        }
        if (rows == 1 && columns == 2) {
            return new RemoteViews(context.getPackageName(), R.layout.one_row_1x2);
        } else if (rows == 2 && columns == 5) {
            return new RemoteViews(context.getPackageName(), R.layout.main_2x5);
        } else if (rows == 1)
            return new RemoteViews(context.getPackageName(), R.layout.one_row);
        if (columns > 2) {
            return new RemoteViews(context.getPackageName(), R.layout.main);
        } else {
            return new RemoteViews(context.getPackageName(), R.layout.small);
        }
    }

    /**
     * Returns number of cells needed for given size of the widget.
     *
     * @param size Widget size in dp.
     * @return Size in number of cells.
     */
    private static int getCellsForSize(int size) {
        int n = 2;
        while (70 * n - 30 < size) {
            ++n;
        }
        return n - 1;
    }

    private void updateWidget(Context context){

        if(this.context == null)
            this.context = context;

        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = mgr.getAppWidgetIds(new ComponentName(context, Widget.class));
        // See the dimensions and
        Bundle options = mgr.getAppWidgetOptions(appWidgetIds[0]);

        // Get min width and height.
        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int minHeight = options
                .getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        // Obtain appropriate widget and update it.
        remoteViews = getRemoteViews(context, minWidth, minHeight);

        setPreferences(context);
        Bitmap bitmap = processImage(context);
        if(bitmap!=null)
            remoteViews.setImageViewBitmap(R.id.commitsView, bitmap);

        switch (status){
            case STATUS_OFFLINE: printMessage(context.getResources().getString(R.string.loading_error));
                break;
            case STATUS_NOTFOUND: printMessage(context.getResources().getString(R.string.not_found));
                break;
        }

        if(appWidgetIds != null){
            for (int appWidgetId : appWidgetIds){
                setClickIntent(context, appWidgetId);
            }
        }
    }


    private void setPreferences(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        username = prefs.getString("username", "xRoker");
        try{
            months = Integer.parseInt(prefs.getString("months", "5"));
            if(months>12 || months<1)
                months = 12;
        } catch (Exception e){
            months = 5;
        }
        theme = prefs.getString("color_theme", ColorTheme.GITHUB);
        startOnMonday = prefs.getBoolean("start_on_monday", false);
        showDaysLabel = prefs.getBoolean("days_labels", true);
        Log.d(TAG, "Preferences updated: " + username + " " + months + " " + theme);

    }

    //On click open the preferences activity
    private void setClickIntent(Context context, int appWidgetId) {

        Intent launchActivity = new Intent(context, WidgetPreferenceActivity.class);
        launchActivity.setAction("android.appwidget.action.APPWIDGET_CONFIGURE");
        launchActivity.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchActivity, 0);

        remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

        ComponentName thisWidget = new ComponentName(context, Widget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(thisWidget, remoteViews);

    }

    private void updateInfoBar(CommitsBase base){
        remoteViews.setTextViewText(R.id.total, String.valueOf(base.commitsNumber()));
        remoteViews.setTextViewText(R.id.totalTextView, context.getString(R.string.total));
        int streak = base.currentStreak();
        remoteViews.setTextViewText(R.id.days, String.valueOf(streak));
        if(streak <= 1){
            remoteViews.setTextViewText(R.id.daysTextView, context.getString(R.string.day));
        } else remoteViews.setTextViewText(R.id.daysTextView, context.getString(R.string.days));
    }

    // Load data from GitHub and generate a bitmap with commits.
    private Bitmap processImage(Context context){

        if(base == null || online){
            CommitsBase refreshedBase = loadData(context, username);
            if (refreshedBase != null){
                base = refreshedBase;
                updateInfoBar(base);
            } else return null;
        }

        Point size = getScreenSize(context);
        int weeks = 4*months+1;
        return createBitmap(base, weeks, size, theme);
    }


    //Load data from the api using AsyncTask.
    private CommitsBase loadData(Context context, String username){
        String prefDataKey = "offline_data";
        GitHubAPITask task = new GitHubAPITask(this, context);

        try {
            status = STATUS_ONLINE;
            String data;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();
            // If the widget have to be updated online, load data and save it to SharedPreferences
            if(online || !prefs.contains(prefDataKey)){
                data = task.execute(username).get();
                if(data!=null){
                    editor.putString(prefDataKey, data).apply();
                }
            } else data = prefs.getString(prefDataKey, null);
            return GitHubAPITask.parseResult(data);
        }
        catch (Exception e)
        {
            task.cancel(true);
            return null;
        }
    }

    private Point getScreenSize(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }


    private Bitmap createBitmap(CommitsBase base, int weeksNumber, Point size, String theme){
        float SPACE_RATIO = 0.1f;
        int TEXT_GRAPH_SPACE = 7;

        float daysLabelSpaceRatio = showDaysLabel ? 0.8f : 0;


        float side = size.x/(weeksNumber+daysLabelSpaceRatio) * (1-SPACE_RATIO);
        float space = size.x/(weeksNumber+daysLabelSpaceRatio) - side;
        float textSize = side*0.87f;

        int height = (int)(7*(side+space)+textSize+TEXT_GRAPH_SPACE);

        ColorTheme colorTheme = new ColorTheme();

        Bitmap bitmap = Bitmap.createBitmap(size.x, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paintText.setTextSize(textSize);
        paintText.setColor(Color.GRAY);

        if(base!=null){
            float x = 0, y;

            // Draw days labels.
            if(showDaysLabel){
                y = startOnMonday ? textSize*2+TEXT_GRAPH_SPACE : textSize*2+TEXT_GRAPH_SPACE+side;
                canvas.drawText(context.getString(R.string.m), 0, y, paintText);
                canvas.drawText(context.getString(R.string.w), 0, y+2*(side+space), paintText);
                canvas.drawText(context.getString(R.string.f), textSize*0.1f, y+4*(side+space), paintText);
                if(startOnMonday)
                    canvas.drawText(context.getString(R.string.s), textSize*0.1f, y+6*(side+space), paintText);

                x = textSize;
            }

            y = textSize+TEXT_GRAPH_SPACE;

            ArrayList<ArrayList<Day>> weeks = base.getWeeks();

            int firstWeek = base.getFirstWeekOfMonth(); //Number of the week above which there will be the first month name.

            for(int i = weeks.size() - weeksNumber; i<weeks.size(); i++){

                // Set the position and draw a month name.
                if( (firstWeek!=-1 && (i-weeks.size()+firstWeek)%4 == 0 && i!=weeks.size()-1) || firstWeek==i){
                    canvas.drawText(weeks.get(i).get(1).getMonthName(), x, textSize, paintText);
                }

                for (Day day : weeks.get(i)){

                    if(startOnMonday && weeks.get(i).indexOf(day)==0)
                        continue;

                    paint.setColor(colorTheme.getColor(theme, day.getLevel()));
                    canvas.drawRect(x, y, x+side, y+side, paint);
                    y = y + side + space;
                }
                if(startOnMonday){
                    try {
                        paint.setColor(colorTheme.getColor(theme, weeks.get(i + 1).get(0).getLevel()));
                    } catch (IndexOutOfBoundsException e) {
                        break;
                    }
                    canvas.drawRect(x, y, x+side, y+side, paint);
                    y = y + side + space;
                }
                y = textSize+TEXT_GRAPH_SPACE;
                x = x + side + space;
            }
        }

        return bitmap;
    }

    private void adjustMonthsNum(Context context, int numColumns, int numRows){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        if(numRows > 1){
            switch (numColumns){
                case 2: editor.putString("months", "2");
                    break;
                case 3: editor.putString("months", "4");
                    break;
                case 4: editor.putString("months", "5");
                    break;
                case 5: editor.putString("months", "5");
                    break;
                case 6: editor.putString("months", "7");
                    break;
                case 8: editor.putString("months", "9");
                    break;
                default: editor.putString("months", "12");
            }
        } else {
            switch (numColumns){
                case 2: editor.putString("months", "2");
                    break;
                case 3: editor.putString("months", "5");
                    break;
                case 4: editor.putString("months", "6");
                    break;
                case 5: editor.putString("months", "7");
                    break;
                case 6: editor.putString("months", "11");
                    break;
                default: editor.putString("months", "12");
            }
        }
        editor.apply();
    }

    private void printMessage(String msg){
        remoteViews.setTextViewText(R.id.total, "");
        remoteViews.setTextViewText(R.id.totalTextView, "");
        remoteViews.setTextViewText(R.id.days, "");
        remoteViews.setTextViewText(R.id.daysTextView, msg);
    }

    public void setStatus(int status){
        this.status = status;
    }

}