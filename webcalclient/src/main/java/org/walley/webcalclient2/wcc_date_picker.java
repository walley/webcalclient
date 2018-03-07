package org.walley.webcalclient2;

import java.util.Calendar;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.app.DialogFragment;

public class wcc_date_picker extends DialogFragment
{

  int y;
  int m;
  int d;

  private OnDateSetListener mDateSetListener;

  public wcc_date_picker()
  {
    // nothing to see here, move along
  }

  public void set_date (int yy, int mm, int dd)
  {
    y = yy;
    m = mm;
    d = dd;
  }

  public wcc_date_picker(OnDateSetListener callback)
  {
    mDateSetListener = (OnDateSetListener) callback;

    Calendar cal = Calendar.getInstance();
    y = cal.get(Calendar.YEAR);
    m = cal.get(Calendar.MONTH);
    d = cal.get(Calendar.DAY_OF_MONTH);
  }

  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    Calendar cal = Calendar.getInstance();

    return new DatePickerDialog(getActivity(), mDateSetListener, y, m, d);
  }

}

