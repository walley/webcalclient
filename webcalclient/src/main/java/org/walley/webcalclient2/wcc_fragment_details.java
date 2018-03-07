package org.walley.webcalclient2;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/******************************************************************************/
/******************************************************************************/
/******************************************************************************/
public class wcc_fragment_details extends Fragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener
/******************************************************************************/
/******************************************************************************/
/******************************************************************************/
{
  Button b_date;
  Button b_time;
  TextView tv;
  wcc_date_picker picker;
  wcc_time_picker picker_t;
  EditText brief_et;
  EditText full_et;
  EditText dur_h_et;
  EditText dur_m_et;
  RadioButton rb;
  View rootView;
  SeekBar priority_sb;
  TextView priority_value_tv;
  RadioGroup access_rg;
  RadioGroup date_rg;
  TextView details_start_tv;
  TextView details_time_tv;
  int date_year;
  int date_month;
  int date_day;

  int time_hour;
  int time_minute;

  int utc_hour;
  int utc_minute;

  String action;

  on_date_set_listener      mCallback;
  on_brief_set_listener     b_callback;
  on_full_set_listener      f_callback ;
  on_access_set_listener    a_callback ;
  on_priority_set_listener  p_callback ;
  on_timed_set_listener     td_callback;
  on_time_set_listener      t_callback ;
  on_durationh_set_listener dh_callback;
  on_durationm_set_listener dm_callback;
  on_timedata_set_listener  timedata_callback;

  long duration_t;
  long duration_h;
  long duration_m;

  /***********/
  /*Listeners*/
  /***********/

  public interface on_date_set_listener
  {
    public void on_date_set(int d, int m, int y);
  }

  public interface on_brief_set_listener
  {
    public void on_brief_set(String s);
  }

  public interface on_full_set_listener
  {
    public void on_full_set(String s);
  }

  public interface on_access_set_listener
  {
    public void on_access_set(String s);
  }

  public interface on_priority_set_listener
  {
    public void on_priority_set(int i);
  }

  public interface on_timed_set_listener
  {
    public void on_timed_set(String s);
  }

  public interface on_time_set_listener
  {
    public void on_time_set(int h, int m);
  }

  public interface on_durationh_set_listener
  {
    public void on_durationh_set(int i);
  }

  public interface on_durationm_set_listener
  {
    public void on_durationm_set(int i);
  }

  public interface on_timedata_set_listener
  {
    public void update_time_data(int y, int m, int d, int h, int min, int dh, int dm);
  }

  /***********/
  /*Callbacks*/
  /***********/

  /******************************************************************************/
  @Override
  public void onAttach(Activity activity)
  /******************************************************************************/
  {
    super.onAttach(activity);

    // This makes sure that the container activity has implemented
    // the callback interface. If not, it throws an exception
    try {
      mCallback = (on_date_set_listener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement on_date_set_listener");
    }

    b_callback  = (on_brief_set_listener    ) activity;
    f_callback  = (on_full_set_listener     ) activity;
    a_callback  = (on_access_set_listener   ) activity;
    p_callback  = (on_priority_set_listener ) activity;
    td_callback = (on_timed_set_listener    ) activity;
    t_callback  = (on_time_set_listener     ) activity;
    dh_callback = (on_durationh_set_listener) activity;
    dm_callback = (on_durationm_set_listener) activity;
    timedata_callback = (on_timedata_set_listener) activity;
  }

  /******************************************************************************/
  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  /******************************************************************************/
  {

    Log.d("WC","wcc_fragment_details onCreateView(): start");

    action = getArguments().getString("action");

    String brief_text = getArguments().getString("label");
    String full_text = getArguments().getString("description");
    String access = getArguments().getString("access");
    String priority = getArguments().getString("priority");
    String duration = getArguments().getString("duration");
    String timestr = getArguments().getString("timestr");

    String starttimestamp = getArguments().getString("starttimestamp");
    String endtimestamp = getArguments().getString("endtimestamp");

    long start_ts = Long.parseLong(starttimestamp, 10);
    long end_ts = Long.parseLong(endtimestamp, 10);

    duration_t = end_ts - start_ts;
    duration_h = duration_t / 3600;
    duration_m = (duration_t - (duration_h*3600)) / 60;


    Log.d("WC","wcc_fragment_details onCreateView(): duration " + duration_h +":"+ duration_m);

    //start date string
    String start_str = getArguments().getString("start");

    //end date string
    String end_str = getArguments().getString("end");

    if (action.equals("new_event_extra")) {
    } else {
    }

    init_timedate_dialog_data(start_str);

    /*
        try {
          DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
          df.setTimeZone(TimeZone.getTimeZone("UTC"));
          Date result =  df.parse(start_str);

          Calendar calendar = Calendar.getInstance();
          calendar.setTimeZone(TimeZone.getDefault());
          calendar.setTime(result);
          date_year = calendar.get(Calendar.YEAR);
          date_month = calendar.get(Calendar.MONTH) + 1;
          date_day = calendar.get(Calendar.DAY_OF_MONTH);
          time_hour = calendar.get(Calendar.HOUR_OF_DAY);
          time_minute = calendar.get(Calendar.MINUTE);

          Log.d("WC","onCreateView(): result date for ui " + result.toString());

          calendar = Calendar.getInstance();
          calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
          calendar.setTime(result);
          int back_year = calendar.get(Calendar.YEAR);
          int back_month = calendar.get(Calendar.MONTH) + 1;
          int back_day = calendar.get(Calendar.DAY_OF_MONTH);
          int back_hour = calendar.get(Calendar.HOUR_OF_DAY);
          int back_minute = calendar.get(Calendar.MINUTE);

          Log.d("WC","onCreateView(): result date for seting time back " + result.toString());

          timedata_callback.update_time_data(back_year, back_month, back_day, back_hour, back_minute, (int)duration_h, (int)duration_m);


        } catch (ParseException pe) {
          pe.printStackTrace();
        }
    */

    rootView = inflater.inflate(R.layout.details_layout, container, false);

    b_date = (Button) rootView.findViewById(R.id.details_start_b);
    b_time = (Button) rootView.findViewById(R.id.details_time_b);
    tv = (TextView) rootView.findViewById(R.id.details_start_tv);
    brief_et = (EditText) rootView.findViewById(R.id.brief_et);
    full_et = (EditText) rootView.findViewById(R.id.full_et);
    dur_h_et = (EditText) rootView.findViewById(R.id.dur_h_et);
    dur_m_et = (EditText) rootView.findViewById(R.id.dur_m_et);
    access_rg = (RadioGroup) rootView.findViewById(R.id.rg_access);
    date_rg = (RadioGroup) rootView.findViewById(R.id.date_rg);
    priority_sb = (SeekBar) rootView.findViewById(R.id.priority_sb);
    priority_value_tv = (TextView) rootView.findViewById(R.id.priority_value_tv);
    details_start_tv = (TextView) rootView.findViewById(R.id.details_start_tv);
    details_time_tv = (TextView) rootView.findViewById(R.id.details_time_tv);

    dur_h_et = (EditText) rootView.findViewById(R.id.dur_h_et);
    dur_m_et = (EditText) rootView.findViewById(R.id.dur_m_et);

    Log.d("WC","wcc_fragment_details onCreateView(): second duration " + duration_h +":"+ duration_m);

    dur_h_et.setText(String.valueOf(duration_h));
    dur_m_et.setText(String.valueOf(duration_m));

    dh_callback.on_durationh_set((int)duration_h);
    dm_callback.on_durationm_set((int)duration_m);

    brief_et.setText(brief_text);
    full_et.setText(full_text);

    details_start_tv.setText(date_day + "/" + date_month + "/" + date_year);
    details_time_tv.setText(String.format("%02d",time_hour) + ":" + String.format("%02d",time_minute));

    switch (access) {
    case "P":
      access_rg.check(R.id.details_r_public);
      break;
    case "R":
      access_rg.check(R.id.details_r_private);
      break;
    case "C":
      access_rg.check(R.id.details_r_confidential);
      break;
    }

    switch (timestr) {
    case "All day event":
      date_rg.check(R.id.details_r_allday);
      break;
    default:
      date_rg.check(R.id.details_r_timed);
      break;
    }

    Log.d("WC","wcc_fragment_details onCreateView(): priority " + priority);
    priority_sb.setProgress(Integer.parseInt(priority));
    priority_value_tv.setText(priority);

    priority_sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        p_callback.on_priority_set(progress);
        priority_value_tv.setText(""+progress);
      }
      public void onStartTrackingTouch(SeekBar seekBar) {
      }
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    //date picker
    picker = new wcc_date_picker(this);
    b_date.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View arg0) {
        Log.d("WC","date_picker onclick():" + date_year +"/"+ date_month +"/"+ date_day);
        picker.set_date(date_year, date_month - 1, date_day);
        picker.show(getFragmentManager(), "datePicker");
      }
    });

    //time picker
    picker_t = new wcc_time_picker(this);
    b_time.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View arg0) {
        picker_t.set_time(time_hour, time_minute);
        picker_t.show(getFragmentManager(), "timePicker");
      }
    });

    brief_et.addTextChangedListener(new TextWatcher() {
      public void afterTextChanged(Editable s) {
        b_callback.on_brief_set(brief_et.getText().toString());
      }
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
      public void onTextChanged(CharSequence s, int start, int before, int count) {}
    });

    full_et.addTextChangedListener(new TextWatcher() {
      public void afterTextChanged(Editable s) {
        f_callback.on_full_set(full_et.getText().toString());
      }
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
      public void onTextChanged(CharSequence s, int start, int before, int count) {}
    });

    //duration_m changed
    dur_m_et.addTextChangedListener(new TextWatcher() {
      public void afterTextChanged(Editable s) {
        int i = 0;
        try {
          i = Integer.parseInt(dur_m_et.getText().toString());
        }  catch (Exception e) {
          Log.e("WC","duration_m text parse error" + e.toString());
        }

        dm_callback.on_durationm_set(i);
      }
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
      public void onTextChanged(CharSequence s, int start, int before, int count) {}
    });

    //duration_h changed
    dur_h_et.addTextChangedListener(new TextWatcher() {
      public void afterTextChanged(Editable s) {
        int i = 0;
        try {
          i = Integer.parseInt(dur_h_et.getText().toString());
        }  catch (Exception e) {
          Log.e("WC","duration_h text parse error" + e.toString());
        }
        dh_callback.on_durationh_set(i);
      }
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
      public void onTextChanged(CharSequence s, int start, int before, int count) {}
    });

    access_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        String as = "";
        rb = (RadioButton)rootView.findViewById(checkedId);
        int idx = group.indexOfChild(rb);

        switch (idx) {
        case 0:
          as = "P";
          break;

        case 1:
          as = "R";
          break;

        case 2:
          as = "C";
          break;

        }
        a_callback.on_access_set(as);
      }
    });

    date_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        String as = "";
        rb = (RadioButton)rootView.findViewById(checkedId);
        as = rb.getText().toString();
        td_callback.on_timed_set(as);

        Log.d("WC","date_rg onclick():" + as);

        if (as.equals(getResources().getString(R.string.allday))) {
          b_time.setEnabled(false);
        } else {
          b_time.setEnabled(true);
        }
      }
    });

    return rootView;
  }

  /******************************************************************************/
  public void onDateSet(DatePicker view, int year, int month, int day)
  /******************************************************************************/
  {
    date_year = year;
    month++;
    date_month = month;
    date_day = day;

    StringBuilder sb = new StringBuilder()
    .append(day)
    .append("/")
    .append(month)
    .append("/")
    .append(year);
    tv.setText(sb.toString());
    Log.d("WC","onDateSet " + sb.toString());

    mCallback.on_date_set(day, month, year);
  }

  /******************************************************************************/
  public void onTimeSet(TimePicker view, int hour, int minute)
  /******************************************************************************/
  {
    Date d = null;
    StringBuilder tmp = new StringBuilder();
    tmp.append(String.format("%02d", hour));
    tmp.append(":");
    tmp.append(String.format("%02d", minute));
    String value = tmp.toString();

    Log.d("WC","onTimeSet before" + value);

    // Parses the value and assumes it represents a date and time in the local timezone
    DateFormat df1 = new SimpleDateFormat("HH:mm");
    df1.setTimeZone(TimeZone.getDefault());
    try {
      d = df1.parse(value);
    } catch (ParseException pe) {
      pe.printStackTrace();
    }

    Log.d("WC","onTimeSet d:"+d.toString());

    // date in the UTC timezone
    DateFormat dfh = new SimpleDateFormat("HH");
    dfh.setTimeZone(TimeZone.getTimeZone("UTC"));

    DateFormat dfm = new SimpleDateFormat("mm");
    dfm.setTimeZone(TimeZone.getTimeZone("UTC"));

    utc_hour = Integer.parseInt(dfh.format(d));
    utc_minute = Integer.parseInt(dfm.format(d));

    time_hour = hour;
    time_minute = minute;

    details_time_tv.setText(new StringBuilder()
                            .append(String.format("%02d", hour))
                            .append(":")
                            .append(String.format("%02d", minute)));

    System.out.println(tv.getText().toString());
    t_callback.on_time_set(utc_hour, utc_minute);

//    update_time_data(int y, int m, int d, utc_hour, utc_minute, int dh, int dm);

  }

  public void init_timedate_dialog_data(String start_str)
  {
    try {
      DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
      df.setTimeZone(TimeZone.getTimeZone("UTC"));
      Date result =  df.parse(start_str);

      Calendar calendar = Calendar.getInstance();
      calendar.setTimeZone(TimeZone.getDefault());
      calendar.setTime(result);
      date_year = calendar.get(Calendar.YEAR);
      date_month = calendar.get(Calendar.MONTH) + 1;
      date_day = calendar.get(Calendar.DAY_OF_MONTH);
      time_hour = calendar.get(Calendar.HOUR_OF_DAY);
      time_minute = calendar.get(Calendar.MINUTE);

      Log.d("WC","init_timedate_dialog_data(): result date for ui " + result.toString());

      calendar = Calendar.getInstance();
      calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
      calendar.setTime(result);
      int back_year = calendar.get(Calendar.YEAR);
      int back_month = calendar.get(Calendar.MONTH) + 1;
      int back_day = calendar.get(Calendar.DAY_OF_MONTH);
      int back_hour = calendar.get(Calendar.HOUR_OF_DAY);
      int back_minute = calendar.get(Calendar.MINUTE);

      Log.d("WC","init_timedate_dialog_data(): result date for seting time back " + result.toString());

      timedata_callback.update_time_data(back_year, back_month, back_day, back_hour, back_minute, (int)duration_h, (int)duration_m);


    } catch (ParseException pe) {
      Log.e("WC","init_timedate_dialog_data");
      pe.printStackTrace();
    }
  }
}
