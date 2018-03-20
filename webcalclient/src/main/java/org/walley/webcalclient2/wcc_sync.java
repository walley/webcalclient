package org.walley.webcalclient2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import de.greenrobot.event.EventBus;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.Runnable;
import java.text.DateFormat;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import javax.net.ssl.SSLPeerUnverifiedException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.StatusLine;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class wcc_sync extends wcc_activity
{
  LinearLayout Linear;
  SQLiteDatabase mydb;
  SQLiteDatabase mydb2;
  private static String DBNAME = "cal.db";
  private static String TABLE = "calendar";
  Context context;
  SharedPreferences prefs;
  TextView sync_tv;
  int mts;
  int number_of_users;
  int number_of_views;
  int users_progress;
  boolean global_error = false;

  Runnable mRunnable;
  Handler mHandler;
  Runnable runnable_owner;
  Handler handler_owner;

  DefaultHttpClient httpclient;

  String class_prefix = "wcc_sync ";
  String func_prefix = "func_prefix";

  private String[] arr_event = {
    "dbid",
    "starttimestamp",
    "location",
    "category_number",
    "label",
    "timestr",
    "access",
    "type",
    "endtimestamp",
    "id",
    "duration",
    "time",
    "title",
    "category_name",
    "start",
    "description",
    "priority",
    "name",
    "linkid",
    "end",
    "user",
    "participants",
    "externals"
  };

  private String sql_create_event = ""
                                    + "create table event"
                                    + "("
                                    + "dbid INTEGER PRIMARY KEY,"
                                    + "starttimestamp integer,"
                                    + "location text,"
                                    + "category_number integer,"
                                    + "label text,"
                                    + "timestr ext,"
                                    + "access character,"
                                    + "type character,"
                                    + "timetype character,"
                                    + "endtimestamp integer,"
                                    + "id integer,"
                                    + "duration integer,"
                                    + "time integer,"
                                    + "title text,"
                                    + "category_name text ,"
                                    + "start integer,"
                                    + "description text,"
                                    + "priority integer,"
                                    + "name text,"
                                    + "linkid text,"
                                    + "end integer,"
                                    + "user integer,"
                                    + "wcc_changed integer,"
                                    + "participants text,"
                                    + "externals text,"
                                    + "cal_id text,"
                                    + "cal_type text,"
                                    + "cal_end text,"
                                    + "cal_frequency text,"
                                    + "cal_days text,"
                                    + "cal_endtime text,"
                                    + "cal_bymonth text,"
                                    + "cal_bymonthday text,"
                                    + "cal_byday text,"
                                    + "cal_bysetpos text,"
                                    + "cal_byweekno text,"
                                    + "cal_byyearday text,"
                                    + "cal_wkst text,"
                                    + "cal_count text"
                                    + ");";

  private String sql_create_users = ""
                                    + "create table users"
                                    + "("
                                    + "dbid INTEGER PRIMARY KEY AUTOINCREMENT,"
                                    + "login text,"
                                    + "name text"
                                    + ");";

  private String sql_create_views = ""
                                    + "create table views"
                                    + "("
                                    + "dbid INTEGER PRIMARY KEY AUTOINCREMENT,"
                                    + "name text,"
                                    + "url text,"
                                    + "typ text,"
                                    + "users text,"
                                    + "id text);";

  HashMap<String, is_user_done> user_test = new HashMap<String, is_user_done>();
  is_user_done owner_test = new is_user_done();
  public boolean end_has_been_reached = false;

  String result_tables = "unknown";
  String result_users_calendars = "unknown";
  String result_own_calendar = "unknown";
  String result_users = "unknown";
  String result_views = "unknown";

  ProgressBar pb_users_calendars;
  ProgressBar pb_tables;
  ProgressBar pb_my_calendar;
  ProgressBar pb_views;
  ProgressBar pb_user_list;
  CheckBox cb_tables;
  CheckBox cb_users_calendars;
  CheckBox cb_my_calendar;
  CheckBox cb_views;
  CheckBox cb_user_list;
  WakeLock wake_lock;

  /******************************************************************************/
  @Override public void onStart()
  /******************************************************************************/
  {
    super.onStart();

    EventBus.getDefault().register(this);

    number_of_users = 0;
    number_of_views = 0;
    users_progress = 0;

    if (is_net_available()) {
      checkin_db();
      checkout_db();
    } else {
      Toast.makeText(context, getResources().getString(R.string.no_inet), Toast.LENGTH_LONG).show();
      finish();
    }
  }

  /******************************************************************************/
  @Override public void onStop()
  /******************************************************************************/
  {
    EventBus.getDefault().unregister(this);
    super.onStop();
  }


  /******************************************************************************/
  @Override public void onCreate(Bundle savedInstanceState)
  /******************************************************************************/
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.sync);
    context = this;

    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
    wake_lock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"wcc_sync_tag");
    wake_lock.acquire(10*60*1000L /*10 minutes*/);

    prefs = PreferenceManager.getDefaultSharedPreferences(this);
    String months_to_sync = prefs.getString("monthstosync", "1");
    mts = Integer.parseInt(months_to_sync);

    create_ui();
  }

  /******************************************************************************/
  public void onEventMainThread(MessageEvent event)
  /******************************************************************************/
  {
    Log.i("WC", "onEventMainThread(): " + event.message);

    switch (event.message) {
    case "done_user_list":
      Log.i("WC", "onEventMainThread(): pb_users_calendars max :" + number_of_users);
      pb_users_calendars.setMax(number_of_users);
      cb_user_list.setChecked(true);
      sync_user_calendars();
      break;
    case "user_done":
      users_progress++;
      Log.i("WC", "onEventMainThread(): pb_users_calendars users_progress/max :" + users_progress + "/" + number_of_users);
      pb_users_calendars.setProgress(users_progress);
      break;
    case "users_calendars_done":
      cb_users_calendars.setChecked(true);
      break;
      case "error_views":
        cb_views.setChecked(true);
        cb_views.setEnabled(false);
      break;
      case "done_views":
        cb_views.setChecked(true);
        break;
      case "parsed_view":
        pb_views.incrementProgressBy(1);
        break;
      case "parsed_user":
      pb_user_list.incrementProgressBy(1);
      break;
    case "set_view":
      Log.i("WC", "onEventMainThread(): set_view param_i :" + event.param_i);
      pb_views.setMax(event.param_i);
      break;
    case "set_user":
      Log.i("WC", "onEventMainThread(): set_user param_i :" + event.param_i);
      pb_user_list.setMax(event.param_i);
      break;
    case "done_tables":
      cb_tables.setChecked(true);
      break;
    case "sql_table_step":
      pb_tables.incrementProgressBy(1);
      break;
    case "done_owner_month":
      pb_my_calendar.incrementProgressBy(1);//setProgress(users_progress);
      break;
    case "error_owner":
      result_own_calendar = "error";
      Toast.makeText(context, getResources().getString(R.string.sync_error), Toast.LENGTH_LONG).show();
      httpclient.getConnectionManager().shutdown();
      wake_lock.release();
      break;
    case "done_owner":
      cb_my_calendar.setChecked(true);
      result_own_calendar = "ok";
      new obtain_list_of_users().execute("");  //onpostexecute starts sync process for each user
      break;
    case "done_everything":
      httpclient.getConnectionManager().shutdown();
      wake_lock.release();
      Toast.makeText(context, getResources().getString(R.string.sync_done), Toast.LENGTH_LONG).show();
      sync_result();
      break;
    default:
      break;
    }
  }

  /******************************************************************************/
  public void  sync_result()
  /******************************************************************************/
  {
    sync_tv.append("result_tables:" + result_tables + "\n");
    sync_tv.append("result_users:" + result_users + "\n");
    sync_tv.append("result_own_calendar:" + result_own_calendar + "\n");
    sync_tv.append("result_views:" + result_views + "\n");
    sync_tv.append("result_users_calendars:" + result_users_calendars + "\n");

    Log.i("WC","result_tables:" + result_tables + "\n");
    Log.i("WC","result_users:" + result_users + "\n");
    Log.i("WC","result_own_calendar:" + result_own_calendar + "\n");
    Log.i("WC","result_views:" + result_views + "\n");
    Log.i("WC","result_users_calendars:" + result_users_calendars + "\n");

  }

  /******************************************************************************/
  public void create_ui()
  /******************************************************************************/
  {
    pb_users_calendars = (ProgressBar) findViewById(R.id.pb_users_calendars);
    pb_tables = (ProgressBar) findViewById(R.id.pb_tables);
    pb_my_calendar = (ProgressBar) findViewById(R.id.pb_my_calendar);
    pb_views = (ProgressBar) findViewById(R.id.pb_views);
    pb_user_list = (ProgressBar) findViewById(R.id.pb_user_list);

    pb_tables.setMax(6);
    pb_my_calendar.setMax(mts * 2 + 1);

    cb_tables          = (CheckBox) findViewById(R.id.cb_tables          );
    cb_users_calendars = (CheckBox) findViewById(R.id.cb_users_calendars );
    cb_my_calendar     = (CheckBox) findViewById(R.id.cb_my_calendar     );
    cb_views           = (CheckBox) findViewById(R.id.cb_views           );
    cb_user_list       = (CheckBox) findViewById(R.id.cb_user_list);

    cb_tables.setClickable(false);
    cb_users_calendars.setClickable(false);
    cb_my_calendar.setClickable(false);
    cb_views.setClickable(false);
    cb_user_list.setClickable(false);

  }

  /******************************************************************************/
  private boolean is_net_available()
  /******************************************************************************/
  {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo ni = cm.getActiveNetworkInfo();
    return ni != null && ni.isConnected();
  }

  /******************************************************************************/
  public void checkout_db()
  /******************************************************************************/
  {
    Calendar calendar;
    String owner = prefs.getString("app_username", "owner");

    String func_prefix = class_prefix + "checkout_db():";
    Log.i("WC", func_prefix + "*** BEGIN SYNCING. ***");

    sync_tv = (TextView) findViewById(R.id.sync_output);
    sync_tv.setText("Debug view - Sync:\n");

    sync_tv.append("tabulky ...\n");

    drop_table();
    create_table();

    sync_tv.append("tabulky vytvoreny ...\n");

    HttpParams params = new BasicHttpParams();
    params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
    httpclient = new DefaultHttpClient(params);

    new obtain_list_of_views().execute("");

    sync_owner();
//after "done_owner" message obtain_list_of_users is executed

  }

  /******************************************************************************/
  public void sync_owner()
  /******************************************************************************/
  {
    Calendar calendar;
    String owner = prefs.getString("app_username", "owner");

//sync owner
    calendar = Calendar.getInstance();
    int syear = calendar.get(Calendar.YEAR);
    int smonth = calendar.get(Calendar.MONTH) + 1;

    for (int i = 0; i < 12; i++) {
      owner_test.months_array[i] = true;
    }

//sync now
    owner_test.months_array[smonth - 1] = false;
    new obtain_data().execute(Integer.toString(smonth), Integer.toString(syear), owner, "owner");

//sync before
    calendar = Calendar.getInstance();
    for (int i = 0; i < mts; i++) {
      calendar.add(Calendar.MONTH, -1);
      smonth = calendar.get(Calendar.MONTH) + 1;
      syear = calendar.get(Calendar.YEAR);
      owner_test.months_array[smonth - 1] = false;
      new obtain_data().execute(Integer.toString(smonth), Integer.toString(syear), owner, "owner");
    }

//sync after
    calendar = Calendar.getInstance();
    for (int i = 0; i < mts; i++) {
      calendar.add(Calendar.MONTH, 1);
      smonth = calendar.get(Calendar.MONTH) + 1;
      syear = calendar.get(Calendar.YEAR);
      owner_test.months_array[smonth - 1] = false;
      new obtain_data().execute(Integer.toString(smonth), Integer.toString(syear), owner, "owner");
    }
//wait for all asynctasks to end

    handler_owner = new Handler();
    runnable_owner = new Runnable() {
      @Override
      public void run() {

        end_has_been_reached = false;

        Log.i("WC", "sync_owner(): checking for end");

        boolean x = true;

        String s = "owner: ";

        for (int i = 0; i < 12; i++) {
          s +="[" + i + "]" + owner_test.months_array[i];
          if (!owner_test.months_array[i]) {
            x = false;
          }
        }

        Log.i("WC", "sync_owner(): " + s);

        if (x) {
          end_has_been_reached = true;
        }

        if (end_has_been_reached || global_error) {
          sync_tv.append("owner sync done\n");
          EventBus.getDefault().post(new MessageEvent("done_owner"));
        } else {
          handler_owner.postDelayed(runnable_owner, 1000);
        }
      }
    };

    handler_owner.postDelayed(runnable_owner, 1000);
  }

  /******************************************************************************/
  public void sync_user_calendars()
  /******************************************************************************/
  {
    ArrayList<String> list = new ArrayList<String>();
    Calendar calendar;

    sync_tv.append("synchronizace uzivatelu ...\n");
    user_test.clear();

    mydb = open_db();
    String q = "select login from users";
    Cursor c = mydb.rawQuery("select login from users", null);
    if(c.moveToFirst()) {
      do {
        String l = c.getString(0);
        list.add(l);
        user_test.put(l, new is_user_done());
      } while(c.moveToNext());
    }
    c.close();
    close_db(mydb);

    for (String s: list) {
      sync_tv.append("start " + s + " ...\n");

      calendar = Calendar.getInstance();
      int syear = calendar.get(Calendar.YEAR);
      int smonth = calendar.get(Calendar.MONTH) + 1;

      /*sync now*/
      new obtain_data().execute(Integer.toString(smonth), Integer.toString(syear), s, "now");
      user_test.get(s).months_array[smonth - 1] = false;

//sync before
      calendar = Calendar.getInstance();
      for (int i = 0; i < mts; i++) {
        calendar.add(Calendar.MONTH, -1);
        smonth = calendar.get(Calendar.MONTH) + 1;
        syear = calendar.get(Calendar.YEAR);
        user_test.get(s).months_array[smonth - 1] = false;
        new obtain_data().execute(Integer.toString(smonth), Integer.toString(syear), s, "before");
      }

//sync after
      calendar = Calendar.getInstance();
      for (int i = 0; i < mts; i++) {
        calendar.add(Calendar.MONTH, 1);
        smonth = calendar.get(Calendar.MONTH) + 1;
        syear = calendar.get(Calendar.YEAR);
        user_test.get(s).months_array[smonth - 1] = false;
        new obtain_data().execute(Integer.toString(smonth), Integer.toString(syear), s, "after");
      }

    }
    sync_tv.append("synchronizace uzivatelu odstartovana...\n");

//wait for all asynctasks to end

    mHandler = new Handler();
    mRunnable = new Runnable() {
      @Override
      public void run() {

        end_has_been_reached = false;

        Log.i("WC", "sync_user_calendars(): checking for end");

        Set<String> keys = user_test.keySet();

        // Loop over String keys.
        boolean x = true;
        for (String key : keys) {
          is_user_done d = user_test.get(key);
          Log.i("WC", "sync_user_calendars(): user " + key + " n/b/a: " + d.now +"/"+ d.before +"/"+ d.after);
          Log.i("WC", "sync_user_calendars(): user " + key + " months: " + d.get_state() + "counted" + d.counted);

          for (int i = 0; i < 12; i++) {
            if (!d.months_array[i]) {
              x = false;
            }
          }

          if (!d.now || !d.before || !d.after) {
            x = false;
          }

          /*          if (d.now && d.before && d.after && !d.counted) {
                      d.counted = true;
                      EventBus.getDefault().post(new MessageEvent("user_done"));
                    }
          */

          if (x && !d.counted) {
            d.counted = true;
            EventBus.getDefault().post(new MessageEvent("user_done"));
          }

        }

        if (x) {
          end_has_been_reached = true;
        }

        Log.i("WC", "sync_user_calendars():x :" + x);

        if (end_has_been_reached) {
          sync_tv.append("synchronizace uzivatelu ukoncena...\n");
          Toast.makeText(context, "konec zmackni zpet", Toast.LENGTH_LONG).show();
          EventBus.getDefault().post(new MessageEvent("users_calendars_done"));
          EventBus.getDefault().post(new MessageEvent("done_everything"));
          sync_result();
        } else {

          mHandler.postDelayed(mRunnable, 1000);
        }
      }
    };

    mHandler.postDelayed(mRunnable, 1000);
  }

  /******************************************************************************/
  public void parse_entries(String json, String user)
  /******************************************************************************/
  {
    String TABLE = "event";
    String query;

    HashMap<String, String> hashMap = new HashMap<String, String>();

    try {

      JSONArray arr = new JSONArray(json);
      for (int i = 0; i < arr.length(); i++) {
        JSONObject entries = arr.getJSONObject(i);
        String can_add = entries.getString("can_add");
        String entry  = entries.optString("entry");

        if (!entry.equals("null")) {
          JSONArray ea = new JSONArray(entry);
          for (int j = 0; j < ea.length(); j++) {
            JSONObject eo = ea.getJSONObject(j);

            //System.out.println(eo.toString());

            JSONObject cal_o = eo.getJSONObject("repeat");
            System.out.println(cal_o.toString());
            Iterator<?> keys = cal_o.keys();
            while (keys.hasNext()) {
              String key = (String)keys.next();
              String value = cal_o.getString(key);

              System.out.println("key: " + key + " value: " + value);

              hashMap.put(key, value);
            }

            for (int k = 0; k < arr_event.length; k++) {
              if (!arr_event[k].equals("dbid") && !arr_event[k].equals("user")) {
                hashMap.put(arr_event[k], eo.getString(arr_event[k]));
              }
            }

            hashMap.put("user", user);

            Iterator<String> keySetIterator = hashMap.keySet().iterator();

            query = "insert into " + TABLE;
            String insert_columns = "";
            String values = "";

            while(keySetIterator.hasNext()) {
              String key = keySetIterator.next();
              //System.out.println("key: " + key + " value: " + hashMap.get(key));

              if (insert_columns.equals("")) {
                insert_columns = key;
              } else {
                insert_columns += "," + key;
              }

              String escaped_value = android.database.DatabaseUtils.sqlEscapeString(hashMap.get(key));

              if (values.equals("")) {
                values = escaped_value;
              } else {
                values += "," + escaped_value;
              }
            }
            String additional_columns = ",wcc_changed,timetype";
            String additional_values = ",'no', ";

            /*all day event test*/
            if (hashMap.get("timestr").equals("All day event")) {
              additional_values += "'A'";
            } else {
              additional_values += "'T'";
            }

            query += "(" + insert_columns + additional_columns + ") values (" + values + additional_values + ")";
            Log.i("WC", "parse_entries() query:" + query);
            mydb2.execSQL(query);
          }
        }
      }

    } catch (Exception e) {
      Log.e("WC","parse_entries error:" + e.toString());
      e.printStackTrace(System.out);
    }
  }


  /******************************************************************************/
  public void sync_month(String json_str_param, String user)
  /******************************************************************************/
  {
    JSONObject jObject = null;
    Iterator<?> keys = null;

    try {
      jObject = new JSONObject(json_str_param);
      keys = jObject.keys();
    } catch (Exception e) {
      Log.e("WC","error 1" + e.toString());
    }

    if (keys == null) {
      Log.i("WC","sync_month():key is null");
      return;
    }

    while (keys.hasNext()) {
      String key = (String)keys.next();
      Log.i("WC","key: " + key);

      if (key.equals("week")) {
        try {
          JSONArray week = jObject.getJSONArray("week");

          for (int i = 0; i < week.length(); i++) {
            JSONObject weekobj = week.getJSONObject(i);
            String weekstr     = weekobj.getString("weekstr");
            String week_link   = weekobj.getString("week_link");
            String week_number = weekobj.getString("week number");
            String entries     = weekobj.getString("entries");

            parse_entries(entries, user);
          }
        } catch (Exception e) {
          Log.e("WC","error 2" + e.toString());
        }
      }
    }
  }

  // THIS FUNCTION SETS COLOR AND PADDING FOR THE TEXTVIEWS
  /******************************************************************************/
  public void setColor(TextView t)
  /******************************************************************************/
  {
    t.setTextColor(Color.BLACK);
    t.setPadding(20, 5, 0, 5);
    t.setTextSize(1, 15);
  }

  // CREATE TABLE IF NOT EXISTS
  /******************************************************************************/
  public void create_table()
  /******************************************************************************/
  {
    try {
      mydb = openOrCreateDatabase(DBNAME, Context.MODE_PRIVATE, null);

      Log.i("WC","create_table:" + sql_create_event);
      mydb.execSQL(sql_create_event);
      EventBus.getDefault().post(new MessageEvent("sql_table_step"));

      Log.i("WC","create_table:" + sql_create_users);
      mydb.execSQL(sql_create_users);
      EventBus.getDefault().post(new MessageEvent("sql_table_step"));

      Log.i("WC","create_views:" + sql_create_views);
      mydb.execSQL(sql_create_views);
      EventBus.getDefault().post(new MessageEvent("sql_table_step"));

      mydb.close();
    } catch(Exception e) {
      EventBus.getDefault().post(new MessageEvent("table_error"));
      Log.e("WC","Error in creating table");
    }

    EventBus.getDefault().post(new MessageEvent("done_tables"));

  }

  /******************************************************************************/
  public void drop_table()
  /******************************************************************************/
  {
    try {
      mydb = openOrCreateDatabase(DBNAME, Context.MODE_PRIVATE,null);
      mydb.execSQL("drop table event");
      EventBus.getDefault().post(new MessageEvent("sql_table_step"));
      mydb.execSQL("drop table users");
      EventBus.getDefault().post(new MessageEvent("sql_table_step"));
      mydb.execSQL("drop table views");
      EventBus.getDefault().post(new MessageEvent("sql_table_step"));
      mydb.close();
    } catch(Exception e) {
      EventBus.getDefault().post(new MessageEvent("table_error"));
      Log.e("WC", "Error encountered while dropping.");
    }
  }


  /******************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
  private class obtain_data extends AsyncTask<String, String, String>
  /******************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
  {
    String ret_json = "";
    private String login_result;
    private int month;
    private int year;
    private String when;
    private String user;
    private String str_result = "x";
    HttpResponse response = null;

    String class_prefix = "wcc_sync-obtain_data";
    String func_prefix = "func_prefix";

    /******************************************************************************/
    private void fetch_data(String url) throws HttpException
    /******************************************************************************/
    {
      show_cookies(httpclient);

      HttpEntity entity = null;
      try {
        HttpPost httpost2 = new HttpPost(url);
        httpost2.setHeader("Authorization", "Basic " + get_credentials());
        response = httpclient.execute(httpost2);
        entity = response.getEntity();
        InputStream is = entity.getContent();
        str_result = IOUtils.toString(is);
        Log.i("WC","obtain_data:  response:\n " + response.getStatusLine() + "\n  string: \n" + str_result);

/*        if (entity != null) {
          try {
            entity.consumeContent();
          } catch (IOException e) {
            Log.e("WC","obtain_data:get_url(): error entity consume " + e.toString());
          }
        }
*/
        entity_consume_content(entity);

      } catch (Exception e) {
        Log.e("WC","obtain_data: fetch_data(): error: " + e.toString());
        str_result = "err";
        throw new HttpException("Cannot obtain_data.fetch_data(" + url  + ")");
      }
      show_cookies(httpclient);

    }

    /******************************************************************************/
    public String get_data() throws LoginException, ApiException
    /******************************************************************************/
    {
      String func_prefix = class_prefix + " get_data():";
      String s_year = Integer.toString(year);
      String s_month = Integer.toString(month);
      String get_params = "?year=" + s_year + "&month=" + s_month;
      String get_param_user = "&user=" + user;
      String url;

      ArrayList<String> list = new ArrayList<String>();

      set_credentials();

      Log.i("WC",func_prefix + " ****** START ******");

      url = get_server_url() + "mobile_month.php" + get_params;
      Log.i("WC",func_prefix + "url: " + url);
      if (!user.equals("owner")) {
        url += get_param_user;
      }

      Log.i("WC",func_prefix +"*** Step 1.");
      try {
        fetch_data(url);
      } catch (HttpException e) {
        Log.e("WC",func_prefix + "fetch_data() did not succeded, wrong url? " + str_result);
        throw new ApiException(str_result);
      }

      int code = response.getStatusLine().getStatusCode();

      if (code == 401) {
        //401 Unauthorized
        Log.i("WC",func_prefix + " 401, not logged in, no or bad intranet password");
        login_result = "401, not logged in, no or bad intranet password";
        //FIXME WAY TO RETURN ERRORS
        show_cookies(httpclient);
        throw new LoginException("Bad pw.");
      } else if (code == 200) {
        //200 OK
        Log.i("WC",func_prefix + "200, OK");
        login_result = "ok";

        if (str_result.contains("error check login")) {
          Log.i("WC",func_prefix + "we need to login ");
          try {
            login_and_get_cookie(httpclient);
          } catch (LoginException e) {
            String estr =  e.toString();
            Log.i("WC",func_prefix + "200, logged in" + estr);
            login_result = estr;
          }

          try {
            fetch_data(url);
          } catch (HttpException e) {
            Log.e("WC",func_prefix + "fetch_data() did not succeded, wrong url?");
            return str_result;
          }

        } else {
          //we are loged in, return result
          Log.i("WC",func_prefix + "*** Step 3.");
          return str_result;
        }
        show_cookies(httpclient);
      }
      Log.i("WC",func_prefix + "*** Step 4.");
      return str_result;
    }

    /******************************************************************************/
    protected void onPreExecute()
    /******************************************************************************/
    {
      Log.i("WC",class_prefix + "*** Preexecute");
    }

    /******************************************************************************/
    protected String doInBackground(String... params)
    /******************************************************************************/
    {
      func_prefix = class_prefix + "doInBackground():";

      String s = null;
      month = Integer.parseInt(params[0]);
      year = Integer.parseInt(params[1]);
      user = params[2];
      when = params[3];
      try {
        s = get_data();
      } catch (LoginException e) {
        Log.i("WC", func_prefix + "bad pw " + e.toString());
        login_result = "bad pw";
      } catch (ApiException e) {
        Log.i("WC", func_prefix + "wrong api call " + e.toString());
        login_result = "bad url";
      } catch (Exception e) {
        Log.i("WC", func_prefix + "error" + e.toString());
        login_result = "error";
      }

      return s;
    }

    /******************************************************************************/
    protected void onPostExecute(String result)
    /******************************************************************************/
    {
      Log.i("WC",class_prefix + "onPostExecute(): user/month/result:" + user + " " + month + " " + login_result);

      if (!login_result.equals("ok")) {
        sync_tv.append("Kalendare pro " + user + " za " + month + " chyba " + login_result + "...\n");
        EventBus.getDefault().post(new MessageEvent("error_owner"));
        global_error = true;
        return;
      }

      mydb2 = open_db();
      sync_month(result, user);
      close_db(mydb2);
      sync_tv.append("Kalendare pro " + user + " za " + month + " precteny ...\n");

      if (when.equals("owner")) {
        owner_test.months_array[month - 1] = true;
        EventBus.getDefault().post(new MessageEvent("done_owner_month"));
      } else {
        user_test.get(user).months_array[month - 1] = true;

        switch (when) {
        case "now":
          user_test.get(user).now = true;
          break;
        case "after":
          user_test.get(user).after = true;
          break;
        case "before":
          user_test.get(user).before = true;
          break;
        default:
          break;
        }

        EventBus.getDefault().post(new MessageEvent("done_data"));
      }
    }
  }

  /******************************************************************************/
  void insert_into_users(String login, String name)
  /******************************************************************************/
  {
    String query  = "insert into users values (null,";
    query += android.database.DatabaseUtils.sqlEscapeString(login);
    query += ",";
    query += android.database.DatabaseUtils.sqlEscapeString(name);
    query += ")";

    mydb.execSQL(query);
  }

  /******************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
  private class obtain_list_of_users extends AsyncTask<String, Void, ArrayList<String>>
  /******************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
  {
    ArrayList<String> temparrlist = new ArrayList<String>();
    private String login_result;

    /******************************************************************************/
    public ArrayList<String> get_users()
    /******************************************************************************/
    {

      HttpResponse response = null;
      HttpEntity entity = null;
      DefaultHttpClient httpclient = new DefaultHttpClient();

      String str_result = null;
      ArrayList<String> list = new ArrayList<String>();

      set_credentials();

      String url = get_server_url();

      Log.i("WC","users: get_users(): Step 1.");
      try {
        Log.i("WC","users: get_users(): blah");
        HttpGet httpget = new HttpGet(url + "mobile_select_user.php");
        httpget.setHeader("Authorization", "Basic " + get_credentials());
        response = httpclient.execute(httpget);
        entity = response.getEntity();
        Log.i("WC","users: get_users(): response: " + response.getStatusLine());
      } catch (SSLPeerUnverifiedException e) {
        Log.e("WC","users: get_users(): error: " + e.toString());
        entity_consume_content(entity);
        login_result = "ssl " + e.toString();
        return list;
      } catch (Exception e) {
        Log.e("WC","users: get_users(): error: " + e.toString());
        entity_consume_content(entity);
        login_result = e.toString();
        return list;
      }

      Log.i("WC"," get_users(): Step 2.");

      int code = response.getStatusLine().getStatusCode();

      if (code == 401) {
        //401 Unauthorized
        Log.i("WC"," get_users(): 401, not logged in, no or bad intranet password");
        //prolly bad intranet pwd, return empty list FIXME WAY TO RETURN ERRORS
        if (entity != null) {
          try {
            entity.consumeContent();
          } catch (IOException e) {
            Log.e("WC","error " + e.toString());
          }
        }
        //h.show_cookies(httpclient);
        login_result = getResources().getString(R.string.wrong_password);
        return list;

      } else if (code == 200) {
        //200 OK
        login_result = "ok";

        String str = null;
        try {
          InputStream is = entity.getContent();
          str = IOUtils.toString(is);
        } catch (IOException e) {
          Log.e("WC","get users() step2 error " + e.toString());
        }
        entity_consume_content(entity);

        if (str.contains("error check login")) {
          Log.i("WC"," get_users(): we need to login ");
          try {
            login_and_get_cookie(httpclient);
          } catch (LoginException e) {
            String estr =  e.toString();
            Log.i("WC","get_users(): 200, logged in" + estr);
            login_result = estr;
          }
        }

        show_cookies(httpclient);
      }

      try {
        Log.i("WC","Step 3.");
        url = get_server_url();
        HttpPost httpost2 = new HttpPost(url + "mobile_select_user.php");
        httpost2.setHeader("Authorization", "Basic " + get_credentials());
        response = httpclient.execute(httpost2);
        entity = response.getEntity();

        Log.i("WC","users response: " + response.getStatusLine());

        InputStream inputStream = entity.getContent();
        str_result = IOUtils.toString(inputStream);

        Log.i("WC","get_users() string: " + str_result);


        if (entity != null) {
          entity.consumeContent();
        }

        // When HttpClient instance is no longer needed,
        // shut down the connection manager to ensure
        // immediate deallocation of all system resources
        httpclient.getConnectionManager().shutdown();
      } catch (Exception e) {
        Log.e("WC","get users() step 3. error " + e.toString());
      }

      try {
        JSONArray arr = new JSONArray(str_result.trim());
        EventBus.getDefault().post(new MessageEvent("set_user", arr.length()));

        for (int i = 0; i < arr.length(); i++) {
          JSONObject users =  arr.getJSONObject(i);
          String name = users.optString("user_name");
          String login = users.optString("user_login");

          Log.i("WC","json: " + users.toString());
          Log.i("WC","json: name " + name);
          Log.i("WC","json: login" + login);
          list.add(name + " (" + login + ")");

          mydb = open_db();
          insert_into_users(login, name);
          close_db(mydb);

          EventBus.getDefault().post(new MessageEvent("parsed_user"));
        }

      } catch (Exception e) {
        Log.e("WC","get_users() json parsing error " + e.toString());
      }

// az onpost      EventBus.getDefault().post(new MessageEvent("done_user_list"));
      number_of_users = list.size();
      return list;
    }

    /******************************************************************************/
    protected void onPreExecute()
    /******************************************************************************/
    {
//      sync_tv.append("Ctu uzivatele ...\n");
//      dialog = new ProgressDialog(context);
//      dialog = ProgressDialog.show(context, null, getResources().getString(R.string.pleasewait));
    }

    /******************************************************************************/
    protected ArrayList<String> doInBackground(String... connection)
    /******************************************************************************/
    {
      temparrlist = get_users();
      Log.e("WC","users: doInBackground(): returned");
      if (temparrlist.size() == 0) {
        Log.e("WC","users: doInBackground(): returned empty list");
      }
      return temparrlist;
    }

    /******************************************************************************/
    protected void onPostExecute(ArrayList<String> result)
    /******************************************************************************/
    {
//      diaLog.iismiss();
//      Toast.makeText(getApplicationContext(), "result:" + login_result, Toast.LENGTH_LONG).show();
      Log.i("WC","users final result" + login_result);

      sync_tv.append("Uzivatele precteni ...\n");

      EventBus.getDefault().post(new MessageEvent("done_user_list"));

//      sync_user_calendars();
    }
  }

  /******************************************************************************/
  public void checkin_db()
  /******************************************************************************/
  {
    HashMap<String, String> h;

    String query;
    String[] header;
    String id_s;
    int id;
    String changed;
    String func_prefix = class_prefix + "checkin_db():";

    query  = "select * from event";
    Log.i("WC",func_prefix + "query " + query);

    mydb = open_db();

    try {
      Cursor query_result  = mydb.rawQuery(query, null);
      header = query_result.getColumnNames();
      Log.i("WC",func_prefix + header.toString());

      if(query_result.moveToFirst()) {
        do {
          h = get_event_hash(query_result);
          id_s = h.get("id");
          changed = h.get("wcc_changed");
          id = Integer.parseInt(id_s);

          if (id < 0) {
            // upload new events
            Log.i("WC",func_prefix + "id < 0 : " + id);
            send_form(h, "create");
          }

          if (changed != null && changed.equals("yes")) {
            send_form(h, "update");
          }

        } while(query_result.moveToNext());
      }
    } catch(Exception e) {
      Log.i("WC",func_prefix + e.toString());
      e.printStackTrace();
    }
    close_db(mydb);

  }

  /******************************************************************************/
  public void send_form (HashMap<String, String> db, String mode)
  /******************************************************************************/
  {

    String func_prefix = class_prefix + "send_form(..., "+mode+"):";

    HashMap<String, String> h = new HashMap<String, String>();
    int year = 0;
    int month = 0;
    int day = 0;
    int hour = 0;
    int minute = 0;

    Log.i("WC","send_form db hash: " + db.toString());
    Log.i("WC","send_form mode: " + mode);

    h.put("eType","event");
    h.put("confirm_conflicts","no");

    if (!mode.equals("update")) {
      h.put("entry_changed","yes");
    }

    if (!mode.equals("create")) {
      h.put("cal_id", db.get("id"));
    }

    h.put("name", db.get("label"));
    h.put("description", db.get("description"));
    h.put("access", db.get("access"));
    h.put("priority", db.get("priority"));
    h.put("location", db.get("location"));
    h.put("timetype", db.get("timetype"));

    long start_ts = Long.parseLong(db.get("starttimestamp"), 10);
    long end_ts = Long.parseLong(db.get("endtimestamp"), 10);

    long duration_t = end_ts - start_ts;
    long duration_h = duration_t / 3600;
    long duration_m = (duration_t - (duration_h*3600)) / 60;

    try {
      DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
      df.setTimeZone(TimeZone.getTimeZone("UTC"));
//      df.setTimeZone(TimeZone.getDefault());
      Date result = df.parse(db.get("start"));

      Calendar calendar = Calendar.getInstance();
      calendar.setTime(result);
      year = calendar.get(Calendar.YEAR);
      month = calendar.get(Calendar.MONTH) + 1;
      day = calendar.get(Calendar.DAY_OF_MONTH);
      hour = calendar.get(Calendar.HOUR_OF_DAY);
      minute = calendar.get(Calendar.MINUTE);
    } catch (ParseException pe) {
      pe.printStackTrace();
    }

    h.put("duration_h",Long.toString(duration_h));
    h.put("duration_m",Long.toString(duration_m));
    h.put("day", Integer.toString(day));
    h.put("month", Integer.toString(month));
    h.put("year",  Integer.toString(year));
    h.put("entry_hour", Integer.toString(hour));
    h.put("entry_minute", Integer.toString(minute));
    h.put("entry_ampm","0");


    String ext = db.get("externals");
    if (ext == null || ext.equals("null")) {
      ext = "[]";
    }

    try {
      Log.i("WC", "send_form(): externals:>" + ext + "<");
      JSONArray ar1 = new JSONArray(ext);
      String e = "";
      for (int i = 0; i < ar1.length(); i++) {
        e += ar1.getString(i) + "\n";
      }
      h.put("externalparticipants", e);
    } catch (JSONException e) {
      Log.e("WC",func_prefix + "externals json: " + e.toString());
      e.printStackTrace();
    }

    Log.i("WC", func_prefix + "externals: end ******************************");

    String put = h.put("participants", db.get("participants"));
    /*externals are parsed from json in asynctask while sending
    because hash cannot contains same keys needed to send select multiple */

    h.put("rpt_type", db.get("cal_type"));

    Log.i("WC", func_prefix + "fill rpt: **********" + db.get("cal_type"));

    h.put("rpt_until", db.get("cal_end"));
    h.put("rpt_until", db.get("cal_endtime"));
    h.put("byyearday", db.get("cal_byyearday"));
    h.put("rpt_count", db.get("cal_count"));
    h.put("wkst", "MO");
    h.put("byday", db.get("cal_byday"));

    /*
    if ( ! empty ( $rpt_year ) ) {
    $rpt_hour += $rpt_ampm;
    $rpt_until = mktime ( $rpt_hour, $rpt_minute, 0, $rpt_month, $rpt_day, $rpt_year );
    mktime returns timestamp
    }

    $names[] = 'cal_end';
    $values[] = gmdate ( 'Ymd', $rpt_until );

    $names[] = 'cal_endtime';
    $values[] = gmdate ( 'His', $rpt_until );
    */


    DateFormat df_t = new SimpleDateFormat("HHmmss");
    DateFormat df_d = new SimpleDateFormat("yyyyMMdd");
    Date result_d = null;
    Date result_t = null;

    df_t.setTimeZone(TimeZone.getTimeZone("UTC"));
    df_d.setTimeZone(TimeZone.getTimeZone("UTC"));

    try {
      result_d =  df_d.parse(db.get("cal_end"));
      result_t =  df_t.parse(db.get("cal_endtime"));
    } catch (ParseException pe) {
      pe.printStackTrace();
      Log.e("WC", "send_form(): error parsing cal_end or cal_endtime");
    } catch (NullPointerException e) {
      e.printStackTrace();
      Log.e("WC", "send_form(): cal_end or cal_endtime is null");
    }

    DateFormat dfy = new SimpleDateFormat("yyy");
    dfy.setTimeZone(TimeZone.getDefault());
    DateFormat dfm = new SimpleDateFormat("MM");
    dfm.setTimeZone(TimeZone.getDefault());
    DateFormat dfd = new SimpleDateFormat("dd");
    dfd.setTimeZone(TimeZone.getDefault());
    DateFormat dfh = new SimpleDateFormat("HH");
    dfh.setTimeZone(TimeZone.getDefault());
    DateFormat dfmin = new SimpleDateFormat("mm");
    dfmin.setTimeZone(TimeZone.getDefault());

    if (result_d != null) {
      String rpt_year  = dfy.format(result_d).toString();
      String rpt_month = dfm.format(result_d).toString();
      String rpt_day   = dfd.format(result_d).toString();

      h.put("rpt_day", rpt_day);
      h.put("rpt_month", rpt_month);
      h.put("rpt_year", rpt_year);
    }

    if (result_t != null) {
      String rpt_hour   = dfh.format(result_t);
      String rpt_minute = dfmin.format(result_t);

      h.put("rpt_hour", rpt_hour);
      h.put("rpt_minute", rpt_minute);
    }

    h.put("rpt_ampm", "");

//???    h.put("rptmode", "");

    h.put("rpt_end_use", "x"); //should be f u c, but not used

    h.put("rpt_freq", db.get("cal_frequency")); //must be 1 or more

    h.put("weekdays_only", ""); //non empty if checked

    Log.i("WC","send_form hash: " + h.toString());
    Log.i("WC","send_form end");

    new form_execute().execute(h);
  }

  /******************************************************************************/
  public HashMap<String, String> get_event_hash(Cursor query_result)
  /******************************************************************************/
  {
    HashMap<String, String> h = new HashMap<String, String>();

    String[] header = query_result.getColumnNames();

    for (int i = 0; i < query_result.getColumnCount(); i++) {
      String k = new String();
      String v = new String();
      k = header[i];
      v = query_result.getString(i);
      h.put(k, v);
//      Log.i("WC","fill_event_hash("+k+","+v+")");
    }
    return h;
  }

  /******************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
  private class form_execute extends AsyncTask<HashMap<String, String>, String, String>
  /******************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
  {
    ArrayList<String> temparrlist = new ArrayList<String>();
    private ProgressDialog dialog;
    private String login_result;

    HashMap<String, String> form_data = null;

    /******************************************************************************/
    public ArrayList<String> send_form()
    /******************************************************************************/
    {

      HttpResponse response = null;
      HttpEntity entity = null;
      DefaultHttpClient httpclient = new DefaultHttpClient();

      String str = null;
      String str_result = null;
      ArrayList<String> list = new ArrayList<String>();

      set_credentials();
//      wcc_http h = new wcc_http(http_username, http_password, wc_username, wc_password);

      try {
        Log.i("WC"," send_form(): Step 1.");

        String url = get_server_url();

        HttpGet httpget = new HttpGet(url + "edit_entry.php");
        httpget.setHeader("Authorization", "Basic " + get_credentials());
        response = httpclient.execute(httpget);
        entity = response.getEntity();
        Log.i("WC"," send_form(): response: " + response.getStatusLine());
      } catch (Exception e) {
        Log.e("WC"," send_form(): error: " + e.toString());
      }

      Log.i("WC"," send_form(): Step 2.");

      int code = response.getStatusLine().getStatusCode();

      if (code == 401) {
        //401 Unauthorized
        Log.i("WC"," send_form(): 401, not logged in, no or bad intranet password");
        //prolly bad intranet pwd, return empty list FIXME WAY TO RETURN ERRORS
        if (entity != null) {
          try {
            entity.consumeContent();
          } catch (IOException e) {
            Log.e("WC","error " + e.toString());
          }
        }
        show_cookies(httpclient);
        return list;

      } else if (code == 200) {
        //200 OK
        login_result = "ok";

        try {
          str = get_entity_content(entity.getContent());
        } catch (IOException e) {
          Log.e("WC","error " + e.toString());
        }

        entity_consume_content(entity);

        if (str.toString().contains("error check login")) {
          Log.i("WC"," send_form(): we need to login ");
          try {
            login_and_get_cookie(httpclient);
          } catch (LoginException e) {
            String estr =  e.toString();
            Log.i("WC","send_form(): 200, logged in" + estr);
            login_result = estr;
          }
        }

        show_cookies(httpclient);
      }

      try {
        Log.i("WC","Step 3.");

        String url = get_server_url() + "mobile_edit_entry_handler.php";
        HttpPost httppost2 = new HttpPost(url);

        httppost2.setHeader("Authorization", "Basic " + get_credentials());

        List <NameValuePair> nvps = new ArrayList <NameValuePair>();

        Log.i("WC","  step3 form: " + form_data.toString());

        for(Map.Entry<String, String> entry : form_data.entrySet()) {
          String k = entry.getKey();
          String v = entry.getValue();

          if (k.equals("participants")) {
            try {
              JSONArray ar2 = new JSONArray(v);
              for (int i = 0; i < ar2.length(); i++) {
                nvps.add(new BasicNameValuePair("participants[]", ar2.getString(i)));
              }
            } catch (JSONException e) {
              Log.e("WC"," send_form(): error participants: " + e.toString());
            }
          } else {
            nvps.add(new BasicNameValuePair(k, v));
          }
        }

        httppost2.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

        response = httpclient.execute(httppost2);
        entity = response.getEntity();

        Log.i("WC","  step3 response statusline: " + response.getStatusLine());

        try {
          str = get_entity_content(entity.getContent());
        } catch (IOException e) {
          Log.e("WC","error " + e.toString());
        }

        str_result = str.toString();

        System.out.println(" WC form_execute response string: " + str_result);
        Log.i("WC"," WC form_execute response string: " + str_result);

        if (entity != null) {
          entity.consumeContent();
        }

        // When HttpClient instance is no longer needed,
        // shut down the connection manager to ensure
        // immediate deallocation of all system resources
        httpclient.getConnectionManager().shutdown();
      } catch (Exception e) {
        Log.e("WC","error " + e.toString());
      }


      return list;
    }

    /******************************************************************************/
    protected void onPreExecute()
    /******************************************************************************/
    {
      dialog = new ProgressDialog(context);
      dialog = ProgressDialog.show(context, null, getResources().getString(R.string.pleasewait));
    }

    /******************************************************************************/
    protected String doInBackground(HashMap<String, String> ... form)
    /******************************************************************************/
    {
      form_data = form[0];
      temparrlist = send_form();

      EventBus.getDefault().post(new MessageEvent("done_form"));
      return "Yo!";
    }

    /******************************************************************************/
    protected void onPostExecute(String result)
    /******************************************************************************/
    {
      dialog.dismiss();
      Toast.makeText(getApplicationContext(), "result:" + login_result, Toast.LENGTH_LONG).show();
      Log.i("WC","form final result" + login_result);
    }

  }

  /******************************************************************************/
  String get_entity_content(InputStream i)
  /******************************************************************************/
  {
    String s = null;

    try {
      s = IOUtils.toString(i);
    } catch (IOException e) {
      Log.e("WC","get_entity_content() error " + e.toString());
      s = "";
    }

    return s;
  }

  /******************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
  private class obtain_list_of_views extends AsyncTask<String, Void, ArrayList<String>>
  /******************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
  {
    ArrayList<String> temparrlist = new ArrayList<String>();
    private ProgressDialog dialog;
    private String login_result = "ok";
    SQLiteDatabase db = null;
    private String str_result = "x";
    HttpResponse response = null;

    String class_prefix = "wcc_sync.obtain_list_of_views-";
    String func_prefix = "func_prefix";

    /******************************************************************************/
    private void fetch_data(String server_url)
    /******************************************************************************/
    {
      String func_prefix = class_prefix + "fetch_data():";
      HttpEntity entity = null;

      show_cookies(httpclient);

      try {
        HttpGet httpget = new HttpGet(server_url);
        httpget.setHeader("Authorization", "Basic " + get_credentials());
        response = httpclient.execute(httpget);
        entity = response.getEntity();

        InputStream is = entity.getContent();
        str_result = IOUtils.toString(is);

        Log.i("WC",func_prefix + "response statusline:" + response.getStatusLine());
        Log.i("WC",func_prefix + "response string    :" + str_result);
      } catch (SSLPeerUnverifiedException e) {
        //something bad with connection (wifi login dialog) fixme
        login_result = e.toString();
        Log.e("WC",func_prefix + "SSLPeerUnverifiedException: " + e.toString());
      } catch (IllegalStateException e) {
        login_result = e.toString();
        Log.e("WC",func_prefix + "IllegalStateException: " + e.toString());
      } catch (Exception e) {
        Log.e("WC",func_prefix + "Exception: " + e.toString());
        login_result = e.toString();
      } finally {
        entity_consume_content(entity);
      }
    }

    /******************************************************************************/
    public ArrayList<String> get_views() throws LoginException, ApiException
    /******************************************************************************/
    {
      String func_prefix = class_prefix + "get_views():";

      HttpEntity entity = null;

      ArrayList<String> list = new ArrayList<String>();

      set_credentials();
      Log.i("WC",func_prefix + " ****** START ******");

      //check_server_url(get_server_url());

      String server_url = get_server_url() + "mobile_views.php";
      Log.i("WC",func_prefix + "url: " + server_url);

      Log.i("WC",func_prefix + "*** Step 1.");
/*      try {
        HttpGet httpget = new HttpGet(server_url);
        httpget.setHeader("Authorization", "Basic " + get_credentials());
        response = httpclient.execute(httpget);
        entity = response.getEntity();
        Log.i("WC",func_prefix + " response: " + response.getStatusLine());
      } catch (SSLPeerUnverifiedException e) {
        //something bad with connection (wifi login dialog) fixme
        login_result = e.toString();
        Log.e("WC",func_prefix + "SSLPeerUnverifiedException: " + e.toString());
        entity_consume_content(entity);
        return null;
      } catch (IllegalStateException e) {
        login_result = e.toString();
        Log.e("WC",func_prefix + "IllegalStateException: " + e.toString());
        entity_consume_content(entity);
        return null;
      } catch (Exception e) {
        Log.e("WC",func_prefix + "Exception: " + e.toString());
        login_result = e.toString();
        entity_consume_content(entity);
        return null;
      }
*/
      fetch_data(server_url);
      if (!login_result.equals("ok")) {
        throw new LoginException(login_result);
      }

      Log.i("WC",func_prefix + "*** Step 2.");

      int code = response.getStatusLine().getStatusCode();

      if (code == 401) {
        //401 Unauthorized
        Log.i("WC",func_prefix + " 401, not logged in, no or bad intranet password");
        login_result = "401, not logged in, no or bad intranet password";
        //prolly bad intranet pwd
        show_cookies(httpclient);
        throw new LoginException("Bad internet login.");

      } else if (code == 200) {
        //200 OK
        Log.i("WC",func_prefix + " 200, OK");
        login_result = "ok";

        if (str_result.contains("error check login")) {
          Log.i("WC",func_prefix + " we need to login ");
          try {
            login_and_get_cookie(httpclient);
          } catch (LoginException e) {
            String estr =  e.toString();
            Log.i("WC",func_prefix + " 200, logging error" + estr);
            login_result = estr;
          }
          fetch_data(server_url);
          if (!login_result.equals("ok")) {
            throw new LoginException(login_result);
          }

        } else {
          Log.i("WC",func_prefix + " logged in");
        }
        show_cookies(httpclient);
      }


/*      try {
        Log.i("WC",func_prefix + "*** Step 3.");

        HttpPost httpost2 = new HttpPost(get_server_url() + "mobile_views.php");
        httpost2.setHeader("Authorization", "Basic " + get_credentials());
        response = httpclient.execute(httpost2);
        entity = response.getEntity();

        Log.i("WC",func_prefix + "response: " + response.getStatusLine());

        try {
          InputStream is = entity.getContent();
          str_result = IOUtils.toString(is);
        } catch (IOException e) {
          Log.e("WC",func_prefix + "error " + e.toString());
        }

        Log.i("WC",func_prefix + "step #3 result: " + str_result);

        if (entity != null) {
          entity.consumeContent();
        }

        // When HttpClient instance is no longer needed,
        // shut down the connection manager to ensure
        // immediate deallocation of all system resources
        httpclient.getConnectionManager().shutdown();
      } catch (Exception e) {
        Log.e("WC",func_prefix + "step 3. error " + e.toString());
        return null;
      }
*/
      try {

        JSONArray arr = new JSONArray(str_result.trim());

        number_of_views = arr.length();
        EventBus.getDefault().post(new MessageEvent("set_view", number_of_views));

        for (int i = 0; i < arr.length(); i++) {
          JSONObject views;
          views = arr.getJSONObject(i);
          String name   = views.getString("name");
          String url    = views.optString("url");
          String type   = views.optString("type");
          String vusers = views.optString("users");
          String id     = views.optString("id");

          Log.i("WC",func_prefix + "json views: name " + name);
          Log.i("WC",func_prefix + "json views: url" + url);
          Log.i("WC",func_prefix + "json views: typ" + type);
          Log.i("WC",func_prefix + "json views: users" + vusers);
          Log.i("WC",func_prefix + "json views: id" + id);
          list.add(name + " " + type);

          mydb = open_db();
          insert_into_views(name, url, type, id, vusers);
          close_db(mydb);

          EventBus.getDefault().post(new MessageEvent("parsed_view"));
        }

      } catch (Exception e) {
        Log.e("WC",func_prefix + "error parsing view" + e.toString());
        return null;
      }

      return list;
    }

    /******************************************************************************/
    protected void onPreExecute()
    /******************************************************************************/
    {
      dialog = new ProgressDialog(context);
      dialog = ProgressDialog.show(context, getResources().getString(R.string.syncing), getResources().getString(R.string.pleasewait));
      sync_tv.append("Ctu pohledy ...\n");
      db = open_db();
    }

    /******************************************************************************/
    protected ArrayList<String> doInBackground(String... connection)
    /******************************************************************************/
    {
      String func_prefix = class_prefix + "doInBackground():";

      try {
        temparrlist = get_views();
      } catch (LoginException e) {
        Log.i("WC", func_prefix + "bad pw " + e.toString());
        login_result = e.toString();
        result_views = "error (" + login_result + ")";
        return null;
      } catch (ApiException e) {
        Log.i("WC", func_prefix + "wrong api call " + e.toString());
        login_result = "bad url";
        result_views = "error (" + login_result + ")";
        return null;
      } catch (Exception e) {
        Log.i("WC", func_prefix + "error" + e.toString());
        login_result = "error";
        result_views = "error (" + login_result + ")";
        return null;
      }

      if (temparrlist == null) {
        Log.e("WC",func_prefix + "returned null list");
        global_error = true;
        result_views = "error (" + login_result + ")";
        EventBus.getDefault().post(new MessageEvent("error_views"));
        return null;
      }

      if (temparrlist.size() == 0) {
        Log.e("WC",func_prefix + "returned empty list");
      }

      result_views = "ok";

      return temparrlist;
    }

    /******************************************************************************/
    protected void onPostExecute(ArrayList<String> result)
    /******************************************************************************/
    {
      dialog.dismiss();
      Log.i("WC",class_prefix + "onPostExecute: views final result: " + login_result);
      sync_tv.append("Pohledy precteny ... \n");
      close_db(db);

      if (result_views.equals("ok")) {
        EventBus.getDefault().post(new MessageEvent("done_views"));
      } else {
        EventBus.getDefault().post(new MessageEvent("error_views"));
      }
    }

    /******************************************************************************/
    void insert_into_views(String name, String url, String typ, String id, String users)
    /******************************************************************************/
    {
      String query  = "insert into views values (null,";
      query += android.database.DatabaseUtils.sqlEscapeString(name);
      query += ",";
      query += android.database.DatabaseUtils.sqlEscapeString(url);
      query += ",";
      query += android.database.DatabaseUtils.sqlEscapeString(typ);
      query += ",";
      query += android.database.DatabaseUtils.sqlEscapeString(users);
      query += ",";
      query += android.database.DatabaseUtils.sqlEscapeString(id);
      query += ")";

      Log.i("WC","views: insert_into_views():" + query);
      mydb.execSQL(query);

    }
  }

  /******************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
  public class is_user_done
  /******************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
  {
    public boolean now;
    public boolean before;
    public boolean after;
    public boolean counted;

    public boolean[] months_array = new boolean[12];

    /******************************************************************************/
    public is_user_done ()
    /******************************************************************************/
    {
      now = false;
      before = false;
      after = false;
      counted = false;

      for (int i = 0; i < 12; i++) {
        months_array[i] = true;
      }
    }

    /******************************************************************************/
    public String get_state ()
    /******************************************************************************/
    {
      StringBuilder s= new StringBuilder();
      for (int i = 0; i < 12; i++) {
        s.append((months_array[i])?"1":"0");
      }
      return s.toString();
    }

  }

  /******************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
  public class MessageEvent
  /******************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
  {
    public final String message;
    public int param_i;

    /******************************************************************************/
    public MessageEvent(String message)
    /******************************************************************************/
    {
      this.message = message;
    }

    /******************************************************************************/
    public MessageEvent(String message, int i)
    /******************************************************************************/
    {
      this.message = message;
      param_i = i;
    }
  }
}