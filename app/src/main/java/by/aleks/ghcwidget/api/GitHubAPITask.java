package by.aleks.ghcwidget.api;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import by.aleks.ghcwidget.Widget;
import by.aleks.ghcwidget.data.CommitsBase;
import by.aleks.ghcwidget.data.Day;


public class GitHubAPITask extends AsyncTask<String, Integer, String> // Username to the input, Progress, Output
{

    private static final String debugTag = "GHCWiget";
    private Widget widget;
    private Context context;

    public GitHubAPITask(Widget widget, Context context) {
        this.widget = widget;
        this.context = context;
    }


    // Call the downloading method in background and load data
    @Override
    protected String doInBackground(String... params) {
        String result;
        try {
            Log.d(debugTag, "Background:" + Thread.currentThread().getName());
            result = GitHubHelper.downloadFromServer(params[0], context);
        } catch (GitHubHelper.ApiException e) {
            Log.d(debugTag, "Loading failed");
            e.getMessage();
            widget.setStatus(Widget.STATUS_OFFLINE);
            return null;
        }
        if (result.equals("invalid_response")) {
            widget.setStatus(Widget.STATUS_NOTFOUND);
            return null;
        }

        return result;
    }

    public static CommitsBase parseResult(final String result) throws ExecutionException, InterruptedException {

        AsyncTask<Void, Void, CommitsBase> task =  new AsyncTask<Void, Void, CommitsBase>(){

            @Override
            protected CommitsBase doInBackground(Void... params) {

                CommitsBase base = new CommitsBase();
                try {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser xpp = factory.newPullParser();

                    xpp.setInput(new StringReader(result));
                    int eventType = xpp.getEventType();

                    boolean firstTagSkipped = false;
                    SimpleDateFormat textFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_DOCUMENT: {
                                break;
                            }
                            case XmlPullParser.START_TAG: {
                                if (xpp.getName().equals("g")) {
                                    if (!firstTagSkipped) {
                                        firstTagSkipped = true;
                                        break;
                                    } else {
                                        base.newWeek();
                                        break;
                                    }

                                }
                                if (xpp.getName().equals("rect")) {
                                    Date date = textFormat.parse(xpp.getAttributeValue(null, "data-date"));
                                    int commits = Integer.valueOf(xpp.getAttributeValue(null, "data-count"));
                                    String color = xpp.getAttributeValue(null, "fill");
                                    Day day = new Day(date, commits, color);
                                    base.addDay(day);
                                    break;
                                }
                            }
                        }

                        eventType = xpp.next();
                    }

                } catch (Exception e) {
                    Log.d(debugTag, "Error in parsing");
                    e.printStackTrace();
                    return null;
                }
                return base;
            }
        };

        return task.execute().get();
    }
}