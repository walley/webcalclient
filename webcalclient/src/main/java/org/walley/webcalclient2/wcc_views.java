package org.walley.webcalclient2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

  String class_name = this.getClass().getSimpleName();

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

        Log.d("WC",class_name + "onitemclick: view id " + view_id);

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
    Cursor query_result = null;

    try {
      query_result  = mydb.rawQuery(query, null);

      if(query_result.moveToFirst()) {

        do {
          desc = query_result.getString(1);
          id = query_result.getString(5);
          hash.put(desc, id);
          Log.d("WC",class_name + "get_views " + desc + " " + id);
        } while(query_result.moveToNext());

      }
    } catch(Exception e) {
      Log.d("WC",class_name + "get_views error:" + e.toString());
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
        result = LayoutInflater.from(parent.getContext()).inflate(R.layout.views_adapter, parent, false);
      } else {
        result = convertView;
      }

      Map.Entry<String, String> item = getItem(position);

      ((TextView) result.findViewById(android.R.id.text1)).setText(item.getKey());
      ((TextView) result.findViewById(android.R.id.text2)).setText(item.getValue());

      if (position % 2 == 1) {
        result.setBackgroundColor(Color.BLUE);
      } else {
        result.setBackgroundColor(Color.CYAN);
      }

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
