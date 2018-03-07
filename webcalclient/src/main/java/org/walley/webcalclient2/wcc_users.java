package org.walley.webcalclient2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.json.JSONArray;
import org.json.JSONObject;

public class wcc_users extends wcc_activity
{

  Context context;
  ListView listview;
  StableArrayAdapter adapter;
  MyAdapter myadapter;
  SharedPreferences prefs;
  String login;
  LinkedHashMap<String, String> hash = new LinkedHashMap<String, String>();

  LinkedHashMap<String, String> LinkedHashMap = new LinkedHashMap<String, String>();
  ArrayList<LinkedHashMap<String, String>> ulist;

  /******************************************************************************/
  public void create_ui()
  /******************************************************************************/
  {
    prefs = PreferenceManager.getDefaultSharedPreferences(this);
    listview = (ListView) findViewById(R.id.listview);

    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
        Map.Entry<String, String> item = myadapter.getItem(position);
        String login = item.getKey();
        Intent i = new Intent();
        i.setClass(wcc_users.this,wcc_ecv.class);
        i.putExtra("user", login);
        startActivity(i);
      }
    });
  }

  /******************************************************************************/
  @Override
  protected void onCreate(Bundle savedInstanceState)
  /******************************************************************************/
  {

    String str_result = null;
    context = this;

    super.onCreate(savedInstanceState);
    setContentView(R.layout.users);

    create_ui();
    get_users2();

  }

  public void get_users2()
  {
    SQLiteDatabase mydb = null;
    String query;
    query  = "select * from users order by name";
    mydb = open_db();
    String l;
    String u;

    try {
      Cursor query_result  = mydb.rawQuery(query, null);

      if(query_result.moveToFirst()) {

        do {
          l = query_result.getString(1);
          u = query_result.getString(2);
          hash.put(l, u);
          Log.d("WC","get_users2 " + l + u);
        } while(query_result.moveToNext());

      }
    } catch(Exception e) {
      Log.d("WC","get_users2 " + e.toString());
      e.printStackTrace();
    }

    myadapter = new MyAdapter(hash);
    listview.setAdapter(myadapter);

    close_db(mydb);

  }

  public class MyAdapter extends BaseAdapter
  {
    private final ArrayList mData;

    public MyAdapter(Map<String, String> map)
    {
      mData = new ArrayList();
      mData.addAll(map.entrySet());
    }

    @Override
    public int getCount()
    {
      return mData.size();
    }

    @Override
    public Map.Entry<String, String> getItem(int position)
    {
      return (Map.Entry) mData.get(position);
    }

    @Override
    public long getItemId(int position)
    {
      // TODO implement you own logic with ID
      return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
      final View result;

      if (convertView == null) {
        result = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_adapter, parent, false);
      } else {
        result = convertView;
      }

      Map.Entry<String, String> item = getItem(position);

      // TODO replace findViewById by ViewHolder
      ((TextView) result.findViewById(android.R.id.text1)).setText(item.getKey());
      ((TextView) result.findViewById(android.R.id.text2)).setText(item.getValue());

      return result;
    }
  }

  /******************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
  private class StableArrayAdapter extends ArrayAdapter<String>
  /******************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
  {

    LinkedHashMap<String, Integer> mIdMap = new LinkedHashMap<String, Integer>();

    public StableArrayAdapter(Context context, int textViewResourceId, List<String> objects)
    {
      super(context, textViewResourceId, objects);
      for (int i = 0; i < objects.size(); ++i) {
        mIdMap.put(objects.get(i), i);
      }
    }

    @Override
    public long getItemId(int position)
    {
      String item = getItem(position);
      return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds()
    {
      return true;
    }

  }

}
