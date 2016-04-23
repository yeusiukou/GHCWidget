package by.aleks.ghcwidget;

/**
 * Created by Alex on 12/7/14.
 */
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;
import by.aleks.ghcwidget.data.ColorTheme;

public class WidgetPreferenceActivity extends PreferenceActivity {
    private static final String TAG = "GHCW";
    private static final String CONFIGURE_ACTION = "android.appwidget.action.APPWIDGET_CONFIGURE";
    private Preference loginPref, logoutPref;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        ListPreference themePref = (ListPreference)findPreference("color_theme");
        themePref.setEntries(ColorTheme.getThemeNames());
        themePref.setEntryValues(ColorTheme.getThemeNames());
        if(themePref.getValue() == null){
            themePref.setValue(ColorTheme.GITHUB);
        }

        //Set up the Listener.
        findPreference("username").setOnPreferenceChangeListener(onPreferenceChange);
        findPreference("color_theme").setOnPreferenceChangeListener(onPreferenceChange);
        findPreference("months").setOnPreferenceChangeListener(onPreferenceChange);
        findPreference("start_on_monday").setOnPreferenceChangeListener(onPreferenceChange);
        findPreference("days_labels").setOnPreferenceChangeListener(onPreferenceChange);


        loginPref = findPreference("login");
        loginPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent loginIntent = new Intent().setClassName(WidgetPreferenceActivity.this, "by.aleks.ghcwidget.LoginActivity");
                startActivity(loginIntent);
                return true;
            }
        });

        logoutPref = findPreference("logout");
        logoutPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                clearCookies();
                displayLoginButton();
                updateWidget(true);
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayLoginButton();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // On successful login update widget
        if (resultCode == RESULT_OK)
            updateWidget(true);
    }

    protected void displayLoginButton(){
        String cookies = CookieManager.getInstance().getCookie(getString(R.string.login_url));
        PreferenceScreen screen = getPreferenceScreen();
        // If there are logged in cookies, show the "logout" button, otherwise show "login" button
        if(cookies != null && cookies.split(";")[0].equals("logged_in=yes")){
            screen.removePreference(loginPref);
            screen.addPreference(logoutPref);
            updateWidget(true);
        } else {
            screen.addPreference(loginPref);
            screen.removePreference(logoutPref);
        }
    }

    @SuppressWarnings("deprecation")
    private void clearCookies()
    {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Using ClearCookies code for API >= Lollipop
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else
        {
            // Using ClearCookies code for API < Lollipop
            CookieSyncManager cookieSyncMngr=CookieSyncManager.createInstance(this);
            cookieSyncMngr.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    /**
     * When the user changes the preferences, update the widget.
     */
    private OnPreferenceChangeListener onPreferenceChange = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            String key = preference.getKey();

            // Exit if the username is invalid
            if(key.equals("username")){
                if(!isUsernameValid((String)newValue)){
                    alert("Invalid username");
                    return false;
                }
            }

            Intent activityIntent = getIntent();
            if (CONFIGURE_ACTION.equals(activityIntent.getAction())) {
                Bundle extras = activityIntent.getExtras();

                if (extras != null) {
                    int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                            AppWidgetManager.INVALID_APPWIDGET_ID);

                    //Put the widget ID into the extras and let the activity caller know the result is ok.
                    Intent result = new Intent();
                    result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

                    setResult(RESULT_OK, result);
                    Log.d(TAG, preference.getTitle().toString());
                    //Update the widget.
                    updateWidget(preference.getKey().equals("username"));

                    finish();
                }
                else {
                    Log.d(TAG, "Intent Extras is null");

                    return false;
                }
            }
            else {
                Log.d(TAG, "Intent Action is: {" + activityIntent.getAction() + "} and not: " + CONFIGURE_ACTION);

                return false;
            }

            return true;
        }
    };

    /**
     * Send an intent to update the widget.
     */
    private void updateWidget(boolean online) {
        Intent updateIntent = new Intent(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE,
                Uri.EMPTY, this, Widget.class);
        updateIntent.putExtra(Widget.LOAD_DATA_KEY, online);
        sendBroadcast(updateIntent);
    }

    private boolean isUsernameValid(String value){
        return value.matches("[a-zA-Z0-9-]+");
    }


    public void alert (String msg)
    {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
}