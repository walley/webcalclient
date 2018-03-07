package org.walley.webcalclient2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import android.preference.PreferenceManager;

public class wcc_sql extends Activity
{

  LinearLayout Linear;
  SQLiteDatabase mydb;
  private static String DBNAME = "cal.db";    // THIS IS THE SQLITE DATABASE FILE NAME.
  private static String TABLE = "calendar";       // THIS IS THE TABLE NAME
  Context context;
  SharedPreferences prefs;
  TextView t;

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
    "end"
  };

  private String sql_create_event = ""
                                    + "create table event"
                                    + "("
                                    + "dbid              INTEGER PRIMARY KEY,"
                                    + "starttimestamp  integer,"
                                    + "location        text,"
                                    + "category_number integer,"
                                    + "label           text,"
                                    + "timestr         text,"
                                    + "access          character,"
                                    + "type            character,"
                                    + "endtimestamp    integer,"
                                    + "id              integer,"
                                    + "duration        integer,"
                                    + "time            integer,"
                                    + "title           text,"
                                    + "category_name   text ,"
                                    + "start           integer,"
                                    + "description     text,"
                                    + "priority        integer,"
                                    + "name            text,"
                                    + "linkid          text,"
                                    + "end             integer"
                                    + ");";

  /******************************************************************************/
  @Override public void onCreate(Bundle savedInstanceState)
  /******************************************************************************/
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.sql);
    context = this;
    Linear  = (LinearLayout)findViewById(R.id.linear);

    t = new TextView(this);
    Linear.removeAllViews();
    Linear.addView(t);

    show_query_results("select * from views");
    show_query_results("select * from users");
    show_query_results("select * from event");
  }


  /******************************************************************************/
  public void sql()
  /******************************************************************************/
  {
    TextView t0 = new TextView(this);
    t0.setText("This tutorial covers CREATION, UPDATION AND DELETION USING SQLITE DATABAS  Creating table complete........");
    Linear.addView(t0);
    Toast.makeText(getApplicationContext(), "Creating table complete.", Toast.LENGTH_SHORT).show();
    insertIntoTable();
    TextView t1 = new TextView(this);
    t1.setText("Insert into table complete........");
    Linear.addView(t1);
    Toast.makeText(getApplicationContext(), "Insert into table complete", Toast.LENGTH_SHORT).show();
    TextView t2 = new TextView(this);
    t2.setText("Showing table values............");
    Linear.addView(t2);
    Toast.makeText(getApplicationContext(), "Showing table values", Toast.LENGTH_SHORT).show();
    updateTable();
    TextView t3 = new TextView(this);
    t3.setText("Updating table values............");
    Linear.addView(t3);
    Toast.makeText(getApplicationContext(), "Updating table values", Toast.LENGTH_SHORT).show();
    TextView t4 = new TextView(this);
    t4.setText("Showing table values after updation..........");
    Linear.addView(t4);
    Toast.makeText(getApplicationContext(), "Showing table values after updation.", Toast.LENGTH_SHORT).show();
    deleteValues();
    TextView t5 = new TextView(this);
    t5.setText("Deleting table values..........");
    Linear.addView(t5);
    Toast.makeText(getApplicationContext(), "Deleting table values", Toast.LENGTH_SHORT).show();
    TextView t6 = new TextView(this);
    t6.setText("Showing table values after deletion.........");
    Linear.addView(t6);
    Toast.makeText(getApplicationContext(), "Showing table values after deletion.", Toast.LENGTH_SHORT).show();
    setColor(t0);
    setColor(t1);
    setColor(t2);
    setColor(t3);
    setColor(t4);
    setColor(t5);
    setColor(t6);
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
      Log.d("WC","create_table:" + sql_create_event);
      mydb.execSQL(sql_create_event);
      mydb.close();
    } catch(Exception e) {
      Toast.makeText(getApplicationContext(), "Error in creating table", Toast.LENGTH_LONG);
    }
  }

  // THIS FUNCTION INSERTS DATA TO THE DATABASE
  /******************************************************************************/
  public void insertIntoTable()
  /******************************************************************************/
  {
    try {
      mydb = openOrCreateDatabase(DBNAME, Context.MODE_PRIVATE,null);
      mydb.execSQL("INSERT INTO " + TABLE + "(NAME, PLACE) VALUES('CODERZHEAVEN','GREAT INDIA')");
      mydb.execSQL("INSERT INTO " + TABLE + "(NAME, PLACE) VALUES('ANTHONY','USA')");
      mydb.execSQL("INSERT INTO " + TABLE + "(NAME, PLACE) VALUES('SHUING','JAPAN')");
      mydb.execSQL("INSERT INTO " + TABLE + "(NAME, PLACE) VALUES('JAMES','INDIA')");
      mydb.execSQL("INSERT INTO " + TABLE + "(NAME, PLACE) VALUES('SOORYA','INDIA')");
      mydb.execSQL("INSERT INTO " + TABLE + "(NAME, PLACE) VALUES('MALIK','INDIA')");
      mydb.close();
    } catch(Exception e) {
      Toast.makeText(getApplicationContext(), "Error in inserting into table", Toast.LENGTH_LONG);
    }
  }

  // THIS FUNCTION SHOWS DATA FROM THE DATABASE
  /******************************************************************************/
  public void show_query_results(String query)
  /******************************************************************************/
  {
    int ci;
    try {
      mydb = openOrCreateDatabase(DBNAME, Context.MODE_PRIVATE,null);

//      String query = "SELECT * FROM " + TABLE;
      Cursor query_result  = mydb.rawQuery(query, null);

      String[] header = query_result.getColumnNames();

      System.out.println("COUNT : " + query_result.getCount());
      System.out.println("Column COUNT : " + query_result.getColumnCount());
      System.out.println("Array COUNT : " + header.length);
//      Integer cindex = query_result.getColumnIndex("endtimestamp");
//      Integer cindex1 = query_result.getColumnIndex("starttimestamp");


      String s = query;
      t.append(s+"\n");

      t.append("\n--- HEADER ---\n");
      if(query_result.moveToFirst()) {
        for (int i = 0; i < header.length; i++) {
          t.append(header[i]+" ");
        }
        t.append("\n--- VALUES ---\n");

        do {
          t.append("#" + query_result.getPosition() + ":");
          for (int i = 0; i < header.length; i++) {
            ci = query_result.getColumnIndex(header[i]);

            t.append(" ");

            s = query_result.getString(i);
            if (s != null) {
              t.append(s);
            } else {
              t.append("null");
            }
            t.append("|");
          }

          t.append("\n");
        } while(query_result.moveToNext());
      }
      mydb.close();
    } catch(Exception e) {
      Log.d("WC","error: " + e.toString());
      e.printStackTrace();
    }
  }

  // THIS FUNCTION UPDATES THE DATABASE ACCORDING TO THE CONDITION
  /******************************************************************************/
  public void updateTable()
  /******************************************************************************/
  {
    try {
      mydb = openOrCreateDatabase(DBNAME, Context.MODE_PRIVATE,null);
      mydb.execSQL("UPDATE " + TABLE + " SET NAME = 'MAX' WHERE PLACE = 'USA'");
      mydb.close();
    } catch(Exception e) {
      Toast.makeText(getApplicationContext(), "Error encountered", Toast.LENGTH_LONG);
    }
  }
  // THIS FUNCTION DELETES VALUES FROM THE DATABASE ACCORDING TO THE CONDITION
  /******************************************************************************/
  public void deleteValues()
  /******************************************************************************/
  {
    try {
      mydb = openOrCreateDatabase(DBNAME, Context.MODE_PRIVATE,null);
      mydb.execSQL("DELETE FROM " + TABLE + " WHERE PLACE = 'USA'");
      mydb.close();
    } catch(Exception e) {
      Toast.makeText(getApplicationContext(), "Error encountered while deleting.", Toast.LENGTH_LONG);
    }
  }

  /******************************************************************************/
  public void drop_table()
  /******************************************************************************/
  {
    try {
      mydb = openOrCreateDatabase(DBNAME, Context.MODE_PRIVATE,null);
      mydb.execSQL("drop table event");
      mydb.close();
    } catch(Exception e) {
      Toast.makeText(getApplicationContext(), "Error encountered while dropping.", Toast.LENGTH_LONG);
    }
  }



}



