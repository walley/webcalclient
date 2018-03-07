package org.walley.webcalclient2;

import android.app.Activity;
import android.util.Log;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
//import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import java.util.ArrayList;
import org.json.JSONArray;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.json.JSONException;
import android.text.TextWatcher;
import android.text.Editable;
import android.text.Layout;

//http://stackoverflow.com/questions/6787071/android-fragment-how-to-save-states-of-views-in-a-fragment-when-another-fragmen
//http://stackoverflow.com/questions/15313598/once-for-all-how-to-correctly-save-instance-state-of-fragments-in-back-stack
/******************************************************************************/
/******************************************************************************/
/******************************************************************************/
public class wcc_fragment_participants extends Fragment
/******************************************************************************/
/******************************************************************************/
/******************************************************************************/
{
  View rootView;
  ArrayList<ListviewContactItem> listContact;
  ListviewContactAdapter adapter;
  EditText e_et;
  ArrayList<String> participants;
  ArrayList<String> externals;
  ArrayList<String> selected;
  ArrayAdapter<String> selected_adapter;
  ListView selected_lv;
  ListView lv;

  ArrayList<String> values = new ArrayList<String>();

  String owner;
  SharedPreferences prefs;
  String participants_json;
  String externals_json;

  on_participants_update_listener p_callback;
  on_externals_update_listener e_callback;

  /******************************************************************************/
  public interface on_participants_update_listener
  /******************************************************************************/
  {
    public void on_participants_update(ArrayList<String> arr);
  }

  /******************************************************************************/
  public interface on_externals_update_listener
  /******************************************************************************/
  {
    public void on_externals_update(ArrayList<String> arr);
  }

  /******************************************************************************/
  public void onAttach(Activity activity)
  /******************************************************************************/
  {
    super.onAttach(activity);

    // This makes sure that the container activity has implemented
    // the callback interface. If not, it throws an exception
    try {
      p_callback = (on_participants_update_listener) activity;
      e_callback = (on_externals_update_listener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement on_on_participants_update_listener");
    }

  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState)
  {
//    savedInstanceState.putStringArrayList("todo_arraylist", Altodo);
    Log.v("WC", "onSaveInstanceState !!!");
    super.onSaveInstanceState(savedInstanceState);
  }

  @Override
  public void onStop()
  {
    Log.v("WC", "onStop !!!");

    super.onStop();
  }

  @Override
  public void onDestroyView()
  {
    Log.v("WC", "ononDestroyView !!!");
    save_state();
    super.onDestroyView();
  }

  /******************************************************************************/
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  /******************************************************************************/
  {

    Log.d("WC","wcc_fragment_participants onCreateView(): start");

    rootView = inflater.inflate(R.layout.participants_layout, container, false);

    prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    owner = prefs.getString("app_username", "nonono");

    SharedPreferences saved_state = getActivity().getSharedPreferences("savedstate", 0);

    if(saved_state.getBoolean("saved", false)) {
      Log.d("WC","wcc_fragment_participants onCreateView(): jsons are saved");
      participants_json = saved_state.getString("p", "[]");
      externals_json = saved_state.getString("e", "[]");
    } else {
      Log.d("WC","wcc_fragment_participants onCreateView(): jsons are bundled");
      participants_json = getArguments().getString("p");
      externals_json = getArguments().getString("e");
    }

    if (externals_json == null || externals_json.equals("null")) {
      externals_json = "[]";
    }

    Log.d("WC","wcc_fragment_participants onCreateView(): p:" + participants_json);
    Log.d("WC","wcc_fragment_participants onCreateView(): e:" + externals_json);


    participants = get_participants_or_externals_array(participants_json);
    externals  = get_participants_or_externals_array(externals_json);

    listContact = get_available_users();

    lv = (ListView) rootView.findViewById(R.id.participants_lv);
    selected_lv = (ListView) rootView.findViewById(R.id.selected_participants_lv);

    selected_adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, participants);
    selected_lv.setAdapter(selected_adapter);

    selected_lv.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int itemPosition = position;
        String itemValue = (String) selected_lv.getItemAtPosition(position);
        Toast.makeText(getActivity(),"Click " + itemValue + position, Toast.LENGTH_LONG).show();

        participants.remove(position);

        selected_adapter.notifyDataSetChanged();
        p_callback.on_participants_update(participants);
      }
    });

    e_et = (EditText)rootView.findViewById(R.id.e_et);

    e_et.addTextChangedListener(new TextWatcher() {
      public void afterTextChanged(Editable s) {
        e_callback.on_externals_update(get_lines(e_et));
      }
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
      public void onTextChanged(CharSequence s, int start, int before, int count) {}
    });


    try {
      String s = "";
      for (int i = 0; i < externals.size(); i++) {
        s += externals.get(i) + "\n";
      }
      Log.d("WC","fill external e_et: " + s);
      e_et.setText(s);
    } catch (Exception e) {
      Log.e("WC","onCreateView(): externals_json error " + e.toString());
      e_et.setText("");
      e.printStackTrace();
    }

    adapter = new ListviewContactAdapter(getActivity(), listContact);
    lv.setAdapter(adapter);

    lv.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListviewContactItem item = (ListviewContactItem) adapter.getItem(position);
//        Toast.makeText(getActivity(),"Click " + item.GetName() + item.is_checked(), Toast.LENGTH_LONG).show();

        if (!participants.contains(item.get_login())) {
          participants.add(item.get_login());
        }

        JSONArray json = new JSONArray(participants);
        participants_json = json.toString();
        selected_adapter.notifyDataSetChanged();
        p_callback.on_participants_update(participants);
      }
    });

    adapter.recheck_participants();
    adapter.notifyDataSetChanged();

    return rootView;
  }


  /******************************************************************************/
  public void save_state()
  /******************************************************************************/
  {

    Log.d("WC","wcc_fragment_participants save_state(): p:" + participants_json);
    Log.d("WC","wcc_fragment_participants save_state(): e:" + externals_json);

    SharedPreferences.Editor saved_state = getActivity().getSharedPreferences("savedstate", 0).edit();
    saved_state.putBoolean("saved", true);
    saved_state.putString("p", participants_json);
    saved_state.putString("e", externals_json);
    saved_state.commit();
  }

  /******************************************************************************/
  public ArrayList<String> get_lines(EditText view)
  /******************************************************************************/
  {
    final ArrayList<String> lines = new ArrayList<String>();
    final Layout layout = view.getLayout();

    if (layout != null) {
      final int lineCount = layout.getLineCount();
      final CharSequence text = layout.getText();

      for (int i = 0, startIndex = 0; i < lineCount; i++) {
        final int endIndex = layout.getLineEnd(i);
        String s = text.subSequence(startIndex, endIndex).toString().trim();
        Log.d("WC","get_lines(): " + s + s.length());
        if (s.length() > 0) {
          lines.add(s);
        }
        startIndex = endIndex;
      }
    }
    return lines;
  }

  /******************************************************************************/
  public void close_db(SQLiteDatabase db)
  /******************************************************************************/
  {
    try {
      db.close();
    } catch(Exception e) {
      Log.e("WC","close_db(): error closing db" + e.toString());
      e.printStackTrace();
    }
  }
  /******************************************************************************/
  public SQLiteDatabase open_db()
  /******************************************************************************/
  {
    SQLiteDatabase db = null;
    try {
      db = getActivity().openOrCreateDatabase("cal.db", android.content.Context.MODE_PRIVATE,null);
    } catch(Exception e) {
      Log.e("WC","open_db(): error opening db" + e.toString());
      e.printStackTrace();
    }
    return db;
  }

  /******************************************************************************/
  public ArrayList<ListviewContactItem> get_available_users()
  /******************************************************************************/
  {
    SQLiteDatabase mydb;
    ArrayList<ListviewContactItem> contactlist = new ArrayList<ListviewContactItem>();

    ListviewContactItem contact = new ListviewContactItem();

    mydb = open_db();

    String q = "select login from users";

    Cursor c = mydb.rawQuery("select name,login from users", null);
    if(c.moveToFirst()) {
      do {
        String n = c.getString(0);
        String l = c.getString(1);
        Log.d("WC","get_available_users():" + l);

        contact = new ListviewContactItem();
        contact.SetName(n);//.substring(0,30));
        contact.SetPhone(l);
        if (participants.contains(l)) {
          Log.d("WC","get_available_users(): is in " + l);
        }
        contactlist.add(contact);

      } while(c.moveToNext());
    }
    c.close();
    close_db(mydb);

    contact = new ListviewContactItem();
    contact.SetName(owner);
    contact.SetPhone(owner);
    contactlist.add(contact);

    return contactlist;
  }

  /******************************************************************************/
  public ArrayList<String> get_participants_or_externals_array(String str)
  /******************************************************************************/
  {
    ArrayList<String> list = new ArrayList<String>();

    try {
      JSONArray arr = new JSONArray(str);
      for (int i = 0; i < arr.length(); i++) {
        String s = arr.getString(i);
        Log.d("WC","get_participants_or_externals_array(): " + s);
        list.add(s);
      }

    } catch (Exception e) {
      Log.e("WC","get_participants_or_externals_array(): " + e.toString());
      e.printStackTrace();
    }
    return list;
  }


  /******************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
  public class ListviewContactAdapter extends BaseAdapter
  /******************************************************************************/
  /******************************************************************************/
  /******************************************************************************/
  {
    private  ArrayList<ListviewContactItem> listContact;

    private LayoutInflater mInflater;

    public ListviewContactAdapter(Context context, ArrayList<ListviewContactItem> results)
    {
      listContact = results;
      mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount()
    {
      return listContact.size();
    }

    @Override
    public Object getItem(int i)
    {
      return listContact.get(i);
    }

    @Override
    public long getItemId(int i)
    {
      return i;
    }

    public ArrayList<String> selected_participants()
    {
      ArrayList<String> temp = new ArrayList<String>();
      for (ListviewContactItem i : listContact) {
        if (i.is_checked()) {
          temp.add(i.get_login());
        }
      }

      return temp;
    }

    public void recheck_participants()
    {
      for (ListviewContactItem i : listContact) {
      }
    }

    /******************************************************************************/
    public View getView(int position, View convertView, ViewGroup parent)
    /******************************************************************************/
    {
      final ViewHolder holder;

      if(convertView == null) {
        convertView = mInflater.inflate(R.layout.checklist_adapter, null);
        holder = new ViewHolder();
        holder.txtname = (TextView) convertView.findViewById(R.id.name);
        holder.txtphone = (TextView) convertView.findViewById(R.id.name2);
//        holder.checkbox = (CheckBox) convertView.findViewById(R.id.check_box);

        /*        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                  @Override
                  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ListviewContactItem element = (ListviewContactItem) holder.checkbox.getTag();
                    element.check_it(isChecked);

                    p_callback.on_participants_update(selected_participants());

                  }
                });
        */
        convertView.setTag(holder);
//        holder.checkbox.setTag(listContact.get(position));
      } else {
        holder = (ViewHolder) convertView.getTag();
      }

      holder.txtname.setText(listContact.get(position).GetName());
      holder.txtphone.setText(listContact.get(position).get_login());
//      holder.checkbox.setChecked(listContact.get(position).is_checked());

      return convertView;
    }

    public class ViewHolder
    {
      TextView txtname;
      TextView txtphone;
//      CheckBox checkbox;
    }
  }

  public class ListviewContactItem
  {
    String name;
    String phone;
    boolean checked;

    public ListviewContactItem()
    {
      name = "";
      phone = "";
      checked = false;
    }

    void SetName(String s)
    {
      name = s;
    }

    boolean is_checked()
    {
      return checked;
    }

/*    void check_it()
    {
      checked = true;
    }

    void check_it(boolean x)
    {
      checked = x;
    }
*/
    void SetPhone(String s)
    {
      phone = s;
    }

    String GetName()
    {
      return name;
    }

    String get_login()
    {
      return phone;
    }
  }
}
