package org.walley.webcalclient2;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.widget.Toast;
import android.util.Log;
import java.util.HashMap;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.*;
import android.view.MenuInflater;
import android.view.Menu;
import android.view.MenuItem;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;
import org.json.JSONArray;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/*
  `cal_id`         int(11) NOT NULL default '0',
  `cal_type`       varchar(20) default NULL,
  `cal_end`        int(11) default NULL,
  `cal_frequency`  int(11) default '1',
  `cal_days`       varchar(7) default NULL,
  `cal_endtime`    int(11) default NULL,
  `cal_bymonth`    varchar(50) default NULL,
  `cal_bymonthday` varchar(100) default NULL,
  `cal_byday`      varchar(100) default NULL,
  `cal_bysetpos`   varchar(50) default NULL,
  `cal_byweekno`   varchar(50) default NULL,
  `cal_byyearday`  varchar(50) default NULL,
  `cal_wkst`       char(2) default 'MO',
  `cal_count`      int(11) default NULL,
*/

public class wcc_editevent extends wcc_activity
  implements
  wcc_fragment_details.on_date_set_listener,
  wcc_fragment_details.on_brief_set_listener,
  wcc_fragment_details.on_full_set_listener,
  wcc_fragment_details.on_access_set_listener,
  wcc_fragment_details.on_priority_set_listener,
  wcc_fragment_details.on_timed_set_listener,
  wcc_fragment_details.on_time_set_listener,
  wcc_fragment_details.on_durationh_set_listener,
  wcc_fragment_details.on_durationm_set_listener,
  wcc_fragment_details.on_timedata_set_listener,
  wcc_fragment_participants.on_participants_update_listener,
  wcc_fragment_participants.on_externals_update_listener,
  wcc_fragment_repeat.on_repeat_update_listener
{

  SQLiteDatabase mydb = null;
  SharedPreferences prefs;

  int     cal_id;
  String  cal_type;
  int     cal_end;
  int     cal_frequency;
  String  cal_days;
  int     cal_endtime;
  String  cal_bymonth;
  String  cal_bymonthday;
  String  cal_byday;
  String  cal_bysetpos;
  String  cal_byweekno;
  String  cal_byyearday;
  String  cal_wkst;
  int     cal_count;

//  String brief_desc;
//  String full_desc;
  int day;
  int month;
  int year;
//  int priority;
  String timed;
  int hour;
  int minute;
  String secs = "00";
  int duration_m;
  int duration_h;

  String starttimestamp;
  String location;
  String category_number;
  String label;
  String timestr;
  String access;
  String type;
  String timetype;
  String endtimestamp;
  String id;
  String duration;
  String time;
  String title;
  String category_name;
  String start;
  String description;
  int priority;
  String name;
  String linkid;
  String end;
  String participants;
  String externals;
  String user;
  String action;

  HashMap<String, String> hash = new HashMap<String, String>();
  HashMap<String, String> repeat_hash = new HashMap<String, String>();

  // Declaring our tabs and the corresponding fragments.
  ActionBar.Tab details_tab, participants_tab, repeat_tab, reminders_tab;

  Fragment details_fragment;
  Fragment participants_fragment;
  Fragment repeat_fragment;
  Fragment reminders_fragment;
  Bundle bundle_p;

  /******************************************************************************/
  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  /******************************************************************************/
  {
    // Inflate the menu items for use in the action bar
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.editevent, menu);
    return super.onCreateOptionsMenu(menu);
  }

  /******************************************************************************/
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  /******************************************************************************/
  {
    // Handle presses on the action bar items
    switch (item.getItemId()) {
    case R.id.editevent_save:
      if (Integer.parseInt(id) < 0) {
        create_event();
      } else {
        save_event();
      }

      show_savedstate();
      delete_savedstate();
      show_savedstate();

      finish();

      return true;
//    case R.id.editevent_settings: //openSettings();  return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  /******************************************************************************/
  @Override
  protected void onCreate(Bundle savedInstanceState)
  /******************************************************************************/
  {
    Bundle input_bundle = null;
    super.onCreate(savedInstanceState);
    setContentView(R.layout.wcc_editevent);
    int in_d,in_m,in_y,in_h,in_n;

    details_fragment      = new wcc_fragment_details();
    participants_fragment = new wcc_fragment_participants();
    repeat_fragment       = new wcc_fragment_repeat();
    reminders_fragment    = new wcc_fragment_reminders();

    try {
      input_bundle = getIntent().getExtras();
      id = (String) input_bundle.getString("id");
      action = (String) input_bundle.getString("action");
      user = (String) input_bundle.getString("user");
      Log.d("WC","wcc_editevent onCreate(): " + id + " " + action + " " + user);
    } catch (Exception e) {
      Log.d("WC", "wcc_editevent onCreate(): empty intent " + e.toString());
    }

    Bundle bundle = new Bundle();
    bundle_p = new Bundle();

    Log.d("WC","wcc_editevent onCreate(): action:" + action);
    bundle.putString("action", action);
    switch (action) {
    case "new_event":
      new_event_hash();
      break;
    case "new_event_extra":
      new_event_hash();
      Log.d("WC", "wcc_editevent onCreate(): db hash extra before " + hash.toString());
      try {
        in_d = (int) input_bundle.getInt("d");
        in_m = (int) input_bundle.getInt("m");
        in_y = (int) input_bundle.getInt("y");
        in_h = (int) input_bundle.getInt("h");
        in_n = (int) input_bundle.getInt("n");
        update_time_data(in_y, in_m, in_d, in_h, in_n, 1, 10);
        hash.put("starttimestamp", starttimestamp);
        hash.put("endtimestamp", endtimestamp);
        hash.put("start", start);
        hash.put("end", end);
        Log.d("WC", "wcc_editevent onCreate(): db hash extra after " + hash.toString());
      } catch (Exception e) {
        Log.d("WC", "wcc_editevent onCreate(): no date data " + e.toString());
      }
      break;
    case "edit_event":
      fill_event_hash(Integer.parseInt(id));
      break;
    }

    Log.d("WC", "wcc_editevent onCreate(): db hash " + hash.toString());

    for(Map.Entry<String, String> e : hash.entrySet()) {
      String k = e.getKey();
      String v = e.getValue();
      bundle.putString(k, v);
    }

    details_fragment.setArguments(bundle);
    repeat_fragment.setArguments(bundle);

    set_bundle_participants();

    ActionBar actionBar = getActionBar();

    // Screen handling while hiding ActionBar icon.
    // actionBar.setDisplayShowHomeEnabled(false);

    // Screen handling while hiding Actionbar title.
    // actionBar.setDisplayShowTitleEnabled(false);

    // Creating ActionBar tabs.
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

    details_tab      = actionBar.newTab().setText(getResources().getString(R.string.details));
    participants_tab = actionBar.newTab().setText(getResources().getString(R.string.participants));
    repeat_tab       = actionBar.newTab().setText(getResources().getString(R.string.repeat));
    reminders_tab    = actionBar.newTab().setText(getResources().getString(R.string.reminders));

    details_tab.setTabListener(new TabListener(details_fragment));
    participants_tab.setTabListener(new TabListener(participants_fragment));
    repeat_tab.setTabListener(new TabListener(repeat_fragment));
    reminders_tab.setTabListener(new TabListener(reminders_fragment));

    actionBar.addTab(details_tab);
    actionBar.addTab(participants_tab);
    actionBar.addTab(repeat_tab);
//not yet implemented    actionBar.addTab(reminders_tab);
  }


  /******************************************************************************/
  public void set_bundle_participants()
  /******************************************************************************/
  {
    bundle_p.clear();
    switch (action) {
    case "new_event":
    case "new_event_extra":
      delete_savedstate();
      bundle_p.putString("p", participants); //filed in new_event_hash
      bundle_p.putString("e", "[]");
      bundle_p.putString("action", action);
      action = "edit_event";
      break;
    case "edit_event":
      delete_savedstate();
      bundle_p.putString("p", participants);
      bundle_p.putString("e", externals);
      bundle_p.putString("action", action);
      break;
    }
    participants_fragment.setArguments(bundle_p);
  }


  /******************************************************************************/
  public void on_date_set(int d, int m, int y)
  /******************************************************************************/
  {
    Log.d("WC", "on_date_set() old start params :" + d+"-"+m+"-"+y);

    update_time_data(y, m, d, hour, minute, duration_h, duration_m);
    year = y;
    month = m;
    day = d;

    /*    Log.d("WC", "old start:" + start);
        Log.d("WC", "old start ts:" + starttimestamp);

        year = y;
        month = m;
        day = d;
        start = get_start_string();
        starttimestamp = get_date_ts(start);

        Log.d("WC", "new start:" + start);
        Log.d("WC", "new start ts:" + starttimestamp);*/
  }

  /******************************************************************************/
  public void on_brief_set(String s)
  /******************************************************************************/
  {
    label = s;
  }

  /******************************************************************************/
  public void on_full_set(String s)
  /******************************************************************************/
  {
    description = s;
  }

  /******************************************************************************/
  public void on_access_set(String s)
  /******************************************************************************/
  {
    access = s;
  }

  /******************************************************************************/
  public void on_priority_set(int i)
  /******************************************************************************/
  {
    priority = i;
  }

  /******************************************************************************/
  public void on_timed_set(String s)
  /******************************************************************************/
  {
    timed = s;

    Log.d("WC", "on_time_set():");
    if (s.equals(getResources().getString(R.string.allday))) {
      timestr = "All day event";
      timetype = "A";
    } else {
      timestr = String.format("%02d", hour)+":"+String.format("%02d", minute);
      timetype = "T";
    }
    Log.d("WC", "on_timed_set(): param " + s + " timetype " + timetype);
  }

  /******************************************************************************/
  public void on_time_set(int h, int m)
  /******************************************************************************/
  {
    //values should be in utc already
    hour = h;
    minute = m;

    Log.d("WC", "on_time_set():");
    update_time_data(year, month, day, h, m, duration_h, duration_m);
  }

  /******************************************************************************/
  public void on_durationh_set(int i)
  /******************************************************************************/
  {

    Log.d("WC", "h old end:" + end);
    Log.d("WC", "h old end ts:" + endtimestamp);

    duration_h = i;
    long endtimestamp_l = Long.parseLong(starttimestamp) + (i * 3600) + duration_m * 60;
    endtimestamp = Long.toString(endtimestamp_l);

    end = get_end_string();

    Log.d("WC", "h new end:" + end);
    Log.d("WC", "h new end ts:" + endtimestamp);

    Log.d("WC", "duration:" + duration_h + ":" + duration_m);
    int duration_i = duration_h * 60 + duration_m;
    duration = Integer.toString(duration_i);
  }

  /******************************************************************************/
  public void on_durationm_set(int i)
  /******************************************************************************/
  {

    Log.d("WC", "m old end:" + end);
    Log.d("WC", "m old end ts:" + endtimestamp);

    duration_m = i;
    long endtimestamp_l = Long.parseLong(starttimestamp) + duration_h * 3600 + i * 60;
    endtimestamp = Long.toString(endtimestamp_l);

    end = get_end_string();

    Log.d("WC", "m new end:" + end);
    Log.d("WC", "m new end ts:" + endtimestamp);

    Log.d("WC", "duration:" + duration_h + ":" + duration_m);

    int duration_i = duration_h * 60 + duration_m;
    duration = Integer.toString(duration_i);
  }

  /******************************************************************************/
  public void on_participants_update(ArrayList<String> arr)
  /******************************************************************************/
  {
    JSONArray jsonArray = new JSONArray();
    for (String s: arr) {
      Log.d("WC", "wcc_editevent on_participants_update(): participants:" + s);
      jsonArray.put(s);
    }
    participants = jsonArray.toString();
    Log.d("WC", "wcc_editevent on_participants_update(): participants json:" + participants);
  }

  /******************************************************************************/
  public void on_externals_update(ArrayList<String> arr)
  /******************************************************************************/
  {
    JSONArray jsonArray = new JSONArray();
    for (String s: arr) {
      Log.d("WC", "wcc_editevent on_externals_update(): externals:" + s);
      jsonArray.put(s);
    }
    externals = jsonArray.toString();
    Log.d("WC", "wcc_editevent on_externals_update(): externals json:" + externals);
  }

  /******************************************************************************/
  public int int_parser(String s)
  /******************************************************************************/
  {
    Log.d("WC", "int_parser():(" + s +")");

    if (s == null || s.equals("null") || s.equals("")) {
      return 0;
    } else {
      return Integer.parseInt(s);
    }
  }

  /******************************************************************************/
  public void fill_event_hash(int event_id)
  /******************************************************************************/
  {
    String query;
    String[] header;

    query  = "select * from event where id=" + event_id;
    Log.d("WC","fill_event_hash(): query " + query);

    mydb = open_db();

    try {
      Cursor query_result  = mydb.rawQuery(query, null);
      header = query_result.getColumnNames();

      if(query_result.moveToFirst()) {
        for (int i = 0; i < header.length; i++) {
          String k = new String();
          String v = new String();
          k = header[i];
          v = query_result.getString(i);
          hash.put(k, v);
          Log.d("WC","fill_event_hash put("+k+","+v+")");

          switch (k) {
          case "cal_id":
            cal_id = int_parser(v);
            break;
          case "cal_type":
            cal_type = v;
            break;
          case "cal_end":
            cal_end = int_parser(v);
            break;
          case "cal_frequency":
            cal_frequency = int_parser(v);
            break;
          case "cal_days":
            cal_days = v;
            break;
          case "cal_endtime":
            cal_endtime = int_parser(v);
            break;
          case "cal_bymonth":
            cal_bymonth = v;
            break;
          case "cal_bymonthday":
            cal_bymonthday = v;
            break;
          case "cal_byday":
            cal_byday = v;
            break;
          case "cal_bysetpos":
            cal_bysetpos = v;
            break;
          case "cal_byweekno":
            cal_byweekno = v;
            break;
          case "cal_byyearday":
            cal_byyearday = v;
            break;
          case "cal_wkst":
            cal_wkst = v;
            break;
          case "cal_count":
            cal_count = int_parser(v);
            break;
          case "starttimestamp":
            starttimestamp = v;
            break;
          case "location":
            location = v;
            break;
          case "category_number":
            category_number = v;
            break;
          case "label":
            label = v;
            break;
          case "timestr":
            timestr = v;
            break;
          case "access":
            access = v;
            break;
          case "type":
            type = v;
            break;
          case "timetype":
            timetype = v;
            break;
          case "endtimestamp":
            endtimestamp = v;
            break;
          case "id":
            id = v;
            break;
          case "duration":
            duration = v;
            break;
          case "time":
            time = v;
            break;
          case "title":
            title = v;
            break;
          case "category_name":
            category_name = v;
            break;
          case "start":
            start = v;
            break;
          case "description":
            description = v;
            break;
          case "priority":
            priority = Integer.parseInt(v);
            break;
          case "name":
            name = v;
            break;
          case "linkid":
            linkid = v;
            break;
          case "end":
            end = v;
            break;
          case "participants":
            participants = v;
            break;
          case "externals":
            externals = v;
            break;
          }
        }
      }
    } catch(Exception e) {
      Log.e("WC","fill_event_hash("+day+","+month+","+year+"): " + e.toString());
      e.printStackTrace();
    }

    close_db(mydb);

    try {
      DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
      df.setTimeZone(TimeZone.getTimeZone("UTC"));
      Date result =  df.parse(start);
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(result);
      year = calendar.get(Calendar.YEAR);
      month = calendar.get(Calendar.MONTH) + 1;
      day = calendar.get(Calendar.DAY_OF_MONTH);
      hour = calendar.get(Calendar.HOUR_OF_DAY);
      minute = calendar.get(Calendar.MINUTE);
      Log.e("WC","fill_event_hash("+day+","+month+","+year+"): " + result);
    } catch (ParseException pe) {
      pe.printStackTrace();
    }

  }

  /******************************************************************************/
  public int get_new_id()
  /******************************************************************************/
  {
    int min_id = 0;
    String ret;
    try {
      mydb = open_db();

      String query = "SELECT min(id) FROM event";
      Cursor query_result  = mydb.rawQuery(query, null);

      if(query_result.moveToFirst()) {
        ret = query_result.getString(0);
        min_id = Integer.parseInt(ret);
        if (min_id > 0) {
          min_id = -1;
        }
      }

    } catch(Exception e) {
      Log.d("WC","get_date_events("+day+","+month+","+year+"): " + e.toString());
      e.printStackTrace();
    }

    close_db(mydb);

    Log.d("WC","min_id" + min_id);

    return --min_id;
  }

  /******************************************************************************/
  public void new_event_hash()
  /******************************************************************************/
  {
    String k = new String();
    String v = new String();
    int vi = 0;

    Calendar cal = Calendar.getInstance();
    int ts = (int)(cal.getTimeInMillis()/1000);

    DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
    df.setTimeZone(TimeZone.getTimeZone("UTC"));
    Date date = cal.getTime();
    String start = df.format(date);

    k = "cal_id";
    cal_id = 0;
    v = Integer.toString(cal_id);
    hash.put(k, v);
    k = "cal_type";
    v = cal_type = "none";
    hash.put(k, v);
    k = "cal_end";
    cal_end = 0;
    v = Integer.toString(cal_end);
    hash.put(k, v);
    k = "cal_frequency";
    cal_frequency = 0;
    v = Integer.toString(cal_frequency);
    hash.put(k, v);
    k = "cal_days";
    v = cal_days = "";
    hash.put(k, v);
    k = "cal_endtime";
    cal_endtime = 0;
    v = Integer.toString(cal_endtime);
    hash.put(k, v);
    k = "cal_bymonth";
    v = cal_bymonth = "";
    hash.put(k, v);
    k = "cal_bymonthday";
    v = cal_bymonthday = "";
    hash.put(k, v);
    k = "cal_byday";
    v = cal_byday = "";
    hash.put(k, v);
    k = "cal_bysetpos";
    v = cal_bysetpos = "";
    hash.put(k, v);
    k = "cal_byweekno";
    v = cal_byweekno = "";
    hash.put(k, v);
    k = "cal_byyearday";
    v = cal_byyearday = "";
    hash.put(k, v);
    k = "cal_wkst";
    v = cal_wkst = "";
    hash.put(k, v);
    k = "cal_count";
    cal_count = 0;
    v = Integer.toString(cal_count);
    hash.put(k, v);

    k = "starttimestamp";
    v = starttimestamp = Integer.toString(ts);
    hash.put(k, v);

    k = "location";
    location = "location";
    hash.put(k, v);

    k = "category_number";
    category_number = v = "0";
    hash.put(k, v);

    k = "label";
    label = v = getString(R.string.brief_title);
    hash.put(k, v);

    k = "timestr";
    timestr = v = "00:00";
    hash.put(k, v);

    k = "access";
    access = v = "P";
    hash.put(k, v);

    k = "type";
    type = v = "E";
    hash.put(k, v);

    k = "timetype";
    timetype = v = "T";
    hash.put(k, v);

    k = "endtimestamp";
    endtimestamp = v = Integer.toString(ts);
    hash.put(k, v);

    k = "id";
    id = v = Integer.toString(get_new_id());
    hash.put(k, v);

    k = "duration";
    duration = v = "0";
    hash.put(k, v);

    k = "time";
    time = v = "0";
    hash.put(k, v);

    k = "title";
    title = v = "new title";
    hash.put(k, v);

    k = "category_name";
    category_name = v = "";
    hash.put(k, v);

    k = "start";
    start = v = start;
    hash.put(k, v);

    k = "description";
    description = v = getString(R.string.full_title);
    hash.put(k, v);

    k = "priority";
    v = "5";
    priority = Integer.parseInt(v);
    hash.put(k, v);

    k = "name";
    name = v = "x";
    hash.put(k, v);

    k = "linkid";
    linkid = v = "";
    hash.put(k, v);

    k = "end";
    end = v = start;
    hash.put(k, v);

    k = "externals";
    externals = v = "[]";
    hash.put(k, v);

    prefs = PreferenceManager.getDefaultSharedPreferences(this);

    k = "participants";
//add other user
    String tmp = "[";
    if (user.equals(prefs.getString("app_username", "nonono"))) {
      tmp += "\"" + user + "\"";
    } else {

      tmp += "\""+ prefs.getString("app_username", "nonono") + "\"";
      tmp += ",";
      tmp += "\"" + user + "\"";
    }
    tmp += "]";
    participants = v = tmp;
    hash.put(k, v);

    try {
      df.setTimeZone(TimeZone.getTimeZone("UTC"));
      Date result =  df.parse(start);
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

    Log.d("WC","new_event_hash() hash:" + hash.toString());

  }

  /******************************************************************************/
  String esc(String s)
  /******************************************************************************/
  {
    try {
      return android.database.DatabaseUtils.sqlEscapeString(s);
    } catch (Exception e) {
      Log.d("WC","esc()" + e.toString());
      e.printStackTrace();
    }
    return "";
  }

  /******************************************************************************/
  public void save_event()
  /******************************************************************************/
  {
    SQLiteDatabase mydb = null;
    String z="'x'";
    String query;

    if (cal_type == null || !cal_type.equals("null") && !cal_type.equals("daily") && !cal_type.equals("none")) {
      Toast.makeText(getApplicationContext(), getResources().getString(R.string.repeat_not_implemented), Toast.LENGTH_LONG).show();
      return;
    }

    mydb = open_db();

    query = "update event set ";

//    "dbid",
    query += "starttimestamp=" + starttimestamp +",";
//implement later    query += "location=" + location +",";
    query += "category_number=" + category_number +",";
    query += "label=" + esc(label) +",";
    query += "timestr=" + esc(timestr) +",";
    query += "access='" + access +"',";
    query += "type='" + type +"',";
    query += "endtimestamp=" + endtimestamp +",";
//    query += "id"
    query += "duration=" + duration +",";
    query += "time=" + time +",";
    query += "title=" + esc(title) +",";
    query += "category_name=" + esc(category_name) +",";
    query += "start=" + start +",";
    query += "description=" + esc(description) + ",";
    query += "priority=" + priority +",";
    query += "name=" + esc(name) + ",";
//    query += "linkid",
    query += "end=" + end + ",";
    query += "wcc_changed=" + esc("yes") + ",";
    query += "timetype=" + esc(timetype) + ",";
    query += "participants=" + esc(participants) + ",";
    query += "externals=" + esc(externals) + ",";

    query += "cal_id="        + cal_id + ",";
    query += "cal_type="      + "'" + cal_type + "'" + ",";
    query += "cal_end="       + cal_end + ",";
    query += "cal_frequency=" + cal_frequency + ",";
    query += "cal_days="      + cal_days + ",";
    query += "cal_endtime="   + cal_endtime + ",";
    query += "cal_bymonth="   + cal_bymonth + ",";
    query += "cal_bymonthday="+ cal_bymonthday + ",";
    query += "cal_byday="     + cal_byday + ",";
    query += "cal_bysetpos="  + cal_bysetpos + ",";
    query += "cal_byweekno="  + cal_byweekno + ",";
    query += "cal_byyearday=" + cal_byyearday + ",";
    query += "cal_wkst="      + cal_wkst + ",";
    query += "cal_count="     + cal_count;

    query += " where id=" + id;

    Log.d("WC","save_event() query:" + query);

    do_sql(mydb, query);
    close_db(mydb);

    Toast.makeText(getApplicationContext(), getResources().getString(R.string.event_saved), Toast.LENGTH_LONG).show();
  }

  /******************************************************************************/
  public void create_event()
  /******************************************************************************/
  {
    SQLiteDatabase mydb = null;
    String z="'x'";
    String query;

    mydb = open_db();

    query = "insert into event(dbid,";

    query += "starttimestamp,";
    query += "category_number,";
    query += "label,";
    query += "timestr,";
    query += "access,";
    query += "type,";
    query += "endtimestamp,";
    query += "id,";
    query += "duration,";
    query += "time,";
    query += "title,";
    query += "category_name,";
    query += "start,";
    query += "description,";
    query += "priority,";
    query += "name,";
    query += "timetype,";
    query += "participants,";
    query += "externals,";
    query += "end";

    query += ") values (null, ";

    query += starttimestamp +",";
    query += category_number +",";
    query += esc(label) +",";
    query += esc(timestr) +",";
    query += esc(access) + ",";
    query += esc(type) +",";
    query += endtimestamp +",";
    query += id +",";
    query += duration +",";
    query += time +",";
    query += esc(title) +",";
    query += esc(category_name) +",";
    query += start +",";
    query += esc(description) + ",";
    query += priority +",";
    query += esc(name) + ",";
    query += esc(timetype) + ",";
    query += esc(participants) + ",";
    query += esc(externals) + ",";
    query += end;

    query += ")";

    Toast.makeText(getApplicationContext(), getResources().getString(R.string.create_new_event), Toast.LENGTH_LONG).show();
    Log.d("WC","query:" + query);

    do_sql(mydb, query);
    close_db(mydb);
    finish();
  }

  /******************************************************************************/
  String get_start_string(int y, int m, int d, int h, int mm)
  /******************************************************************************/
  {
    return y+m+d+h+mm+secs;
  }

  /******************************************************************************/
  String get_start_string(int y, int m, int d)
  /******************************************************************************/
  {
    return y+m+d+hour+minute+secs;
  }

  /******************************************************************************/
  String get_start_string()
  /******************************************************************************/
  {
    StringBuilder tmp = new StringBuilder();
    tmp.append(String.format("%04d", year));
    tmp.append(String.format("%02d", month));
    tmp.append(String.format("%02d", day));
    tmp.append(String.format("%02d", hour));
    tmp.append(String.format("%02d", minute));
    tmp.append(secs);

    try {
      DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
//      df.setTimeZone(TimeZone.getDefault());
      df.setTimeZone(TimeZone.getTimeZone("UTC"));
      Date result =  df.parse(tmp.toString());
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(result);

      calendar.setTimeZone(TimeZone.getTimeZone("UTC"));

      year = calendar.get(Calendar.YEAR);
      month = calendar.get(Calendar.MONTH) + 1;
      day = calendar.get(Calendar.DAY_OF_MONTH);
      hour = calendar.get(Calendar.HOUR_OF_DAY);
      minute = calendar.get(Calendar.MINUTE);
    } catch (ParseException pe) {
      pe.printStackTrace();
    }

    StringBuilder tmp2 = new StringBuilder();
    tmp2.append(String.format("%04d", year));
    tmp2.append(String.format("%02d", month));
    tmp2.append(String.format("%02d", day));
    tmp2.append(String.format("%02d", hour));
    tmp2.append(String.format("%02d", minute));
    tmp2.append(secs);

    return tmp2.toString();

  }

  /******************************************************************************/
  String get_end_string()
  /******************************************************************************/
  {

    long endtimestamp_l = Long.parseLong(endtimestamp);

    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
    calendar.setTimeInMillis(endtimestamp_l * 1000);

    int e_year =   calendar.get(Calendar.YEAR);
    int e_month =  calendar.get(Calendar.MONTH) + 1;
    int e_day =    calendar.get(Calendar.DAY_OF_MONTH);
    int e_hour =   calendar.get(Calendar.HOUR_OF_DAY);
    int e_minute = calendar.get(Calendar.MINUTE);

    Log.d("WC", "get_end_string calendar" + calendar.toString());

    StringBuilder tmp = new StringBuilder();
    tmp.append(String.format("%04d", e_year));
    tmp.append(String.format("%02d", e_month));
    tmp.append(String.format("%02d", e_day));
    tmp.append(String.format("%02d", e_hour));
    tmp.append(String.format("%02d", e_minute));
    tmp.append(secs);

    return tmp.toString();
  }

  /******************************************************************************/
  String get_date_ts(String datestr)
  /******************************************************************************/
  {
    long ts_long = 0;
    String ts = null;

    try {
      DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
      df.setTimeZone(TimeZone.getTimeZone("UTC"));
      Date result =  df.parse(datestr);
      ts_long = result.getTime() / 1000;
      ts = Long.toString(ts_long);

      Log.d("WC", "get_date_ts(" + datestr + ") date result:" + result);
    } catch (ParseException e) {
      e.printStackTrace();
    }

    return ts;
  }

  /******************************************************************************/
  public void update_time_data(int y, int m, int d, int h, int min, int dh, int dm)
  /******************************************************************************/
  {

    Log.d("WC", "all start params :" + d+"/"+m+"/"+y +" "+ h+":"+min +" ends in" +dh+":"+dm);

    Log.d("WC", "all old start:" + start);
    Log.d("WC", "all old start ts:" + starttimestamp);
    Log.d("WC", "all old end:" + end);
    Log.d("WC", "all old end ts:" + endtimestamp);

    year = y;
    month = m;
    day = d;
    hour = h;
    minute = min;
    duration_h = dh;
    duration_m = dm;

    start = get_start_string();
    starttimestamp = get_date_ts(start);

    long endtimestamp_l = Long.parseLong(starttimestamp) + (duration_h * 3600) + duration_m * 60;
    endtimestamp = Long.toString(endtimestamp_l);
    end = get_end_string();

    int duration_i = duration_h * 60 + duration_m;
    duration = Integer.toString(duration_i);

    Log.d("WC", "all new start:" + start);
    Log.d("WC", "all new start ts:" + starttimestamp);
    Log.d("WC", "all new end:" + end);
    Log.d("WC", "all new end ts:" + endtimestamp);

    Log.d("WC", "duration:" + duration_h + ":" + duration_m + "=" + duration);

  }

//  HashMap<String, String> repeat_hash = new HashMap<String, String>();

  /******************************************************************************/
  public void on_repeat_update(HashMap<String, String> repeat_hash)
  /******************************************************************************/
  {
    cal_id         = int_parser(repeat_hash.get("cal_id"));
    cal_type       = repeat_hash.get("cal_type");
    cal_end        = int_parser(repeat_hash.get("cal_end"));
    cal_frequency  = int_parser(repeat_hash.get("cal_frequency"));
    cal_days       = repeat_hash.get("cal_days");
    cal_endtime    = int_parser(repeat_hash.get("cal_endtime"));
    cal_bymonth    = repeat_hash.get("cal_bymonth");
    cal_bymonthday = repeat_hash.get("cal_bymonthday");
    cal_byday      = repeat_hash.get("cal_byday");
    cal_bysetpos   = repeat_hash.get("cal_bysetpos");
    cal_byweekno   = repeat_hash.get("cal_byweekno");
    cal_byyearday  = repeat_hash.get("cal_byyearday");
    cal_wkst       = repeat_hash.get("cal_wkst");
    cal_count      = int_parser(repeat_hash.get("cal_count"));

  }

  /******************************************************************************/
  public void show_savedstate()
  /******************************************************************************/
  {
    SharedPreferences saved_state = getSharedPreferences("savedstate", 0);

    String x = (saved_state.getBoolean("saved", false))?"true":"false";
    x += ",";
    x += saved_state.getString("p", "[]");
    x += ",";
    x += saved_state.getString("e", "[]");
    x += ".";

    Log.d("WC", "show_savedstate(): " + x);
  }

  /******************************************************************************/
  public void delete_savedstate()
  /******************************************************************************/
  {
    SharedPreferences preferences = getSharedPreferences("savedstate", 0);
    SharedPreferences.Editor editor = preferences.edit();

    editor.putBoolean("saved", false);

    editor.clear();
    editor.commit();
  }


}
