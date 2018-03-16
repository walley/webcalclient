package org.walley.webcalclient2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
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
import android.os.AsyncTask;
import android.util.Base64;
import org.apache.http.conn.HttpHostConnectException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.Intent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import android.widget.BaseAdapter;
import java.util.Map;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public class wcc_views extends wcc_activity
{

  Context context;
  ListView listview;
  StableArrayAdapter adapter;
  MyAdapter myadapter;
  SharedPreferences prefs;
  String login;
  HashMap<String, String> hash = new HashMap<String, String>();

  HashMap<String, String> hashMap = new HashMap<String, String>();
  ArrayList<HashMap<String, String>> ulist;

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
        String view_id = item.getValue();
        Intent i = new Intent();
        i.setClass(wcc_views.this,wcc_webview.class);
        i.putExtra("view_id", view_id);

        Log.d("WC","wcc_views onitemclick: view id " + view_id);

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
    setContentView(R.layout.views);

    create_ui();
    get_views();

  }

  /******************************************************************************/
  public void get_views()
  /******************************************************************************/
  {
    SQLiteDatabase mydb = null;
    String query;
    query  = "select * from views";
    mydb = open_db();
    String desc;
    String id;
    Cursor query_result;

    try {
      query_result  = mydb.rawQuery(query, null);

      if(query_result.moveToFirst()) {

        do {
          desc = query_result.getString(1);
          id = query_result.getString(5);
          hash.put(desc, id);
          Log.d("WC","wcc_views get_views " + desc + " " + id);
        } while(query_result.moveToNext());

      }
    } catch(Exception e) {
      Log.d("WC","wcc_views get_views " + e.toString());
      e.printStackTrace();
    } finally {
      if(query_result != null){
        query_result.close();
      }
    }

    myadapter = new MyAdapter(hash);
    listview.setAdapter(myadapter);

    close_db(mydb);

  }

  /******************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
  public class MyAdapter extends BaseAdapter
  /******************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
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


  /*****************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
  private class StableArrayAdapter extends ArrayAdapter<String>
  /******************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
  {

    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

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
