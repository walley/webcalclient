package org.walley.webcalclient2;


import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.tyczj.extendedcalendarview.CalendarProvider;
import com.tyczj.extendedcalendarview.Day;
import com.tyczj.extendedcalendarview.Event;
import com.tyczj.extendedcalendarview.ExtendedCalendarView;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class wcc_ecv extends wcc_activity implements ExtendedCalendarView.OnDayClickListener
{
  ExtendedCalendarView b_calendar;
  TextView user_info;
  String user = "owner";
  ActionBar actionBar;
  String class_prefix = "wcc_ecv ";
  String func_prefix = "func_prefix";

  private static final int RESULT_SETTINGS = 1;

  /******************************************************************************/
  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  /******************************************************************************/
  {
    // Inflate the menu items for use in the action bar
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.calendar, menu);
    return super.onCreateOptionsMenu(menu);
  }

  /******************************************************************************/
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  /******************************************************************************/
  {
    // Handle presses on the action bar items
    switch (item.getItemId()) {
      case R.id.calendar_add:

        Intent i = new Intent();
        i.setClass(wcc_ecv.this,wcc_editevent.class);
        i.putExtra("id",  "-1");
        i.putExtra("user",  user);
        i.putExtra("action",  "new_event");
        startActivity(i);

        return true;

//    case R.id.editevent_settings://  openSettings();      return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  /******************************************************************************/
  @Override
  public void onCreate(Bundle savedInstanceState)
  /******************************************************************************/
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.cal_layout);

    try {
      Bundle bundle = getIntent().getExtras();
      user   = (String) bundle.getString("user");
      Log.d("WC","wcc_ecv.onCreate(): " + user);
    } catch (Exception e) {
      Log.d("WC", "wcc_ecv.onCreate(): empty intent " + e.toString());
      user = "owner";
    }

    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

    if (user.equals("owner")) {
      user = sharedPrefs.getString("app_username", "owner");
    }

    fill_calendar(user);

    create_ui();
  }

  /******************************************************************************/
  public void create_ui()
  /******************************************************************************/
  {
    b_calendar = (ExtendedCalendarView) findViewById(R.id.calendar);
    user_info = (TextView) findViewById(R.id.user_info);

    actionBar = getActionBar();

    // Screen handling while hiding ActionBar icon.
    // actionBar.setDisplayShowHomeEnabled(false);

    // Screen handling while hiding Actionbar title.
    // actionBar.setDisplayShowTitleEnabled(false);

    // Creating ActionBar tabs.
    //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);


    user_info.setText(user);

    b_calendar.setOnDayClickListener(new ExtendedCalendarView.OnDayClickListener() {
      @Override
      public void  onDayClicked(Day d) {
        //Toast.makeText(getApplicationContext(), "day clicked "+d.getDay()+"/"+d.getMonth(), Toast.LENGTH_LONG).show();

        Intent i = new Intent();
        i.setClass(wcc_ecv.this,wcc_day.class);
        i.putExtra("day",   Integer.toString(d.getDay()));
        i.putExtra("month", Integer.toString(d.getMonth() + 1));
        i.putExtra("year",  Integer.toString(d.getYear()));
        i.putExtra("user",  user);
        startActivity(i);

      }
    });

    return;
  }


  /******************************************************************************/
  public long get_start(int day, int month, int year)
  /******************************************************************************/
  {
    func_prefix = class_prefix + "get_start():";

    long start = 0;
    StringBuilder str = new StringBuilder();
    str.append(String.format("%04d", year));
    str.append(String.format("%02d", month));
    str.append(String.format("%02d", day));

    try {
      DateFormat df = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
      df.setTimeZone(TimeZone.getDefault());
      Date result =  df.parse(str.toString());
      start = result.getTime() / 1000;

      Log.d("WC", func_prefix + start + " date result:" + result);
    } catch (ParseException e) {
      e.printStackTrace();
    }

    return start;
  }


  public void add_event(long s_l, long e_l)
  {
    func_prefix = class_prefix + "add_event():";

    ContentValues values = new ContentValues();
    values.put(CalendarProvider.COLOR, Event.COLOR_RED);
    values.put(CalendarProvider.DESCRIPTION, "Some Description");
    values.put(CalendarProvider.LOCATION, "Some location");
    values.put(CalendarProvider.EVENT, "Event name");

    Calendar cal = Calendar.getInstance();
    TimeZone tz = TimeZone.getDefault();
//          TimeZone tz = TimeZone.getTimeZone("UTC");

    int dayJulian = Time.getJulianDay(s_l, TimeUnit.MILLISECONDS.toSeconds(tz.getOffset(cal.getTimeInMillis())));
//    int dayJulian = Time.getJulianDay(s_l, 0);
    values.put(CalendarProvider.START, s_l);
    values.put(CalendarProvider.START_DAY, dayJulian);

    int endDayJulian = Time.getJulianDay(e_l, TimeUnit.MILLISECONDS.toSeconds(tz.getOffset(cal.getTimeInMillis())));
//    int endDayJulian = Time.getJulianDay(e_l, 0);
    values.put(CalendarProvider.END, e_l);
    values.put(CalendarProvider.END_DAY, endDayJulian);

    Uri uri = getContentResolver().insert(CalendarProvider.CONTENT_URI, values);
  }

  /******************************************************************************/
  public void fill_calendar(String user)
  /******************************************************************************/
  {
    String query;

    func_prefix = class_prefix + "fill_calendar("+user+"):";

//    long start = 0;
    long stop = 0;

    SQLiteDatabase mydb;

    mydb = open_db("Calendar");
    do_sql(mydb, "delete from events");
    close_db(mydb);

    query = "select * from event where ";
    query += " cal_type!='daily' ";
    query += " and (user='" + user +"' or participants like '%" + user + "%') group by id";

    Log.d("WC", func_prefix + "query 1: classic: " + query);

    mydb = open_db();

    try {
      Cursor query_result  = mydb.rawQuery(query, null);

      Integer c0 = query_result.getColumnIndex("endtimestamp");
      Integer c1 = query_result.getColumnIndex("starttimestamp");


      if (query_result.moveToFirst()) {
        do {

          String ets = query_result.getString(c0);
          String sts = query_result.getString(c1);

          Log.d("WC", func_prefix + " orig classic start ts: " + sts + " end ts:" + ets);

          long starttimestamp_l = Long.parseLong(sts) * 1000;
          long endtimestamp_l = Long.parseLong(ets) * 1000 - 2 * 3600000 - 1000; //hack

          Log.d("WC", func_prefix + " classic start ts: " + starttimestamp_l + " end ts:" + endtimestamp_l);

          add_event(starttimestamp_l, endtimestamp_l);

        } while (query_result.moveToNext());
      }
    } catch (Exception e) {
      Log.d("WC", func_prefix + "send_event_form error " + e.toString());
      e.printStackTrace();
    }

    int current_month = Calendar.getInstance().get(Calendar.MONTH);
//    Calendar.getInstance().get(Calendar.YEAR);
//get current year and month, get sync+-, iterate over months get_days_in_month(int month, int year)
//    Calendar current_cal = get_calendar_from_datetext_localzone(str + "0000");

    query = query_daily_repeat_not_a(0, 0, user);
    mark_not(query, mydb);

    query = query_daily_repeat_not_t(0, 0, user);
    mark_not(query, mydb);

    query = query_daily_repeat_uet_a(0, 0, user);
    mark_ued(query, mydb);

    query = query_daily_repeat_uet_t(0, 0, user);
    mark_ued(query, mydb);

    query = query_daily_repeat_fvr_a(2114380800, 0, user);
    mark_forever(query, mydb);

    query = query_daily_repeat_fvr_t(2114380800, 0, user);
    mark_forever(query, mydb);

    close_db(mydb);
  }

  /******************************************************************************/
  public void mark_forever(String query, SQLiteDatabase mydb)
  /******************************************************************************/
  {

    func_prefix = class_prefix + "mark_forever():";

    try {
      Cursor query_result  = mydb.rawQuery(query, null);
      String[] header = query_result.getColumnNames();
      Log.d("WC",func_prefix + "q: " + query);

      if (query_result.moveToFirst()) {
        do {

          String start = query_result.getString(query_result.getColumnIndex("start"));
          String cal_end = "21000101235900";

          Log.d("WC", func_prefix + " start: " + start + " cal_end:" + cal_end);

          Calendar start_cal = get_calendar_from_datetext_localzone(start);
          Calendar end_cal = get_calendar_from_datetext_localzone(cal_end);
//        Calendar start_cal = get_calendar_from_datetext(start);
//        Calendar end_cal = get_calendar_from_datetext(cal_end);

          long start_ts = start_cal.getTimeInMillis();
          long end_ts = end_cal.getTimeInMillis();

          Log.d("WC", func_prefix + " start: " + start + " cal_end:" + cal_end);
          Log.d("WC", func_prefix + " start ts: " + start_ts + " end ts:" + end_ts);

          add_event(start_ts, end_ts);

        } while (query_result.moveToNext());
      }
    } catch (Exception e) {
      Log.d("WC", func_prefix + "error " + e.toString());
      e.printStackTrace();
    }
  }


  /******************************************************************************/
  public void mark_ued(String query, SQLiteDatabase mydb)
  /******************************************************************************/
  {
    func_prefix = class_prefix + "mark_ued():";

    try {
      Cursor query_result  = mydb.rawQuery(query, null);
      String[] header = query_result.getColumnNames();
      Log.d("WC",func_prefix + "q: " + query);
      //mark day

      if (query_result.moveToFirst()) {
        do {

          String start = query_result.getString(query_result.getColumnIndex("start"));
          String cal_end = query_result.getString(query_result.getColumnIndex("cal_end")) + "235900";

          Log.d("WC", func_prefix + " start: " + start + " cal_end:" + cal_end);

          Calendar start_cal = get_calendar_from_datetext_localzone(start);
          Calendar end_cal = get_calendar_from_datetext_localzone(cal_end);
//        Calendar start_cal = get_calendar_from_datetext(start);
//        Calendar end_cal = get_calendar_from_datetext(cal_end);

          long start_ts = start_cal.getTimeInMillis();
          long end_ts = end_cal.getTimeInMillis();

          Log.d("WC", func_prefix + " start: " + start + " cal_end:" + cal_end);
          Log.d("WC", func_prefix + " start ts: " + start_ts + " end ts:" + end_ts);

          add_event(start_ts, end_ts);

        } while (query_result.moveToNext());
      }
    } catch (Exception e) {
      Log.d("WC", func_prefix + "error " + e.toString());
      e.printStackTrace();
    }
  }

  /******************************************************************************/
  public void mark_not(String query, SQLiteDatabase mydb)
  /******************************************************************************/
  {
    func_prefix = class_prefix + "mark_not():";

    try {
      Cursor query_result  = mydb.rawQuery(query, null);
      String[] header = query_result.getColumnNames();
      Log.d("WC",func_prefix + "q: " + query);
      //mark day

      if (query_result.moveToFirst()) {
        do {


          long start_ts = query_result.getLong(query_result.getColumnIndex("starttimestamp")) * 1000;
          long end_ts = query_result.getLong(query_result.getColumnIndex("endtimestamp")) * 1000;

          String start = query_result.getString(query_result.getColumnIndex("start"));
          int cal_count = query_result.getInt(query_result.getColumnIndex("cal_count"));
          Calendar start_cal = get_calendar_from_datetext_localzone(start);
          long start_tsx = start_cal.getTimeInMillis();
          long end_tsx = start_tsx + cal_count * 24 * 3600 * 1000 - 3600000;

          Log.d("WC", func_prefix + " start tsx: " + start_tsx + " end tsx:" + end_tsx);
          Log.d("WC", func_prefix + " start ts : " + start_ts  + " end ts :" + end_ts);

          add_event(start_tsx, end_tsx);

        } while (query_result.moveToNext());
      }
    } catch (Exception e) {
      Log.d("WC", func_prefix + "error " + e.toString());
      e.printStackTrace();
    }
  }

  /******************************************************************************/
  public void onDayClicked(Day d)
  /******************************************************************************/
  {
    Toast.makeText(getApplicationContext(), "day clicked", Toast.LENGTH_LONG).show();
  }
}
