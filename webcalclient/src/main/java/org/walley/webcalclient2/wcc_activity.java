package org.walley.webcalclient2;

import android.app.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.TimeZone;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.cookie.Cookie;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.StatusLine;


public class wcc_activity extends Activity
{
  private SharedPreferences prefs;

  String http_username;
  String http_password;
  String app_username;
  String app_password;

  String class_prefix = "wcc_activity-";
  String func_prefix = "func_prefix";

  /******************************************************************************/
  public boolean isOnline()
  /******************************************************************************/
  {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = cm.getActiveNetworkInfo();
    return netInfo != null && netInfo.isConnectedOrConnecting();
  }

  /******************************************************************************/
  public SQLiteDatabase open_db()
  /******************************************************************************/
  {
    SQLiteDatabase db = null;

    try {
      db = openOrCreateDatabase("cal.db", Context.MODE_PRIVATE,null);
    } catch (Exception e) {
      Log.e("WC","open_db(): error opening db" + e.toString());
      e.printStackTrace();
    }

    return db;
  }

  /******************************************************************************/
  public SQLiteDatabase open_db(String file)
  /******************************************************************************/
  {
    SQLiteDatabase db = null;

    try {
      db = openOrCreateDatabase(file, Context.MODE_PRIVATE,null);
    } catch (Exception e) {
      Log.e("WC","open_db(): error opening db" + e.toString());
      e.printStackTrace();
    }

    return db;
  }

  /******************************************************************************/
  public void update_changed(int id, String s)
  /******************************************************************************/
  {
    SQLiteDatabase db = open_db();

    try {
      db.execSQL("UPDATE  SET wcc_changed = " + s + " WHERE id = " + id);
    } catch (Exception e) {
      Log.e("WC", "update-changed Error encountered");
    }

    close_db(db);
  }


  /******************************************************************************/
  public void close_db(SQLiteDatabase db)
  /******************************************************************************/
  {
    try {
      db.close();
    } catch (Exception e) {
      Log.e("WC","close_db(): error closing db" + e.toString());
      e.printStackTrace();
    }
  }

  /******************************************************************************/
  public void do_sql(SQLiteDatabase db, String query)
  /******************************************************************************/
  {
    try {
      db.execSQL(query);
    } catch (Exception e) {
      Log.e("WC","do_sql(): error " + e.toString());
    }
  }

  /******************************************************************************/
  public String do_query(String query)
  /******************************************************************************/
  {
    String ret = "";
    SQLiteDatabase mydb = open_db();

    try {
      Cursor query_result  = mydb.rawQuery(query, null);

      if (query_result.moveToFirst()) {
        ret = query_result.getString(0);
      }
    } catch (Exception e) {
      Log.d("WC","do_query error query: " + " error: " + e.toString());
      e.printStackTrace();
    }

    close_db(mydb);
    return ret;
  }

  /******************************************************************************/
  public void set_credentials()
  /******************************************************************************/
  {

    prefs = PreferenceManager.getDefaultSharedPreferences(this);

    try {
      http_username = prefs.getString("http_username", "nonono");
      http_password = prefs.getString("http_password", "nonono");
      app_username = prefs.getString("app_username", "nonono");
      app_password = prefs.getString("app_password", "nonono");

    } catch (Exception e) {
      Log.e("WC"," set_credentials():" + e.toString());
      e.printStackTrace();
    }

  }

  /******************************************************************************/
  public String get_credentials()
  /******************************************************************************/
  {
//    Log.d("WC","get_credentials(): " + http_username + ":" + http_password);
    return Base64.encodeToString((http_username + ":" + http_password).getBytes(), Base64.NO_WRAP);
  }

  /******************************************************************************/
  public Calendar get_calendar_from_datetext(String datetext)
  /******************************************************************************/
  {
    Calendar calendar = null;

    try {
      DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
      df.setTimeZone(TimeZone.getTimeZone("UTC"));

      Date result =  df.parse(datetext);

      calendar = Calendar.getInstance();
      calendar.setTime(result);
      calendar.setTimeZone(TimeZone.getDefault());

    } catch (ParseException pe) {
      pe.printStackTrace();
    }

    return calendar;
  }

  /******************************************************************************/
  public Calendar get_calendar_from_datetext_localzone(String datetext)
  /******************************************************************************/
  {
    Calendar calendar = null;

    try {
      DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
      df.setTimeZone(TimeZone.getDefault());

      Date result =  df.parse(datetext);

      calendar = Calendar.getInstance();
      calendar.setTime(result);
      calendar.setTimeZone(TimeZone.getDefault());

    } catch (ParseException pe) {
      pe.printStackTrace();
    }

    return calendar;
  }

  /******************************************************************************/
  public String get_server_url()
  /******************************************************************************/
  {
    prefs = PreferenceManager.getDefaultSharedPreferences(this);
    String url = prefs.getString("url", "http://example.com/");
    return url;
  }

  /******************************************************************************/
  public int login_and_get_cookie(DefaultHttpClient hc) throws LoginException
  /******************************************************************************/
  {
    HttpResponse response;
    HttpEntity entity;
    int ret = 1;
    String err = "nothing";
    String func_prefix = class_prefix + "login_and_get_cookie():";

    try {

      show_cookies(hc);

      HttpPost httppost = new HttpPost(get_server_url() + "login.php");
      httppost.setHeader("Authorization", "Basic " + get_credentials());

      List <NameValuePair> nvps = new ArrayList <NameValuePair>();
      nvps.add(new BasicNameValuePair("login", app_username));
      nvps.add(new BasicNameValuePair("password", app_password));

      httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

      response = hc.execute(httppost);
      entity = response.getEntity();

      StatusLine status = response.getStatusLine();
      Log.i("WC",func_prefix + "get webcalendar loginform: " + status);

      if (status.getStatusCode() != 200) {
        Log.e("WC",func_prefix + "login error: " + status.toString());
        throw new LoginException(status.toString());
      }

      Log.i("WC",func_prefix + "login ok: " + status);

      InputStream inputStream = entity.getContent();
      ByteArrayOutputStream content = new ByteArrayOutputStream();

      // Read response into a buffered stream
      byte[] sBuffer = new byte[512];
      int readBytes = 0;

      while ((readBytes = inputStream.read(sBuffer)) != -1) {
        content.write(sBuffer, 0, readBytes);
      }

      // Return result from buffered stream
      String str_result = new String(content.toByteArray());

      String patternString1 = "<!--ERROR(.*)ERROR-->";

      Pattern pattern = Pattern.compile(patternString1);
      Matcher matcher = pattern.matcher(str_result);

      if (matcher.find()) {
        err = matcher.group(1);
        Log.e("WC",func_prefix + "result from form: error found: " + err);
      } else {
        Log.i("WC",func_prefix + "logged in! ");
        err = "(OK)";
      }

      show_cookies(hc);

      entity_consume_content(entity);

    } catch (HttpHostConnectException e) {
      Log.e("WC",func_prefix + "connection refused: " + e.toString());
      ret = 0;
//      Toast.makeText(this, getResources().getString(R.string.connection_refused), Toast.LENGTH_LONG).show();
    } catch (Exception e) {
      Log.e("WC",func_prefix + "general error" + e.toString());
      ret = 0;
    }

    if (!err.equals("(OK)")) {
      Log.d("WC","login_and_get_cookie: " + err);
      throw new LoginException(err);
    }

    return ret;
  }

  /******************************************************************************/
  public void entity_consume_content(HttpEntity entity)
  /******************************************************************************/
  {
    if (entity != null) {
      try {
        entity.consumeContent();
      } catch (IOException e) {
        Log.e("WC","entity_consume_content error " + e.toString());
      }
    }
  }

  /******************************************************************************/
  public void show_cookies(DefaultHttpClient hc)
  /******************************************************************************/
  {
    Log.d("WC","show_cookies():");
    List<Cookie> cookies = hc.getCookieStore().getCookies();

    if (cookies.isEmpty()) {
      Log.d("WC","# No cookies");
    } else {
      for (int i = 0; i < cookies.size(); i++) {
        Log.d("WC","# " + i + " " + cookies.get(i).toString());
      }
    }
  }

  /******************************************************************************/
  public Calendar timestamp_to_calendar(String time)
  /******************************************************************************/
  {
    long timestampLong = Long.parseLong(time) * 1000;
    Date d = new Date(timestampLong);
    Calendar c = Calendar.getInstance();
    c.setTime(d);
//    c.setTimeInMillis(timestampLong);
    Log.d("WC","timestamp_to_calendar("+time+"): " + c.toString());
    return c;
  }

  /******************************************************************************/
  public String calendar_to_human(Calendar c)
  /******************************************************************************/
  {
    String s = "";
    s += " YEAR " + c.get(Calendar.YEAR);
    s += " MONTH " + c.get(Calendar.MONTH);
    s += " DAY_OF_MONTH " + c.get(Calendar.DAY_OF_MONTH);
    s += " HOUR_OF_DAY " + c.get(Calendar.HOUR_OF_DAY);
    s += " MINUTE " + c.get(Calendar.MINUTE);
//    s += c.toString();

    return s;
  }

  private int get_days_in_month(int month, int year)
  {
    Calendar cal = Calendar.getInstance();  // or pick another time zone if necessary
    cal.set(Calendar.MONTH, month);
    cal.set(Calendar.DAY_OF_MONTH, 1);      // 1st day of month
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.HOUR, 0);
    cal.set(Calendar.MINUTE, 0);
    Date startDate = cal.getTime();

    int nextMonth = (month == Calendar.DECEMBER) ? Calendar.JANUARY : month + 1;
    cal.set(Calendar.MONTH, nextMonth);

    if (month == Calendar.DECEMBER) {
      cal.set(Calendar.YEAR, year + 1);
    }

    Date endDate = cal.getTime();

    // get the number of days by measuring the time between the first of this
    //   month, and the first of next month
    return (int)((endDate.getTime() - startDate.getTime()) / (24 * 60 * 60 * 1000));
  }

  /******************************************************************************/
  String query_classic(long start, long stop, String user)
  /******************************************************************************/
  {
    String query;
    query  = "select * from event where ";
    query += "((starttimestamp>=" + start + " and endtimestamp<=" + stop + ") or ";
    query += "(starttimestamp<" + start + " and endtimestamp>" + stop + ") or ";
    query += "(starttimestamp<=" + start + " and endtimestamp>=" + start + ") or ";
    query += "(starttimestamp<=" + stop + " and endtimestamp>=" + stop + "))";
    query += " and cal_type != 'daily'";
    query += " and (user='" + user +"' or participants like '%" + user + "%') group by id";

    return query;
  }

  /******************************************************************************/
  String query_daily_repeat_fvr_a(long start, long stop, String user)
  /******************************************************************************/
  {
    String query;
    query  = "select * from event where ";
    query += " starttimestamp<=" + start ;
    query += " and timetype = 'A'";
    query += " and cal_type='daily' ";
    query += " and cal_count='null' and cal_end='null'";
    query += " and (user='" + user +"' or participants like '%" + user + "%') group by id";
    return query;
  }

  /******************************************************************************/
  String query_daily_repeat_fvr_t(long start, long stop, String user)
  /******************************************************************************/
  {
    String query;
    query  = "select * from event where ";
    query += " starttimestamp<=" + start ;
    query += " and timetype = 'T'";
    query += " and cal_type='daily' ";
    query += " and cal_count='null' and cal_end='null'";
    query += " and (user='" + user +"' or participants like '%" + user + "%') group by id";
    return query;
  }

  /******************************************************************************/
  String query_daily_repeat_uet_a(long start, long stop, String user)
  /******************************************************************************/
  {
    String query;
    query  = "select * from event where ";
    query += " timetype = 'A'";
    query += " and cal_type='daily' ";
    query += " and cal_count='null'";
    query += " and cal_end > 0 and cal_end!='null'";
    query += " and (user='" + user +"' or participants like '%" + user + "%') group by id";
    return query;
  }

  /******************************************************************************/
  String query_daily_repeat_uet_t(long start, long stop, String user)
  /******************************************************************************/
  {
    String query;
    query  = "select * from event where ";
    query += " timetype = 'T'";
    query += " and cal_type='daily' ";
    query += " and cal_count='null'";
    query += " and cal_end > 0 and cal_end!='null'";
    query += " and (user='" + user +"' or participants like '%" + user + "%') group by id";
    return query;
  }

  /******************************************************************************/
  String query_daily_repeat_not_a(long start, long stop, String user)
  /******************************************************************************/
  {
    String query;
    query  = "select * from event where ";
    query += " timetype = 'A'";
    query += " and cal_type='daily' ";
    query += " and cal_count > 0 and cal_count!='null'";
    query += " and (user='" + user +"' or participants like '%" + user + "%') group by id";
    return query;
  }

  /******************************************************************************/
  String query_daily_repeat_not_t(long start, long stop, String user)
  /******************************************************************************/
  {
    String query;
    query  = "select * from event where ";
    query += " timetype = 'T'";
    query += " and cal_type='daily' ";
    query += " and cal_count > 0 and cal_count!='null'";
    query += " and (user='" + user +"' or participants like '%" + user + "%') group by id";
    return query;
  }

}
