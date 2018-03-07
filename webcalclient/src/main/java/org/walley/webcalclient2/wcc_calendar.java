package org.walley.webcalclient2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import android.view.Gravity;
import android.graphics.Typeface;
import android.widget.TableLayout;
import android.text.TextUtils;

/******************************************************************************/
/******************************************************************************/
/******************************************************************************/
public class wcc_calendar extends wcc_activity implements OnClickListener
/******************************************************************************/
/******************************************************************************/
/******************************************************************************/
{
  private static final String tag = "WC";

  private ImageView calendarToJournalButton;
  private Button selectedDayMonthYearButton;
  private Button currentMonth;
  private ImageView prevMonth;
  private ImageView nextMonth;
  private  GridView calendarView;
//  private  ExpandableHeightGridView calendarView;

  private GridCellAdapter adapter;
  private Calendar _calendar;
  private int month, year;
  private final DateFormat dateFormatter = new DateFormat();
  private static final String dateTemplate = "MMMM yyyy";

  public wcc_u u = new wcc_u();


  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.simple_calendar_view);

    _calendar = Calendar.getInstance(Locale.getDefault());
    month = _calendar.get(Calendar.MONTH) + 1;
    year = _calendar.get(Calendar.YEAR);

    prevMonth = (ImageView) this.findViewById(R.id.prevMonth);
    prevMonth.setOnClickListener(this);

    currentMonth = (Button) this.findViewById(R.id.currentMonth);
    currentMonth.setText(dateFormatter.format(dateTemplate, _calendar.getTime()));

    nextMonth = (ImageView) this.findViewById(R.id.nextMonth);
    nextMonth.setOnClickListener(this);

    calendarView = (GridView) this.findViewById(R.id.calendar);

//    adapter = new GridCellAdapter(getApplicationContext(), R.id.ll_gridcell, month, year);
//    adapter.notifyDataSetChanged();
//    calendarView.setAdapter(adapter);

    setGridCellAdapterToDate(month, year);
  }

  private void setGridCellAdapterToDate(int month, int year)
  {
    Log.d(tag, "Calendar Instance:= " + "Month: " + month + " " + "Year: " + year);
    adapter = new GridCellAdapter(getApplicationContext(), R.id.ll_gridcell, month, year);
    _calendar.set(year, month - 1, _calendar.get(Calendar.DAY_OF_MONTH));
    currentMonth.setText(dateFormatter.format(dateTemplate, _calendar.getTime()));

//    adapter.refreshDays();
    adapter.notifyDataSetChanged();

    calendarView.setAdapter(adapter);
  }

  @Override
  public void onClick(View v)
  {
    if (v == prevMonth) {
      if (month <= 1) {
        month = 12;
        year--;
      } else {
        month--;
      }
      Log.d(tag, "Setting Prev Month in GridCellAdapter: " + "Month: " + month + " Year: " + year);
      setGridCellAdapterToDate(month, year);
    }
    if (v == nextMonth) {
      if (month > 11) {
        month = 1;
        year++;
      } else {
        month++;
      }
      Log.d(tag, "Setting Next Month in GridCellAdapter: " + "Month: " + month + " Year: " + year);
      setGridCellAdapterToDate(month, year);
    }
  }

  @Override
  public void onDestroy()
  {
    Log.d(tag, "Destroying View ...");
    super.onDestroy();
  }

  /******************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
  static class ViewHolder
  /******************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
  {
    TextView tv_1;
    TextView tv_2;
    TextView tv_3;
    LinearLayout ll;
    int position;
    TextView text;
    TextView [] txt = new TextView[10];
    String day;
    String month;
    String year;
  }

  /******************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
  public class GridCellAdapter extends BaseAdapter implements OnClickListener
  /******************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
  {
    private static final String tag = "GridCellAdapter";
    private final Context _context;

    private final List<String> list;
    HashMap events_data = new HashMap<Integer, ArrayList<String>>();

    private static final int DAY_OFFSET = 1;
    private final String[] weekdays = new String[] {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private final String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    private final int[] daysOfMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private final int month, year;
    private int daysInMonth, prevMonthDays;
    private int currentDayOfMonth;
    private int currentWeekDay;
    private LinearLayout gridcell;
    private TextView num_events_per_day;
    private TextView gridcell_dn;
    private TextView tv_txt;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy");
    GradientDrawable gd;


    /******************************************************************************/
    public void get_date_events(int day, int month, int year)
    /******************************************************************************/
    {
      long timestamp = u.get_timestamp_from_date(day, month, year);
      int ci;
      SQLiteDatabase mydb;
      String s;
      ArrayList arr_list = new ArrayList<String>();

      String query = "select * from event where starttimestamp>="+timestamp+" and endtimestamp<="+(timestamp+86400);
      Log.d("WC","get_date_events(): query " + query);

      mydb = open_db();

      try {
        Cursor query_result  = mydb.rawQuery(query, null);
        String[] header = query_result.getColumnNames();

        System.out.println("COUNT : " + query_result.getCount());
        System.out.println("Column COUNT : " + query_result.getColumnCount());

        if(query_result.moveToFirst()) {

          do {
            ci = query_result.getColumnIndex("id");
            s  = query_result.getString(ci) + " ";
            ci = query_result.getColumnIndex("label");
            s += query_result.getString(ci) + " ";
            arr_list.add(s);
            Log.d("WC","get_date_events("+day+","+month+","+year+"): adding " + s);
            events_data.put(day, arr_list);
          } while(query_result.moveToNext());
        }
      } catch(Exception e) {
        Log.d("WC","get_date_events("+day+","+month+","+year+"): " + e.toString());
        e.printStackTrace();
      }
      close_db(mydb);
    }

    /******************************************************************************/
    public GridCellAdapter(Context context, int textViewResourceId, int month, int year)
    /******************************************************************************/
    {
      super();
      this._context = context;
      this.list = new ArrayList<String>();
      this.month = month;
      this.year = year;

      Log.d(tag, "==> Passed in Date FOR Month: " + month + " " + "Year: " + year);
      Calendar calendar = Calendar.getInstance();
      setCurrentDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
      setCurrentWeekDay(calendar.get(Calendar.DAY_OF_WEEK));
      Log.d(tag, "New Calendar:= " + calendar.getTime().toString());
      Log.d(tag, "CurrentDayOfWeek :" + getCurrentWeekDay());
      Log.d(tag, "CurrentDayOfMonth :" + getCurrentDayOfMonth());

      GradientDrawable gd = new GradientDrawable();
      gd.setColor(0xFF00FF00); // Changes this drawbale to use a single color instead of a gradient
      gd.setCornerRadius(5);
      gd.setStroke(1, 0xFF000000);

      // Print Month
      create_month(month, year);
    }

    /******************************************************************************/
    private String getMonthAsString(int i)
    /******************************************************************************/
    {
      return months[i];
    }

    /******************************************************************************/
    private String getWeekDayAsString(int i)
    /******************************************************************************/
    {
      return weekdays[i];
    }

    /******************************************************************************/
    private int getNumberOfDaysOfMonth(int i)
    /******************************************************************************/
    {
      return daysOfMonth[i];
    }

    /******************************************************************************/
    public String getItem(int position)
    /******************************************************************************/
    {
      return list.get(position);
    }

    /******************************************************************************/
    @Override
    public int getCount()
    /******************************************************************************/
    {
//      return 35;
      return list.size();
    }

    /******************************************************************************/
    private void create_month(int mm, int yy)
    /******************************************************************************/
    {
      Log.d(tag, "create_month: mm: " + mm + " " + "yy: " + yy);
      // The number of days to leave blank at
      // the start of this month.
      int trailingSpaces = 0;
      int leadSpaces = 0;
      int daysInPrevMonth = 0;
      int prevMonth = 0;
      int prevYear = 0;
      int nextMonth = 0;
      int nextYear = 0;
      String color = "WHITE";
      int currentMonth = mm - 1;
      String currentMonthName = getMonthAsString(currentMonth);
      daysInMonth = getNumberOfDaysOfMonth(currentMonth);

      Log.d(tag, "Current Month: " + currentMonth + " " + currentMonthName + " having " + daysInMonth + " days.");

      // Gregorian Calendar : MINUS 1, set to FIRST OF MONTH
      GregorianCalendar cal = new GregorianCalendar(yy, currentMonth, 1);
      Log.d(tag, "Gregorian Calendar:= " + cal.getTime().toString());

      if (currentMonth == 11) {
        prevMonth = currentMonth - 1;
        daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
        nextMonth = 0;
        prevYear = yy;
        nextYear = yy + 1;
      } else if (currentMonth == 0) {
        prevMonth = 11;
        prevYear = yy - 1;
        nextYear = yy;
        daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
        nextMonth = 1;
      } else {
        prevMonth = currentMonth - 1;
        nextMonth = currentMonth + 1;
        nextYear = yy;
        prevYear = yy;
        daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
      }

      Log.d(tag, "PrevYear: " + prevYear + " PrevMonth:" + prevMonth + " NextMonth: " + nextMonth + " NextYear: " + nextYear);

      // Compute how much to leave before before the first day of the
      // month.
      // getDay() returns 0 for Sunday.
      int currentWeekDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
      trailingSpaces = currentWeekDay;

      Log.d(tag, "Week Day:" + currentWeekDay + " is " + getWeekDayAsString(currentWeekDay));
      Log.d(tag, "No. Trailing space to Add: " + trailingSpaces);
      Log.d(tag, "No. of Days in Previous Month: " + daysInPrevMonth);

      if (cal.isLeapYear(cal.get(Calendar.YEAR)) && mm == 1) {
        ++daysInMonth;
      }

      // Trailing Month days
      for (int i = 0; i < trailingSpaces; i++) {
        String ss = String.valueOf((daysInPrevMonth - trailingSpaces + DAY_OFFSET) + i) + "-GREY" + "-" + (prevMonth+1) + "-" + prevYear;
        list.add(ss);;
      }

      // Current Month Days
      /*      for (int i = 1; i <= daysInMonth; i++) {
              Log.d(currentMonthName, String.valueOf(i) + " " + getMonthAsString(currentMonth) + " " + yy);
              if (i == getCurrentDayOfMonth()) {
                list.add(String.valueOf(i) + "-BLUE" + "-" + getMonthAsString(currentMonth) + "-" + yy);
              } else {
                list.add(String.valueOf(i) + "-WHITE" + "-" + getMonthAsString(currentMonth) + "-" + yy);
              }
              get_date_events(i,currentMonth+1,yy);
            }
      */
      for (int i = 1; i <= daysInMonth; i++) {
        Log.d(currentMonthName, String.valueOf(i) + " " + mm + " " + yy);
        if (i == getCurrentDayOfMonth()) {
          color="BLUE";
        } else {
          color="WHITE";
        }
        list.add(String.valueOf(i) + "-" + color + "-" + mm + "-" + yy);
        get_date_events(i, mm, yy);
      }

      // Leading Month days
      for (int i = 0; i < list.size() % 7; i++) {
        Log.d(tag, "NEXT MONTH:= " + getMonthAsString(nextMonth));
        list.add(String.valueOf(i + 1) + "-GREY" + "-" + (nextMonth+1) + "-" + nextYear);
      }
    }

    /******************************************************************************/
    private HashMap findNumberOfEventsPerMonth(int year, int month)
    /******************************************************************************/
    {
      HashMap map = new HashMap<String, Integer>();
      return map;
    }

    /******************************************************************************/
    @Override
    public long getItemId(int position)
    /******************************************************************************/
    {
      return position;
    }


    /******************************************************************************/
    private void set_textview_style(TextView tv)
    /******************************************************************************/
    {

//      tv.setHeight(500);
      tv.setTextColor(Color.parseColor("#000000"));
      tv.setTextSize(7);
      tv.setSingleLine(true);
      tv.setEllipsize(TextUtils.TruncateAt.END);
//      tv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
      tv.setLayoutParams(new TableLayout.LayoutParams(
                           LayoutParams.WRAP_CONTENT,
                           LayoutParams.WRAP_CONTENT, 1f));
      tv.setBackgroundDrawable(gd);
    }

    /******************************************************************************/
    private ArrayList<String> get_events_list(int d)
    /******************************************************************************/
    {
      ArrayList<String> list = null;

      if ((!events_data.isEmpty()) && (events_data != null)) {
        if (events_data.containsKey(d)) {
          list = (ArrayList)events_data.get(d);
          if (list == null) {
            list = new ArrayList<String>();
            list.add("nic pole ma");
          }

        } else {

          if (list == null) {
            list = new ArrayList<String>();
            list.add("nic pole nema");
          }
        }
      } else {
        list = new ArrayList<String>();
        list.add("nic");
      }

      return list;
    }


    /******************************************************************************/
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    /******************************************************************************/
    {
      ViewHolder holder;
      ArrayList<String> ev_list;

      String[] day_color = list.get(position).split("-");
      String theday = day_color[0];
      String themonth = day_color[2];
      String theyear = day_color[3];
      int i_theday = Integer.parseInt(theday);
      Log.d(tag, "position, theday: " + position + " " + theday);

      ev_list = get_events_list(i_theday);
      int ev_list_size;
      if (ev_list == null) {
        ev_list_size = 0;
      } else {
        ev_list_size = ev_list.size();
      }

      Log.d(tag, "ev_list: " + ev_list_size);

      if (convertView == null) {
        LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.calendar_day_gridcell, parent, false);
        holder = new ViewHolder();

        holder.ll = (LinearLayout)convertView.findViewById(R.id.ll_gridcell);
        holder.ll.setOnClickListener(this);

        holder.text = (TextView)convertView.findViewById(R.id.day_number);
        holder.tv_1 = new TextView(_context);
        holder.tv_2 = new TextView(_context);
        holder.tv_3 = new TextView(_context);

        for(int i = 0; i < 5; i++) {
          holder.txt[i] = new TextView(_context);
          holder.ll.addView(holder.txt[i],0);
        }
        holder.ll.addView(holder.tv_1, 0);
        holder.ll.addView(holder.tv_2, 0);
        holder.ll.addView(holder.tv_3, 0);

        holder.day = theday;
        holder.month = themonth;
        holder.year = theyear;

        convertView.setTag(holder); // set the View holder
        Log.d(tag, "getView(): settag to: " + theday  + " " + themonth + " " + theyear);
      } else {
        holder = (ViewHolder) convertView.getTag();
      }

      if (day_color[1].equals("GREY")) {
        holder.text.setTextColor(Color.LTGRAY);
      }
      if (day_color[1].equals("WHITE")) {
        holder.text.setTextColor(Color.BLACK);
      }
      if (day_color[1].equals("BLUE")) {
        holder.text.setTextColor(Color.BLUE);
      }

      holder.text.setText(Integer.toString(position));
      holder.tv_1.setText(theday);
      holder.tv_2.setText("2");
      holder.tv_1.setTextSize(20);
      holder.tv_2.setTextSize(20);

      for(int i = 0; i < 5; i++) {
        if (i < ev_list_size) {
          holder.txt[i].setText(ev_list.get(i));
          Log.d(tag, " added " + ev_list.get(i));
          set_textview_style(holder.txt[i]);
        } else {
          holder.txt[i].setText("empty"+i);
          holder.txt[i].setTextSize(7);
        }
      }

      return convertView;
    }

    /******************************************************************************/
    @Override
    public void onClick(View view)
    /******************************************************************************/
    {
      ViewHolder holder = (ViewHolder) view.getTag();
      String date_month_year = holder.day + "-" + holder.month + "-" + holder.year;
      Log.d(tag, "onClick() Selected: " + date_month_year);

      Intent i = new Intent();
      i.setClass(wcc_calendar.this,wcc_day.class);
      i.putExtra("day",   holder.day);
      i.putExtra("month", holder.month);
      i.putExtra("year",  holder.year);
      startActivity(i);
    }

    /******************************************************************************/
    public int getCurrentDayOfMonth()
    /******************************************************************************/
    {
      return currentDayOfMonth;
    }

    /******************************************************************************/
    private void setCurrentDayOfMonth(int currentDayOfMonth)
    /******************************************************************************/
    {
      this.currentDayOfMonth = currentDayOfMonth;
    }

    /******************************************************************************/
    public void setCurrentWeekDay(int currentWeekDay)
    /******************************************************************************/
    {
      this.currentWeekDay = currentWeekDay;
    }

    /******************************************************************************/
    public int getCurrentWeekDay()
    /******************************************************************************/
    {
      return currentWeekDay;
    }
  }
}
