package by.aleks.ghcwidget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashSet;
import java.util.Set;

public class LoginActivity extends ActionBarActivity {

    private WebView loginView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(getString(R.string.login_to_github));

        loginView = (WebView)findViewById(R.id.login_view);
        loginView.loadUrl(getString(R.string.login_url));
        loginView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                String cookies = CookieManager.getInstance().getCookie(url);
                if(cookies != null){
                    if(cookies.split(";")[0].equals("logged_in=yes")){
                        Intent returnIntent = new Intent();
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    }
                }
            }
        });
    }
}
