package org.walley.webcalclient2;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

class wcc_u
{

  /******************************************************************************/
  public long get_timestamp_from_date(int day, int month, int year)
  /******************************************************************************/
  {
    String str_date = month + "-" + day + "-" + year;
    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

    Date date = null;
    try {
      date = (Date)sdf.parse(str_date);
    } catch (ParseException e) {
      Log.e("WC","get_timestamp_from_date(): " + e.toString());
    }
    long output = date.getTime()/1000L;
    String str = Long.toString(output);
    long timestamp = Long.parseLong(str); // * 1000;
    Log.d("WC","get_timestamp_from_date(): timestamp, d/m/y: " + timestamp +","+ day +"/"+ month +"/"+ year);
    return timestamp;
  }

  /******************************************************************************/
  public void get_date_from_timestamp(long ts)
  /******************************************************************************/
  {
  }
}

