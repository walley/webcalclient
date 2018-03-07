package org.walley.webcalclient2;

import java.util.TimeZone;
import java.util.Calendar;
import java.util.Locale;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Toast;
import android.content.Context;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.ConsoleMessage;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import java.util.ArrayList;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.text.TextUtils;
import android.content.Intent;
import org.json.JSONArray;
import org.json.JSONException;

//import android.os.Build;
//import android.app.Application;
//import android.content.pm.ApplicationInfo;

/******************************************************************************/
/******************************************************************************/
/******************************************************************************/
public class wcc_webview extends wcc_activity
/******************************************************************************/
/******************************************************************************/
/******************************************************************************/
{

  private WebView webView;
  String html_script = "";
  SQLiteDatabase mydb = null;
  String view_id = "";
  String view_type = "";

  /******************************************************************************/
  public void onCreate(Bundle savedInstanceState)
  /******************************************************************************/
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.webview);

    try {
      Bundle bundle = getIntent().getExtras();
      view_id = (String) bundle.getString("view_id");
      Log.d("WC","wcc_webview onCreate(): " + view_id);
    } catch (Exception e) {
      Log.d("WC", "wcc_webview onCreate(): empty intent " + e.toString());
    }

    view_type = get_view_type(view_id);

    Log.d("WC", "wcc_webview view_id view_type" + view_id + view_type);

    webView = (WebView) findViewById(R.id.webView1);
    webView.setWebChromeClient(new WebChromeClient() {
      public boolean onConsoleMessage(ConsoleMessage cm) {
        Log.d("WC", cm.message() + " -- From line "
              + cm.lineNumber() + " of "
              + cm.sourceId() );
        return true;
      }
    });
    webView.getSettings().setJavaScriptEnabled(true);
    webView.getSettings().setBuiltInZoomControls(true);
    webView.getSettings().setSupportZoom(true);

    webView.addJavascriptInterface(new WebAppInterface(this), "Android");
    webView.loadData("", "text/html", null);

    final String mimeType = "text/html";
    final String encoding = "UTF-8";

    InputStream inputStream = null;
    int resource = 0;

    switch (view_type) {
    case "D":
      resource = R.raw.viewd;
      break;
    case "E":
      resource = R.raw.viewe;
      break;
    case "L":
      resource = R.raw.viewl;
      break;
    case "M":
      resource = R.raw.viewm;
      break;
    case "R":
      resource = R.raw.viewr;
      break;
    case "S":
      resource = R.raw.views;
      break;
    case "T":
      resource = R.raw.viewt;
      break;
    case "V":
      resource = R.raw.viewv;
      break;
    case "W":
      resource = R.raw.vieww;
      break;

    default:
      resource = R.raw.viewx;
    }

    inputStream = getResources().openRawResource(resource);
    String html_script = "error something should be here error";
    try {
      html_script = IOUtils.toString(inputStream);
    } catch (IOException e) {
      Log.e("WC","read script " + e.toString());
      e.printStackTrace();
    }

    String events = get_arrays();

    Log.e("WC","get arrays " + events);

    Calendar cal = Calendar.getInstance(TimeZone.getDefault(), Locale.ENGLISH);
    int year = cal.get(Calendar.YEAR);
    int month = cal.get(Calendar.MONTH) + 1;
    int day = cal.get(Calendar.DAY_OF_MONTH);
    int hour = cal.get(Calendar.HOUR_OF_DAY);
    int minute = cal.get(Calendar.MINUTE);

    String html =
      getResources().getString(R.string.html_head) + "\n" +
      "\n<script>\n" + events + "\n</script>\n" +
      html_script +
      getResources().getString(R.string.html_between) +
      getResources().getString(R.string.html_body) +
      "\n<script>display_calendar("+year +"," + month + "," + day + ");</script>\n" +
      getResources().getString(R.string.html_end);

    System.out.println(html);

    /*    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
          if (0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
            WebView.setWebContentsDebuggingEnabled(true);
          }
        }
    */
    webView.loadDataWithBaseURL("", html, mimeType, encoding, "");

    /*      "<input type='button' value='Say hell' onClick='showAndroidToast(\"Hello Android!\")' />"+
          "<script type='text/javascript'>"+
          "    function showAndroidToast(toast) {"+
          "        Android.showToast(toast);"+
          "    }"+
          "</script>" +
    */
  }

  /******************************************************************************/
  public class WebAppInterface
  /******************************************************************************/
  {
    Context mContext;

    /** Instantiate the interface and set the context */
    WebAppInterface(Context c)
    {
      mContext = c;
    }

    /** Show a toast from the web page */
    //@JavascriptInterface
    public void showToast(String toast)
    {
      Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    public void show_event(String id)
    {
      Intent i = new Intent();
      i.setClass(wcc_webview.this,wcc_editevent.class);
      i.putExtra("id", id);
      i.putExtra("action", "edit_event");
      startActivity(i);
    }
  }

  int event_number = 0;

  /******************************************************************************/
  public String get_arrays()
  /******************************************************************************/
  {

    event_number = 0;

//    String event_string = "var events = {};\n";
//declare as array for sort
    String event_string = "var events = [];\n";
    String user_names_string = "var user_names = [";
    String user_string = "var users = ";

//get users for view

//    SQLiteDatabase mydb = null;
    String query;
    String users;
    String users_json = "[]";
    JSONArray json_array = null;

    query  = "select users from views where id = " + view_id;
    mydb = open_db();

    try {
      Cursor query_result  = mydb.rawQuery(query, null);

      if(query_result.moveToFirst()) {
        do {
          users_json = query_result.getString(0);
          Log.d("WC","wcc_webview get_arrays(): users json array:" + users_json);
        } while (query_result.moveToNext());
      }
    } catch(Exception e) {
      Log.d("WC","wcc_webview get_arrays error " + e.toString());
      e.printStackTrace();
    }
    close_db(mydb);

    ArrayList<String> names_list = new ArrayList<String>();

    try {
      json_array = new JSONArray(users_json);

      for (int i = 0; i < json_array.length(); i++) {
        event_string += user_events(json_array.getString(i));
        names_list.add("\"" + get_users_name_by_login(json_array.getString(i)) + "\"");
      }
    } catch (JSONException e) {
    }

    user_names_string += TextUtils.join(", ", names_list);
    user_names_string += "];";

    String ret = user_string + users_json + ";" + "\n" + event_string + "\n" + user_names_string;
    Log.d("WC","wcc_webview get_arrays(): return:" + ret);
    return ret;
  }


  /******************************************************************************/
  public Integer value_or_null(String val)
  /******************************************************************************/
  {
    if (val.equals("null")) {
      return 0;
    } else {
      return Integer.parseInt(val);
    }
  }

  /******************************************************************************/
  public String user_events(String user)
  /******************************************************************************/
  {

    //non repeating events
    String query = "select * from event where user='" + user + "' and cal_type='null' order by starttimestamp";
    String event_string = "";
    String event = "";
    Calendar calendar;

    Integer id_i = 0;
    Integer sts_i = 0;
    Integer title_i = 0;
    Integer duration_i = 0;

    int i_cal_id;
    int i_cal_type;
    int i_cal_end;
    int i_cal_frequency;
    int i_cal_days;
    int i_cal_endtime;
    int i_cal_bymonth;
    int i_cal_bymonthday;
    int i_cal_byday;
    int i_cal_bysetpos;
    int i_cal_byweekno;
    int i_cal_byyearday;
    int i_cal_wkst;
    int i_cal_count;

    mydb = open_db();
    try {
      Cursor query_result  = mydb.rawQuery(query, null);

      id_i = query_result.getColumnIndex("id");
      sts_i = query_result.getColumnIndex("starttimestamp");
      title_i = query_result.getColumnIndex("label");
      duration_i = query_result.getColumnIndex("duration");

      if(query_result.moveToFirst()) {
        do {
          String id = query_result.getString(id_i);
          long sts_l = query_result.getLong(sts_i);
          String title = query_result.getString(title_i);

          calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.ENGLISH);
          calendar.setTimeInMillis(sts_l * 1000);

          int sts = calendar.get(Calendar.DAY_OF_MONTH);

          int year = calendar.get(Calendar.YEAR);
          int month = calendar.get(Calendar.MONTH) + 1;
          int day = calendar.get(Calendar.DAY_OF_MONTH);
          int hour = calendar.get(Calendar.HOUR_OF_DAY);
          int minute = calendar.get(Calendar.MINUTE);

          String user_name = get_users_name_by_login(user);

          event  = "events["+ event_number++ +"] = {";
          event += "day: " + sts;
          event += ", year: " + year;
          event += ", month: " + month;
          event += ", hour: " + hour;
          event += ", minute: " + minute;
          event += ", user: '" + user + "'";
          event += ", user_name: '" + user_name + "'";
          event += ", id: " + id;
          event += ", event:\""+title+"\"";
          event += ", repeat: 'none'";
          event += "};";

          Log.d("WC","wcc_webview user_events(): " + event);
          event_string += event + "\n";

        } while(query_result.moveToNext());
      }
    } catch(Exception e) {
      Log.e("WC","wcc_webview user_events(" + user + "): "+ query + "error:" + e.toString());
      e.printStackTrace();
    }

    String repeat = "none";
    query = "select * from event where user='" + user + "' and cal_type='daily' group by cal_id";
    try {

      Cursor query_result  = mydb.rawQuery(query, null);

      id_i = query_result.getColumnIndex("id");
      sts_i = query_result.getColumnIndex("starttimestamp");
      title_i = query_result.getColumnIndex("label");
      duration_i = query_result.getColumnIndex("duration");

      i_cal_id          = query_result.getColumnIndex("cal_id");
      i_cal_type        = query_result.getColumnIndex("cal_type");
      i_cal_end         = query_result.getColumnIndex("cal_end");
      i_cal_frequency   = query_result.getColumnIndex("cal_frequency");
      i_cal_days        = query_result.getColumnIndex("cal_days");
      i_cal_endtime     = query_result.getColumnIndex("cal_endtime");
      i_cal_bymonth     = query_result.getColumnIndex("cal_bymonth");
      i_cal_bymonthday  = query_result.getColumnIndex("cal_bymonthday");
      i_cal_byday       = query_result.getColumnIndex("cal_byday");
      i_cal_bysetpos    = query_result.getColumnIndex("cal_bysetpos");
      i_cal_byweekno    = query_result.getColumnIndex("cal_byweekno");
      i_cal_byyearday   = query_result.getColumnIndex("cal_byyearday");
      i_cal_wkst        = query_result.getColumnIndex("cal_wkst");
      i_cal_count       = query_result.getColumnIndex("cal_count");

      if(query_result.moveToFirst()) {
        do {

          int fq = value_or_null(query_result.getString(i_cal_frequency));
          int count = value_or_null(query_result.getString(i_cal_count));
          int end = value_or_null(query_result.getString(i_cal_end));

          repeat = "none";
          //daily forever
          if (fq > 0 && count == 0) {
            repeat = "forever";
          }

          //daily 'count' times
          if (fq > 0 && count > 0) {
            //create multiple events
            repeat = "x";
          }

          //daily til end date
          if (fq > 0 && end > 0) {
            //create multiple events
            repeat = "end";
          }

          String id = query_result.getString(id_i);
          long sts_l = query_result.getLong(sts_i);
          String title = query_result.getString(title_i);

          calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.ENGLISH);
          calendar.setTimeInMillis(sts_l * 1000);

          int sts = calendar.get(Calendar.DAY_OF_MONTH);

          int year = calendar.get(Calendar.YEAR);
          int month = calendar.get(Calendar.MONTH) + 1;
          int day = calendar.get(Calendar.DAY_OF_MONTH);
          int hour = calendar.get(Calendar.HOUR_OF_DAY);
          int minute = calendar.get(Calendar.MINUTE);

          String user_name = get_users_name_by_login(user);

          event  = "events["+ event_number++ +"] = {";
          event += "day: " + sts;
          event += ", year: " + year;
          event += ", month: " + month;
          event += ", hour: " + hour;
          event += ", minute: " + minute;
          event += ", user: '" + user + "'";
          event += ", user_name: '" + user_name + "'";
          event += ", id: " + id;
          event += ", event:\""+title+"\"";
          event += ", repeat: '"+ repeat +"'";
          event += "};";

          Log.d("WC","wcc_webview user_events(): repeatable " + event);
          event_string += event + "\n";

        } while(query_result.moveToNext());
      }

    } catch(Exception e) {
      Log.e("WC","wcc_webview user_events(" + user + "): "+ query + "error:" + e.toString());
      e.printStackTrace();
    }

    close_db(mydb);
    return event_string;

  }

  /******************************************************************************/
  public String get_view_type(String view_id)
  /******************************************************************************/
  {
    String query = "select typ from views where id='" + view_id + "'";
    return do_query(query);
  }

  /******************************************************************************/
  public String get_users_name_by_login(String login)
  /******************************************************************************/
  {
    String query = "select name from users where login='" + login + "'";
    return do_query(query);
  }

}