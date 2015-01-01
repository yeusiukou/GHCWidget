package by.aleks.ghcwidget;

        import android.app.PendingIntent;
        import android.appwidget.AppWidgetManager;
        import android.appwidget.AppWidgetProvider;
        import android.content.ComponentName;
        import android.content.Context;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.graphics.*;
        import android.preference.PreferenceManager;
        import android.util.Log;
        import android.view.Display;
        import android.view.WindowManager;
        import android.widget.RemoteViews;
        import by.aleks.ghcwidget.api.GitHubAPITask;
        import by.aleks.ghcwidget.data.ColorTheme;
        import by.aleks.ghcwidget.data.CommitsBase;
        import by.aleks.ghcwidget.data.Day;

        import java.util.ArrayList;

public class Widget extends AppWidgetProvider {

    public static final int STATUS_OFFLINE = 0;
    public static final int STATUS_NOTFOUND = 1;
    public static final int STATUS_ONLINE = 2;

    private static final String debugTag = "GHCWidget";
    private RemoteViews remoteViews;
    private CommitsBase base;
    private int status = STATUS_ONLINE;

    //Parameters
    private String username;
    private int months;
    private String theme;
    private boolean startOnMonday;
    private boolean showDaysLabel;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        setPreferences(context);
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
        Bitmap bitmap = processImage(context);
        if(bitmap!=null)
            remoteViews.setImageViewBitmap(R.id.commitsView, bitmap);

        switch (status){
            case STATUS_OFFLINE: printMessage(context.getResources().getString(R.string.loading_error));
                break;
            case STATUS_NOTFOUND: printMessage(context.getResources().getString(R.string.not_found));
                break;
        }


        for (int appWidgetId : appWidgetIds){
            setClickIntent(context, appWidgetId);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action != null) {
            if (action.equals(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE) ||
                    action.equals(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_ENABLED)) {

                AppWidgetManager appWM = AppWidgetManager.getInstance(context);

                //Update the Widget.
                if (appWM != null) {
                    onUpdate(context, appWM, appWM.getAppWidgetIds(intent.getComponent()));
                }
            }

            super.onReceive(context, intent);
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
        Log.d(debugTag, "Preferences updated: "+username+" "+months+" "+theme);

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
        remoteViews.setTextViewText(R.id.leftView, base.commitsNumber()+" total");
        int streak = base.currentStreak();
        if(streak == 1){
            remoteViews.setTextViewText(R.id.rightView, streak+" day");
        } else remoteViews.setTextViewText(R.id.rightView, streak+" days");
    }

    // Load data from GitHub and generate a bitmap with commits.
    private Bitmap processImage(Context context){
        CommitsBase refreshedBase = loadData(username);

        if (refreshedBase != null){
            base = refreshedBase;
            updateInfoBar(base);
        } else return null;

        Point size = getScreenSize(context);
        int weeks = 4*months+1;
        return createBitmap(base, weeks, size, theme);
    }


    //Load data from the api using AsyncTask.
    private CommitsBase loadData(String username){
        GitHubAPITask task = new GitHubAPITask(this);

        try {
            status = STATUS_ONLINE;
            CommitsBase refreshedBase = task.execute(username).get();
            return refreshedBase;
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
                canvas.drawText("M", 0, y, paintText);
                canvas.drawText("W", 0, y+2*(side+space), paintText);
                canvas.drawText("F", textSize*0.1f, y+4*(side+space), paintText);
                if(startOnMonday)
                    canvas.drawText("S", textSize*0.1f, y+6*(side+space), paintText);

                x = textSize;
            }

            y = textSize+TEXT_GRAPH_SPACE;

            ArrayList<ArrayList<Day>> weeks = base.getWeeks();

            int firstWeek = -1; //Number of the week above which there will be the first month name.

            for(int i = weeks.size() - weeksNumber; i<weeks.size(); i++){

                // Set the position and draw a month name.
                if(firstWeek!=-1 && (i+firstWeek)%4 == 0 && i!=weeks.size()-1){
                    canvas.drawText(weeks.get(i).get(1).getMonthName(), x, textSize, paintText);
                }

                for (Day day : weeks.get(i)){

                    if (firstWeek==-1 && day.isFirst())
                        firstWeek = i;
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

    private void printMessage(String msg){
        remoteViews.setTextViewText(R.id.leftView, "");
        remoteViews.setTextViewText(R.id.rightView, msg);
    }

    public void setStatus(int status){
        this.status = status;
    }

}