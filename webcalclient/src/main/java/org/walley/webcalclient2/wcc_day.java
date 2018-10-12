package org.walley.webcalclient2;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class wcc_day extends wcc_activity implements OnClickListener
{
  ActionBar actionBar;
  TableLayout table_layout;
  EditText rowno_et, colno_et;
  Button build_btn;
  String day;
  String month;
  String year;
  String user;
  Context context = this;
  String date;
  TextView tv_date;
  TextView tv_user;
  wcc_u u = new wcc_u();
  SQLiteDatabase mydb = null;
  LinearLayout all_l;
  ArrayList<String> all_day_events = new ArrayList<String>();
  SharedPreferences prefs;

  /******************************************************************************/
  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  /******************************************************************************/
  {
    // Inflate the menu items for use in the action bar
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.day, menu);
    return super.onCreateOptionsMenu(menu);
  }

  /******************************************************************************/
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  /******************************************************************************/
  {
    // Handle presses on the action bar items
    switch (item.getItemId()) {
      case R.id.day_calendar_add:

        Intent i = new Intent();
        i.setClass(wcc_day.this, wcc_editevent.class);
        i.putExtra("id",  "-1");
        i.putExtra("user", user);
        i.putExtra("action", "new_event_extra");
        i.putExtra("d", Integer.parseInt(day));
        i.putExtra("m", Integer.parseInt(month));
        i.putExtra("y", Integer.parseInt(year));
        i.putExtra("h", 1);
        i.putExtra("n", 10);
        startActivity(i);

        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  /******************************************************************************/
  @Override
  protected void onCreate(Bundle savedInstanceState)
  /******************************************************************************/
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.day);

    table_layout = (TableLayout) findViewById(R.id.tableLayout1);
    tv_date = (TextView) findViewById(R.id.date);
    tv_user = (TextView) findViewById(R.id.user);

    try {
      Bundle bundle = getIntent().getExtras();
      day   = (String) bundle.getString("day");
      month = (String) bundle.getString("month");
      year  = (String) bundle.getString("year");
      user  = (String) bundle.getString("user");
      Log.i("WC","wcc_day onCreate(): " + day + " " + month + " " + year);
    } catch (Exception e) {
      Log.e("WC", "wcc_day onCreate(): empty intent " + e.toString());
    }


    tv_date.setText(day + "/" + month + "/" + year);

    create_ui();

  }

  /******************************************************************************/
  public void onClick(View v)
  /******************************************************************************/
  {
    String tag = v.getTag().toString();
    String[] parts = tag.split(":");
    String part1 = parts[0];
    String part2 = parts[1];

    Intent i = new Intent();
    i.setClass(wcc_day.this,wcc_editevent.class);

    if (part1.equals("hodina")) {

      Date d = null;

      String sm = String.format("%02d", Integer.parseInt(month));
      String sd = String.format("%02d", Integer.parseInt(day));
      String sp = String.format("%02d", Integer.parseInt(part2));

      Log.e("WC", "wcc_day onClick" +year+sm+sd+sp +" - "+ part1 + ":" + part2);

      DateFormat df = new SimpleDateFormat("yyyyMMddHH");
      df.setTimeZone(TimeZone.getDefault());

      try {
        d = df.parse(year+sm+sd+sp);
      } catch (ParseException pe) {
        pe.printStackTrace();
      }


      // date in the UTC timezone
      DateFormat dfutc = new SimpleDateFormat("HH");
      dfutc.setTimeZone(TimeZone.getTimeZone("UTC"));

      int utc_hour = Integer.parseInt(dfutc.format(d));

      Log.e("WC", "wcc_day onClick " + d.toString() + " " + utc_hour);


      i.putExtra("id",  "-1");
      i.putExtra("user", user);
      i.putExtra("action", "new_event_extra");
      i.putExtra("d", Integer.parseInt(day));
      i.putExtra("m", Integer.parseInt(month));
      i.putExtra("y", Integer.parseInt(year));
      i.putExtra("h", utc_hour);
      i.putExtra("n", 0);
    } else {
      i.putExtra("id", part1);
      i.putExtra("action", "edit_event");
    }

    startActivity(i);
  }


  /******************************************************************************/
  private void create_ui()
  /******************************************************************************/
  {
    prefs = PreferenceManager.getDefaultSharedPreferences(this);
    int i_d = Integer.parseInt(day);
    int i_m = Integer.parseInt(month);
    int i_y = Integer.parseInt(year);
    String from_h = prefs.getString("workhoursfrom", "0");
    String to_h = prefs.getString("workhoursto", "24");
    int frm;
    int to;

    actionBar = getActionBar();

    tv_date.setText(day + "/" + month + "/" + year);
    tv_user.setText(user);

    frm = Integer.parseInt(from_h);
    to = Integer.parseInt(to_h) + 1;

    BuildTable(2, frm, to, i_d, i_m, i_y);
  }

  /******************************************************************************/
  private View get_cell_at_pos(TableLayout table, int x, int y)
  /******************************************************************************/
  {
    int columns = 2;
    int pos = (y - 1) * columns + x - 1;
    TableRow row = (TableRow) table.getChildAt(pos / columns);
    return row.getChildAt(pos % columns);
  }


  /******************************************************************************/
  void build_table_header()
  /******************************************************************************/
  {
    TableRow row = new TableRow(this);
    row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

    TextView tv = new TextView(this);
    tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
    tv.setBackgroundResource(R.drawable.selector);
    tv.setTextColor(context.getResources().getColor(R.color.day_color_selector));
    tv.setPadding(1, 1, 1, 1);
    tv.setText("  " + getResources().getString(R.string.hour) + "  ");
    row.addView(tv);

    TextView tv2 = new TextView(this);
    tv2.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
    tv2.setBackgroundResource(R.drawable.selector);
    tv2.setTextColor(context.getResources().getColor(R.color.day_color_selector));
    tv2.setPadding(1, 1, 1, 1);
    tv2.setText("  " + getResources().getString(R.string.event) + "  ");
    row.addView(tv2);

    table_layout.addView(row);

    TableRow row2 = new TableRow(this);
    row2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

    TextView tv_all = new TextView(this);
    tv_all.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
    tv_all.setBackgroundResource(R.drawable.selector);
    tv_all.setTextColor(context.getResources().getColor(R.color.day_color_selector));
    tv_all.setPadding(1, 1, 1, 1);
    tv_all.setText("  " + getResources().getString(R.string.all_day) + "  ");
    row2.addView(tv_all);

    all_l = new LinearLayout(this);
    all_l.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    all_l.setOrientation(LinearLayout.VERTICAL);
    row2.addView(all_l);

    table_layout.addView(row2);

  }

  /******************************************************************************/
  public void add_empty_tv(LinearLayout l)
  /******************************************************************************/
  {
    TextView tvtemp = new TextView(this);
    tvtemp.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
    tvtemp.setBackgroundResource(R.drawable.selector);
    tvtemp.setTextColor(context.getResources().getColor(R.color.day_color_selector));
    tvtemp.setPadding(1, 1, 1, 1);
    tvtemp.setText(" ");
    l.addView(tvtemp);
  }

  /******************************************************************************/
  private void BuildTable(int cols, int fromrows, int torows, int i_d, int i_m, int i_y)
  /******************************************************************************/
  {
    ArrayList<String[]> a = null;
    ArrayList<TextView> at = new ArrayList<TextView>();
    String func_prefix = "wcc_day BuildTable(" + cols + " " + fromrows + " " + torows + " " + i_d + " " + i_m + " " + i_y + "):";
    boolean all_day_event;

    build_table_header();

    mydb = open_db();

    if (fromrows > torows) {
      Log.e("WC",func_prefix + "from hour must be lower than to hour");
    }

    for (int i = fromrows; i < torows; i++) {

      Log.i("WC",func_prefix + "row:" + i);

      TableRow row = new TableRow(this);
      row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

      TextView tv = new TextView(this);
      tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
      tv.setBackgroundResource(R.drawable.selector_hour);
      tv.setTextColor(context.getResources().getColor(R.color.day_hour_color_selector));
      tv.setPadding(1, 1, 1, 1);
      tv.setText(Integer.toString(i));
      tv.setTag("hodina" + ":" + i);
      tv.setOnClickListener(this);

      row.addView(tv);

      LinearLayout l = new LinearLayout(this);
      l.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
      l.setOrientation(LinearLayout.VERTICAL);

      a = get_hour_events(i_d, i_m, i_y, i);
      Log.i("WC",func_prefix + "adding events:" + a.size());

      for (String s[] : a) {

        all_day_event = !s[8].equals("T");

        TextView tvtemp = new TextView(this);

        tvtemp.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        tvtemp.setBackgroundResource(R.drawable.selector);
        tvtemp.setTextColor(context.getResources().getColor(R.color.day_color_selector));
        tvtemp.setPadding(1, 1, 1, 1);

        String desc = "";

        if (s[3].length() > 25) {
          desc = s[3].substring(0,25);
        } else {
          desc = s[3];
        }

        Calendar from_cal = get_calendar_from_datetext(s[6]);
        Calendar to_cal = get_calendar_from_datetext(s[7]);

        Log.d("WC",func_prefix + "adding: fromcal:" + from_cal);
        Log.d("WC",func_prefix + "adding: tocal:" + to_cal);
        Log.d("WC",func_prefix + "start stop: start:" + s[6] + " end:" + s[7]);

        String event_s = "";

        if (!all_day_event) {
          event_s += "["+ String.format("%02d", from_cal.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", from_cal.get(Calendar.MINUTE)) + "]";
        }

        event_s += "\n";
        event_s += desc + "\n";

        if (!all_day_event) {
          event_s += "Konec " + to_cal.get(Calendar.DAY_OF_MONTH) + "/" + (to_cal.get(Calendar.MONTH)+1) + "/" + to_cal.get(Calendar.YEAR) ;
          event_s += " v "+ to_cal.get(Calendar.HOUR_OF_DAY) + ":" + to_cal.get(Calendar.MINUTE);
        }

        if (!user.equals(s[9])) {
//          event_s += "\n jsi participant";
        } else {
//          event_s += "\ntest"+user;
        }

        tvtemp.setText(event_s);

        tvtemp.setTag(s[0] + ":" + i);
        tvtemp.setOnClickListener(this);

        if (s[8].equals("T")) {
          l.addView(tvtemp);
        } else {
          if (!all_day_events.contains(s[0])) {
            all_l.addView(tvtemp);
            all_day_events.add(s[0]); //id
          }

          add_empty_tv(l);
        }
      }

      if (a.isEmpty()) {
        add_empty_tv(l);
      }

      row.addView(l);

      table_layout.addView(row);
    }

    if (all_day_events.isEmpty()) {
      add_empty_tv(all_l);
    }

    close_db(mydb);
  }

  /******************************************************************************/
  public ArrayList<String[]> get_hour_events(int day, int month, int year, int hour)
  /******************************************************************************/
  {
//    long timestamp = u.get_timestamp_from_date(day, month, year);
    int ci;
    String query;
    ArrayList arr_list = new ArrayList<String>();
    String func_prefix = "wcc_day get_hour_events("+day+","+month+","+year+","+hour+"):";
    Log.d("WC", func_prefix + "******** start ********");

    long start = 0;// = timestamp + ((hour-offset) * 3600);
    long stop = 0;// =  timestamp + ((hour + 1) * 3600) - 1;

    StringBuilder str = new StringBuilder();
    str.append(String.format("%04d", year));
    str.append(String.format("%02d", month));
    str.append(String.format("%02d", day));
    str.append(String.format("%02d", hour));

    try {
      DateFormat df = new SimpleDateFormat("yyyyMMddHH", Locale.ENGLISH);
      df.setTimeZone(TimeZone.getDefault());
      Date result =  df.parse(str.toString());
      start = result.getTime() / 1000;

      Log.d("WC", func_prefix + start + " date result:" + result);
    } catch (ParseException e) {
      e.printStackTrace();
    }

    //calendar of hour we are filling
    Calendar current_cal = get_calendar_from_datetext_localzone(str + "0000");

    stop = start + 3599;

    Log.d("WC", func_prefix + "start, stop:" + start + ", " + stop);

//    query  = "select * from event where ";
//    query += "((starttimestamp>=" + start + " and endtimestamp<=" + stop + ") or ";
//    query += "(starttimestamp<" + start + " and endtimestamp>" + stop + ") or ";
//    query += "(starttimestamp<=" + start + " and endtimestamp>=" + start + ") or ";
//    query += "(starttimestamp<=" + stop + " and endtimestamp>=" + stop + "))";
//    query += " and cal_type != 'daily'";
//    query += " and (user='" + user +"' or participants like '%" + user + "%') group by id";

    query  =  query_classic(start, stop, user);

    Log.d("WC", func_prefix + "classic query:" + query);

    try {
      Cursor query_result  = mydb.rawQuery(query, null);
      String[] header = query_result.getColumnNames();

      Log.d("WC", func_prefix + "classic event result count: " + query_result.getCount());

      if (query_result.moveToFirst()) {
        do {
          Log.d("WC", func_prefix + "classic add " + query_result.getCount());
          arr_list.add(get_event_fields(query_result));
        } while (query_result.moveToNext());
      }
    } catch (Exception e) {
      Log.d("WC", func_prefix + "error " + e.toString());
      e.printStackTrace();
    }

    Log.d("WC", func_prefix + "classic list size " + arr_list.size());


////////////////
//daily repeat//
////////////////

////////////////////////////////////////////////////////////////////////////////

//forever allday
    query = query_daily_repeat_fvr_a(start, stop, user);

    try {
      Cursor query_result  = mydb.rawQuery(query, null);
      String[] header = query_result.getColumnNames();
      Log.d("WC",func_prefix + "daily forever a q:" + query);
      arr_list.addAll(get_daily_forever_all_day(query, query_result));
    } catch (Exception e) {
      Log.d("WC", func_prefix + "error " + e.toString());
      e.printStackTrace();
    }

//forever timed
    query = query_daily_repeat_fvr_t(start, stop, user);

    try {
      Cursor query_result  = mydb.rawQuery(query, null);
      String[] header = query_result.getColumnNames();
      Log.d("WC",func_prefix + "daily forever t q:" + query);
      arr_list.addAll(get_daily_forever_timed(query, query_result, current_cal));
    } catch (Exception e) {
      Log.d("WC", func_prefix + "error " + e.toString());
      e.printStackTrace();
    }

////////////////////////////////////////////////////////////////////////////////

//number of times allday
//    query  = "select * from event where ";
//    query += " timetype = 'A'";
//    query += " and cal_type='daily' ";
//    query += " and cal_count > 0 and cal_count!='null'";
//    query += " and (user='" + user +"' or participants like '%" + user + "%') group by id";

    query = query_daily_repeat_not_a(start, stop, user);

    try {
      Cursor query_result  = mydb.rawQuery(query, null);
      String[] header = query_result.getColumnNames();
      Log.d("WC",func_prefix + "not allday q:" + query);
      arr_list.addAll(get_daily_all_day_count(query, query_result, current_cal));
    } catch (Exception e) {
      Log.d("WC", func_prefix + "error " + e.toString());
      e.printStackTrace();
    }

//number of times timed
//    query  = "select * from event where ";
//    query += " timetype = 'T'";
//    query += " and cal_type='daily' ";
//    query += " and cal_count > 0 and cal_count!='null'";
//    query += " and (user='" + user +"' or participants like '%" + user + "%') group by id";

    query = query_daily_repeat_not_t(start, stop, user);

    try {
      Cursor query_result  = mydb.rawQuery(query, null);
      String[] header = query_result.getColumnNames();
      Log.d("WC",func_prefix + "not timed q:" + query);
      arr_list.addAll(get_daily_timed_count(query, query_result, current_cal));
    } catch (Exception e) {
      Log.d("WC", func_prefix + "error " + e.toString());
      e.printStackTrace();
    }

////////////////////////////////////////////////////////////////////////////////

//use end date allday event
    query = query_daily_repeat_uet_a(start, stop, user);

    try {
      Cursor query_result  = mydb.rawQuery(query, null);
      String[] header = query_result.getColumnNames();
      Log.d("WC",func_prefix + "use_end_date all_day q:" + query);
      arr_list.addAll(get_daily_ued_all_day_count(query, query_result, current_cal));
    } catch (Exception e) {
      Log.d("WC", func_prefix + "error " + e.toString());
      e.printStackTrace();
    }

//useenddate timed event
    query = query_daily_repeat_uet_t(start, stop, user);

    try {
      Cursor query_result  = mydb.rawQuery(query, null);
      String[] header = query_result.getColumnNames();
      Log.d("WC",func_prefix + "use_end_date timed q:" + query);
      arr_list.addAll(get_daily_ued_timed_count(query, query_result, current_cal));
    } catch (Exception e) {
      Log.d("WC", func_prefix + "error " + e.toString());
      e.printStackTrace();
    }

////////////////////////////////////////////////////////////////////////////////

    Log.d("WC", func_prefix + "returning " + arr_list.size());

    return arr_list;
  }

  /******************************************************************************/
  public ArrayList<String[]> get_daily_forever_all_day(String query, Cursor query_result)
  /******************************************************************************/
  {

    int ci;
    ArrayList arr_list = new ArrayList<String>();
    String func_prefix = "wcc_day get_daily_forever_all_day():";
    Log.d("WC",func_prefix + "count:" + query_result.getCount());

    if (query_result.moveToFirst()) {
      do {
        arr_list.add(get_event_fields(query_result));
      } while (query_result.moveToNext());
    }

    return arr_list;
  }

  /******************************************************************************/
  public ArrayList<String[]> get_daily_forever_timed(String query, Cursor query_result, Calendar current_cal)
  /******************************************************************************/
  {
    //tbd
    ArrayList arr_list = new ArrayList<String>();
    String func_prefix = "wcc_day get_daily_forever_timed():";
    Log.d("WC",func_prefix + "count:" + query_result.getCount());

    if (query_result.moveToFirst()) {
      do {

        String startts = query_result.getString(query_result.getColumnIndex("starttimestamp"));
        String endts = query_result.getString(query_result.getColumnIndex("endtimestamp"));
        int start_hour = timestamp_to_calendar(startts).get(Calendar.HOUR_OF_DAY);
        int end_hour = timestamp_to_calendar(endts).get(Calendar.HOUR_OF_DAY);

        Log.d("WC",func_prefix + "ts:"+startts+" "+endts+" sh, eh - c:" + start_hour +","+ end_hour+" - " + current_cal.get(Calendar.HOUR_OF_DAY));

        if (
          current_cal.get(Calendar.HOUR_OF_DAY) >= start_hour && current_cal.get(Calendar.HOUR_OF_DAY) < end_hour
          ||
          current_cal.get(Calendar.HOUR_OF_DAY) == start_hour && current_cal.get(Calendar.HOUR_OF_DAY) == end_hour
        ) {
          Log.d("WC",func_prefix + "!!! HIT");
          arr_list.add(get_event_fields(query_result));
        } else {
          Log.d("WC",func_prefix + "!!! MISS");
        }

      } while (query_result.moveToNext());
    }

    return arr_list;
  }

  /******************************************************************************/
  public ArrayList<String[]> get_daily_all_day_count(String query, Cursor query_result, Calendar current_cal)
  /******************************************************************************/
  {

    int ci;
    int count;
    ArrayList arr_list = new ArrayList<String>();
    String func_prefix = "wcc_day get_daily_all_day_count():";
    Log.d("WC",func_prefix + "count:" + query_result.getCount());

    if (query_result.moveToFirst()) {
      do {
        count = Integer.parseInt(query_result.getString(query_result.getColumnIndex("cal_count")));
        Log.d("WC", func_prefix + query_result.getCount());

        String start = query_result.getString(query_result.getColumnIndex("start"));
        String end = query_result.getString(query_result.getColumnIndex("start"));

        Calendar start_cal = get_calendar_from_datetext(start);
        Calendar end_cal = get_calendar_from_datetext(start);
        end_cal.add(Calendar.DATE, count);

        Log.d("WC",func_prefix + "s,e:" + start_cal.toString() + end_cal.toString());

        if ((current_cal.getTimeInMillis() >= start_cal.getTimeInMillis()) && (current_cal.getTimeInMillis() <= end_cal.getTimeInMillis())) {
          arr_list.add(get_event_fields(query_result));
        }
      } while (query_result.moveToNext());
    }

    return arr_list;
  }

  /******************************************************************************/
  public ArrayList<String[]> get_daily_timed_count(String query, Cursor query_result, Calendar current_cal)
  /******************************************************************************/
  {

    int ci;
    int count;
    ArrayList arr_list = new ArrayList<String>();
    String func_prefix = "wcc_day get_daily_timed_count():";
    Log.d("WC",func_prefix + "testing for:" + calendar_to_human(current_cal));
    Log.d("WC",func_prefix + " query result getCount:" + query_result.getCount());

    if (query_result.moveToFirst()) {
      do {
        count = Integer.parseInt(query_result.getString(query_result.getColumnIndex("cal_count")));
        Log.d("WC", func_prefix + " count days:" + count);

        String start = query_result.getString(query_result.getColumnIndex("start"));
        String end = query_result.getString(query_result.getColumnIndex("start"));
        Calendar start_cal = get_calendar_from_datetext(start);
        Calendar end_cal = get_calendar_from_datetext(start);
        end_cal.add(Calendar.DATE, count - 1); //one repeat means no repeat

        String startts = query_result.getString(query_result.getColumnIndex("starttimestamp"));
        String endts = query_result.getString(query_result.getColumnIndex("endtimestamp"));
        int start_hour = timestamp_to_calendar(startts).get(Calendar.HOUR_OF_DAY);
        int end_hour = timestamp_to_calendar(endts).get(Calendar.HOUR_OF_DAY);

        Log.d("WC",func_prefix + "!!! s" + calendar_to_human(start_cal));
        Log.d("WC",func_prefix + "!!! e" + calendar_to_human(end_cal));

        Log.d("WC",func_prefix + "ts:"+startts+" "+endts+" sh, eh - c:" + start_hour +","+ end_hour+" - " + current_cal.get(Calendar.HOUR_OF_DAY));

        if (
          (current_cal.getTimeInMillis() >= start_cal.getTimeInMillis()) && (current_cal.getTimeInMillis() <= end_cal.getTimeInMillis())
          &&
          (current_cal.get(Calendar.HOUR_OF_DAY) >= start_hour && current_cal.get(Calendar.HOUR_OF_DAY) < end_hour)
        ) {
          Log.d("WC",func_prefix + "!!! HIT");
          arr_list.add(get_event_fields(query_result));
        } else {
          Log.d("WC",func_prefix + "!!! MISS");
        }
      } while (query_result.moveToNext());
    }

    Log.d("WC", func_prefix + "returning " + arr_list.size());
    return arr_list;
  }


  /******************************************************************************/
  public ArrayList<String[]> get_daily_ued_all_day_count(String query, Cursor query_result, Calendar current_cal)
  /******************************************************************************/
  {
    int ci;
    int count;
    ArrayList arr_list = new ArrayList<String>();
    String func_prefix = "wcc_day get_daily_ued_all_day_count():";
    Log.d("WC",func_prefix + "count:" + query_result.getCount());

    if (query_result.moveToFirst()) {
      do {

        String start = query_result.getString(query_result.getColumnIndex("start"));
        String cal_end = query_result.getString(query_result.getColumnIndex("cal_end"));

        Log.d("WC", func_prefix + " start: " + start + " cal_end:" + cal_end);

        Calendar start_cal = get_calendar_from_datetext(start);
        Calendar end_cal = get_calendar_from_datetext(cal_end + "240000");

        if ((current_cal.getTimeInMillis() >= start_cal.getTimeInMillis()) && (current_cal.getTimeInMillis() <= end_cal.getTimeInMillis())) {
          arr_list.add(get_event_fields(query_result));
        }
      } while (query_result.moveToNext());
    }

    return arr_list;
  }

  /******************************************************************************/
  public ArrayList<String[]> get_daily_ued_timed_count(String query, Cursor query_result, Calendar current_cal)
  /******************************************************************************/
  {
    int ci;
    int count;
    ArrayList arr_list = new ArrayList<String>();
    String func_prefix = "wcc_day get_daily_ued_all_day_count():";
    Log.d("WC",func_prefix + "count:" + query_result.getCount());

    if (query_result.moveToFirst()) {
      do {

        String start = query_result.getString(query_result.getColumnIndex("start"));
        String cal_end = query_result.getString(query_result.getColumnIndex("cal_end"));

        Log.d("WC", func_prefix + " start: " + start + " cal_end:" + cal_end);

        Calendar start_cal = get_calendar_from_datetext(start);
        Calendar end_cal = get_calendar_from_datetext(cal_end + "240000");

        String startts = query_result.getString(query_result.getColumnIndex("starttimestamp"));
        String endts = query_result.getString(query_result.getColumnIndex("endtimestamp"));
        int start_hour = timestamp_to_calendar(startts).get(Calendar.HOUR_OF_DAY);
        int end_hour = timestamp_to_calendar(endts).get(Calendar.HOUR_OF_DAY);

        if (
          (current_cal.getTimeInMillis() >= start_cal.getTimeInMillis()) && (current_cal.getTimeInMillis() <= end_cal.getTimeInMillis())
          &&
          (current_cal.get(Calendar.HOUR_OF_DAY) >= start_hour && current_cal.get(Calendar.HOUR_OF_DAY) < end_hour)
        ) {
          arr_list.add(get_event_fields(query_result));
        }
      } while (query_result.moveToNext());
    }

    return arr_list;
  }

  /******************************************************************************/
  public String[] get_event_fields(Cursor query_result)
  /******************************************************************************/
  {
    String func_prefix = "wcc_day get_event_fields():";
    int ci;
    String[] s = new String[10];

    ci = query_result.getColumnIndex("id");
    s[0] = query_result.getString(ci);

    ci = query_result.getColumnIndex("label");
    s[1]= query_result.getString(ci);

    ci = query_result.getColumnIndex("title");
    s[2]= query_result.getString(ci);

    ci = query_result.getColumnIndex("description");
    s[3]= query_result.getString(ci);

    ci = query_result.getColumnIndex("starttimestamp");
    s[4]= query_result.getString(ci);

    ci = query_result.getColumnIndex("endtimestamp");
    s[5]= query_result.getString(ci);

    ci = query_result.getColumnIndex("start");
    s[6]= query_result.getString(ci);

    ci = query_result.getColumnIndex("end");
    s[7]= query_result.getString(ci);

    ci = query_result.getColumnIndex("timetype");
    s[8]= query_result.getString(ci);

    ci = query_result.getColumnIndex("user");
    s[9]= query_result.getString(ci);

    Log.d("WC",func_prefix + "sts:" + s[4] + " ets:" + s[5] + " start:" + s[6] + " end:" + s[7] + " timetype:" + s[8] + " user:" + s[9]);

    return s;
  }
}
