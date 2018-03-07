package org.walley.webcalclient2;

import android.text.Editable;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TimePicker;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import android.util.Log;
import android.text.TextWatcher;
import android.widget.CheckBox;
import android.view.View.OnClickListener;
import android.view.View;

public class wcc_fragment_repeat
  extends Fragment
  implements
  OnItemSelectedListener,
  DatePickerDialog.OnDateSetListener,
  TimePickerDialog.OnTimeSetListener
{
  wcc_date_picker picker;
  wcc_time_picker picker_t;

  Button b_date;
  Button b_time;

  public String cal_id;
  public String cal_type;
  public String cal_end;
  public String cal_frequency;
  public String cal_days;
  public String cal_endtime;
  public String cal_bymonth;
  public String cal_bymonthday;
  public String cal_byday;
  public String cal_bysetpos;
  public String cal_byweekno;
  public String cal_byyearday;
  public String cal_wkst;
  public String cal_count;

  public EditText et_cal_id;
  public EditText et_cal_type;
  public EditText et_cal_end;
  public EditText et_cal_frequency;
  public EditText et_cal_days;
  public EditText et_cal_endtime;
  public EditText et_cal_bymonth;
  public EditText et_cal_bymonthday;
  public EditText et_cal_byday;
  public EditText et_cal_bysetpos;
  public EditText et_cal_byweekno;
  public EditText et_cal_byyearday;
  public EditText et_cal_wkst;
  public EditText et_cal_count;

  public EditText et_number_of_times;

  public LinearLayout none_ll;
  public LinearLayout daily_ll;
  public LinearLayout not_implemented_ll;
  public RadioGroup rg_daily_repeat_ending;

  CheckBox cb_weekend;  //  cal_byday: MO,TU,WE,TH,FR
  View rootView;

  on_repeat_update_listener callback;

  int y, m, d, h, e;

  /******************************************************************************/
  public interface on_repeat_update_listener
  /******************************************************************************/
  {
    public void on_repeat_update(HashMap<String, String> repeat_hash);
  }

  /******************************************************************************/
  public void onAttach(Activity activity)
  /******************************************************************************/
  {
    super.onAttach(activity);
    // This makes sure that the container activity has implemented
    // the callback interface. If not, it throws an exception
    try {
      callback = (on_repeat_update_listener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement on_repeat_update_listener");
    }
  }

  /******************************************************************************/
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  /******************************************************************************/
  {

    y = m = d = h = e = 0;

    rootView = inflater.inflate(R.layout.repeat_layout, container, false);

    rg_daily_repeat_ending = (RadioGroup) rootView.findViewById(R.id.rg_daily_repeat_ending);
    cb_weekend = (CheckBox)rootView.findViewById(R.id.cb_weekend);
    et_number_of_times = (EditText)rootView.findViewById(R.id.et_number_of_times);
    b_date = (Button) rootView.findViewById(R.id.repeat_ending_d_b);
    b_time = (Button) rootView.findViewById(R.id.repeat_ending_t_b);
    Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner_repeat);

    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                                         R.array.repeat_array, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
    spinner.setOnItemSelectedListener(this);

    b_time.setEnabled(false);
    b_date.setEnabled(false);

    et_cal_id         = (EditText)rootView.findViewById(R.id.cal_id);
    et_cal_type       = (EditText)rootView.findViewById(R.id.cal_type);
    et_cal_end        = (EditText)rootView.findViewById(R.id.cal_end);
    et_cal_frequency  = (EditText)rootView.findViewById(R.id.cal_frequency);
    et_cal_days       = (EditText)rootView.findViewById(R.id.cal_days);
    et_cal_endtime    = (EditText)rootView.findViewById(R.id.cal_endtime);
    et_cal_bymonth    = (EditText)rootView.findViewById(R.id.cal_bymonth);
    et_cal_bymonthday = (EditText)rootView.findViewById(R.id.cal_bymonthday);
    et_cal_byday      = (EditText)rootView.findViewById(R.id.cal_byday);
    et_cal_bysetpos   = (EditText)rootView.findViewById(R.id.cal_bysetpos);
    et_cal_byweekno   = (EditText)rootView.findViewById(R.id.cal_byweekno);
    et_cal_byyearday  = (EditText)rootView.findViewById(R.id.cal_byyearday);
    et_cal_wkst       = (EditText)rootView.findViewById(R.id.cal_wkst);
    et_cal_count      = (EditText)rootView.findViewById(R.id.cal_count);

    none_ll = (LinearLayout)rootView.findViewById(R.id.none_ll);
    daily_ll = (LinearLayout)rootView.findViewById(R.id.daily_ll);
    not_implemented_ll = (LinearLayout)rootView.findViewById(R.id.not_implemented_ll);

    cal_id = getArguments().getString("cal_id");
    cal_type = getArguments().getString("cal_type");
    cal_end = getArguments().getString("cal_end");
    cal_frequency = getArguments().getString("cal_frequency");
    cal_days = getArguments().getString("cal_days");
    cal_endtime = getArguments().getString("cal_endtime");
    cal_bymonth = getArguments().getString("cal_bymonth");
    cal_bymonthday = getArguments().getString("cal_bymonthday");
    cal_byday = getArguments().getString("cal_byday");
    cal_bysetpos = getArguments().getString("cal_bysetpos");
    cal_byweekno = getArguments().getString("cal_byweekno");
    cal_byyearday = getArguments().getString("cal_byyearday");
    cal_wkst = getArguments().getString("cal_wkst");
    cal_count = getArguments().getString("cal_count");

    et_cal_id        .setText(cal_id        );
    et_cal_type      .setText(cal_type      );
    et_cal_end       .setText(cal_end       );
    et_cal_frequency .setText(cal_frequency );
    et_cal_days      .setText(cal_days      );
    et_cal_endtime   .setText(cal_endtime   );
    et_cal_bymonth   .setText(cal_bymonth   );
    et_cal_bymonthday.setText(cal_bymonthday);
    et_cal_byday     .setText(cal_byday     );
    et_cal_bysetpos  .setText(cal_bysetpos  );
    et_cal_byweekno  .setText(cal_byweekno  );
    et_cal_byyearday .setText(cal_byyearday );
    et_cal_wkst      .setText(cal_wkst      );
    et_cal_count     .setText(cal_count     );

    switch (cal_type) {
    case "none":
      spinner.setSelection(0);
      break;
    case "daily":
      spinner.setSelection(1);
      break;
    }

    if (cal_type.equals("daily")) {
      if (!cal_count.equals("null")) {
        rg_daily_repeat_ending.check(R.id.rb_ending_t);
        et_number_of_times.setText(cal_count);
        et_number_of_times.setEnabled(true);
      } else if (!cal_end.equals("null")) {
      } else {
        rg_daily_repeat_ending.check(R.id.rb_ending_f);
      }
    }
    update_debugview();


    rg_daily_repeat_ending.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton rb = (RadioButton)rootView.findViewById(checkedId);
        int idx = group.indexOfChild(rb);

        switch (idx) {
        case 0:  // number of times
          b_time.setEnabled(false);
          b_date.setEnabled(false);
          et_number_of_times.setEnabled(true);
          et_cal_end.setText("null");
          et_cal_endtime.setText("null");
          et_cal_count.setText("null");
          cal_count = "null";
          cal_end = "null";
          cal_endtime = "null";
          break;
        case 1: // use end date
          b_time.setEnabled(true);
          b_date.setEnabled(true);
          et_number_of_times.setEnabled(false);
          et_cal_count.setText("null");
          cal_count = "null";
          break;
        case 2: // forever
          b_time.setEnabled(false);
          b_date.setEnabled(false);
          et_number_of_times.setEnabled(false);
          et_number_of_times.setText("");
          et_cal_end.setText("null");
          et_cal_endtime.setText("null");
          cal_end = "null";
          cal_endtime = "null";
          break;

        }

        update();
      }
    });

    //date picker
    picker = new wcc_date_picker(this);

    b_date.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View arg0) {
//        picker.set_date(date_year, date_month - 1, date_day);
        picker.show(getFragmentManager(), "datePicker");
      }
    });

    //time picker
    picker_t = new wcc_time_picker(this);

    b_time.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View arg0) {
//        picker_t.set_time(time_hour, time_minute);
        picker_t.show(getFragmentManager(), "timePicker");
      }
    });

    et_number_of_times.addTextChangedListener(new TextWatcher() {
      public void afterTextChanged(Editable s) {
        cal_count = et_number_of_times.getText().toString();
        et_cal_count.setText(cal_count);
        update();
      }
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
      public void onTextChanged(CharSequence s, int start, int before, int count) {}
    });

    cb_weekend.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        boolean checked = ((CheckBox) v).isChecked();
        if (checked) {
          et_cal_byday.setText("MO,TU,WE,TH,FR");
        } else {
          et_cal_byday.setText("null");
        }
      }
    });


    return rootView;
  }

  /******************************************************************************/
  public void onCheckboxClicked(View view)
  /******************************************************************************/
  {
    boolean checked = ((CheckBox) view).isChecked();

    switch(view.getId()) {
    case R.id.cb_weekend:
      if (checked) {
        et_cal_byday.setText("MO,TU,WE,TH,FR");
      } else {
        et_cal_byday.setText("null");
      }
      break;
    }
    update();
  }

  /******************************************************************************/
  public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
  /******************************************************************************/
  {
    none_ll.setVisibility(LinearLayout.GONE);
    daily_ll.setVisibility(LinearLayout.GONE);
    not_implemented_ll.setVisibility(LinearLayout.GONE);

    switch (pos) {
    case 0:
      none_ll.setVisibility(LinearLayout.VISIBLE);
      empty();
      cal_type = "none";
      et_cal_type.setText(cal_type);
      break;
    case 1:
      daily_ll.setVisibility(LinearLayout.VISIBLE);
      cal_type = "daily";
      et_cal_type.setText(cal_type);
      break;
    default:
      not_implemented_ll.setVisibility(LinearLayout.VISIBLE);
      empty();
      cal_type = "none";
      et_cal_type.setText(cal_type);
      break;
    }

    update();

  }

  /******************************************************************************/
  @Override
  public void onNothingSelected(AdapterView<?> arg0)
  /******************************************************************************/
  {
  }

  /******************************************************************************/
  public void update()
  /******************************************************************************/
  {
    HashMap<String, String> repeat_hash = new HashMap<String, String>();

    repeat_hash.put("cal_id", cal_id);
    repeat_hash.put("cal_type", cal_type);
    repeat_hash.put("cal_end", cal_end);
    repeat_hash.put("cal_frequency", cal_frequency);
    repeat_hash.put("cal_days", cal_days);
    repeat_hash.put("cal_endtime", cal_endtime);
    repeat_hash.put("cal_bymonth", cal_bymonth);
    repeat_hash.put("cal_bymonthday", cal_bymonthday);
    repeat_hash.put("cal_byday", cal_byday);
    repeat_hash.put("cal_bysetpos", cal_bysetpos);
    repeat_hash.put("cal_byweekno", cal_byweekno);
    repeat_hash.put("cal_byyearday", cal_byyearday);
    repeat_hash.put("cal_wkst", cal_wkst);
    repeat_hash.put("cal_count", cal_count);

    update_debugview();

    callback.on_repeat_update(repeat_hash);
  }

  /******************************************************************************/
  public void empty()
  /******************************************************************************/
  {
    HashMap<String, String> repeat_hash = new HashMap<String, String>();

    cal_type = "none";
    cal_end = "null";
    cal_frequency = "null";
    cal_days = "null";
    cal_endtime = "null";
    cal_bymonth = "null";
    cal_bymonthday = "null";
    cal_byday = "null";
    cal_bysetpos = "null";
    cal_byweekno = "null";
    cal_byyearday = "null";
    cal_wkst = "null";
    cal_count = "null";

    update();
  }

  public int get()
  {
    return 1;
  }

  /******************************************************************************/
  public void onDateSet(DatePicker view, int year, int month, int day)
  /******************************************************************************/
  {
    y = year - 1900;
    m = month + 1;
    d = day;

    Log.d("WC","ondateset(): " + y + "/" +m + "/" +d);

    update_datetime(y, m, d, h, e);
  }

  /******************************************************************************/
  public void onTimeSet(TimePicker view, int hour, int minute)
  /******************************************************************************/
  {
    h = hour;
    e = minute;
    Log.d("WC","ondateset(): " + h + ":" + e);

    update_datetime(y, m, d, h, e);
  }

  /******************************************************************************/
  public void update_datetime(int year, int month, int day, int hour, int minute)
  /******************************************************************************/
  {
    //ampm = 0;

    Date d = new Date(year, month, day, hour, minute, 0);

    Log.d("WC","update_datetime(): " + d.toString());

    DateFormat df_d = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
    df_d.setTimeZone(TimeZone.getTimeZone("UTC"));
//    df_d.setTimeZone(TimeZone.getDefault());
    String result_d = df_d.format(d);

    DateFormat df_t = new SimpleDateFormat("HHmmss", Locale.ENGLISH);
    df_t.setTimeZone(TimeZone.getTimeZone("UTC"));
//    df_t.setTimeZone(TimeZone.getDefault());
    String result_t = df_t.format(d);

    cal_end = result_d;
    cal_endtime = result_t;
    et_cal_end.setText(cal_end);
    et_cal_endtime.setText(cal_endtime);

    update();

    /*
    if ( ! empty ( $rpt_year ) ) {
    $rpt_hour += $rpt_ampm;
    $rpt_until = mktime ( $rpt_hour, $rpt_minute, 0, $rpt_month, $rpt_day, $rpt_year );
    mktime returns timestamp
    }

    cal_end';
    $values[] = gmdate ( 'Ymd', $rpt_until );

    $names[] = 'cal_endtime';
    $values[] = gmdate ( 'His', $rpt_until
    */

  }

  /******************************************************************************/
  public void update_debugview()
  /******************************************************************************/
  {
    et_cal_id        .setText(cal_id        );
    et_cal_type      .setText(cal_type      );
    et_cal_end       .setText(cal_end       );
    et_cal_frequency .setText(cal_frequency );
    et_cal_days      .setText(cal_days      );
    et_cal_endtime   .setText(cal_endtime   );
    et_cal_bymonth   .setText(cal_bymonth   );
    et_cal_bymonthday.setText(cal_bymonthday);
    et_cal_byday     .setText(cal_byday     );
    et_cal_bysetpos  .setText(cal_bysetpos  );
    et_cal_byweekno  .setText(cal_byweekno  );
    et_cal_byyearday .setText(cal_byyearday );
    et_cal_wkst      .setText(cal_wkst      );
    et_cal_count     .setText(cal_count     );

    Log.d("WC", "update_debugview(): cal_id        " + cal_id        );
    Log.d("WC", "update_debugview(): cal_type      " + cal_type      );
    Log.d("WC", "update_debugview(): cal_end       " + cal_end       );
    Log.d("WC", "update_debugview(): cal_frequency " + cal_frequency );
    Log.d("WC", "update_debugview(): cal_days      " + cal_days      );
    Log.d("WC", "update_debugview(): cal_endtime   " + cal_endtime   );
    Log.d("WC", "update_debugview(): cal_bymonth   " + cal_bymonth   );
    Log.d("WC", "update_debugview(): cal_bymonthday" + cal_bymonthday);
    Log.d("WC", "update_debugview(): cal_byday     " + cal_byday     );
    Log.d("WC", "update_debugview(): cal_bysetpos  " + cal_bysetpos  );
    Log.d("WC", "update_debugview(): cal_byweekno  " + cal_byweekno  );
    Log.d("WC", "update_debugview(): cal_byyearday " + cal_byyearday );
    Log.d("WC", "update_debugview(): cal_wkst      " + cal_wkst      );
    Log.d("WC", "update_debugview(): cal_count     " + cal_count     );
  }
}