package org.walley.webcalclient2;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.content.Context;


public class webcalmenu extends Activity
{
  Button b_calendar;
  Button b_preferences;
  Button b_users;
  Button b_views;
  Button b_sql;
  Button b_sync;
  ListView listView;
  TextView version;
  Context context = this;

  private static final int RESULT_SETTINGS = 1;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.menu);

    create_ui();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    super.onActivityResult(requestCode, resultCode, data);

    switch (requestCode) {
    case RESULT_SETTINGS:
      validate_settings();
      showUserSettings();
      break;
    }

  }

  public void create_ui()
  {
    b_calendar = (Button) findViewById(R.id.b_calendar);
    b_preferences = (Button) findViewById(R.id.b_preferences);
    b_users = (Button) findViewById(R.id.b_users);
    b_views = (Button) findViewById(R.id.b_views);
    b_sql = (Button) findViewById(R.id.b_sql);
    b_sync = (Button) findViewById(R.id.b_sync);
    version = (TextView) findViewById(R.id.version);

    String version_name = "version";
    try {
      version_name = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
    } catch (Exception e) {
    }
    version.setText(version_name);

    b_calendar.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View arg0) {
        Intent i = new Intent();
//        i.setClass(webcalmenu.this,wcc_calendar.class);
        i.setClass(webcalmenu.this,wcc_ecv.class);
        startActivity(i);
      }
    });

    b_preferences.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View arg0) {
        Intent i_preferences = new Intent();
        i_preferences.setClass(webcalmenu.this,wcc_preferences.class);
        startActivityForResult(i_preferences, RESULT_SETTINGS);
      }
    });

    b_users.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View arg0) {
        Intent i = new Intent();
        i.setClass(webcalmenu.this,wcc_users.class);
        startActivity(i);
      }
    });

    b_views.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View arg0) {
        Intent i = new Intent();
        i.setClass(webcalmenu.this,wcc_views.class);
        startActivity(i);
      }
    });

    b_sql.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View arg0) {
        Intent i = new Intent();
        i.setClass(webcalmenu.this,wcc_sql.class);
        startActivity(i);
      }
    });

    b_sync.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View arg0) {
        Intent i = new Intent();
        i.setClass(webcalmenu.this,wcc_sync.class);
        startActivity(i);
      }
    });


    return;
  }

  private void showUserSettings()
  {
    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

    StringBuilder builder = new StringBuilder();

    builder.append("\n http username: ");
    builder.append(sharedPrefs.getString("http_username", "nic"));
    builder.append("\n http password:" + sharedPrefs.getString("http_password", "nic"));
    builder.append("\n app username: " + sharedPrefs.getString("app_username", "nic"));
    builder.append("\n app password:" + sharedPrefs.getString("app_password", "nic"));
    builder.append("\n url: " + sharedPrefs.getString("url", "nic"));

    Toast.makeText(getApplicationContext(), builder, Toast.LENGTH_LONG).show();

  }

  private void validate_settings()
  {
    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor editor;

    editor = sharedPrefs.edit();

    String u = sharedPrefs.getString("url", "nic");

    if (!u.substring(u.length() - 1).equals("/")) {
      u += "/";
      editor.putString("url", u);
      editor.commit();
    }
  }
}
