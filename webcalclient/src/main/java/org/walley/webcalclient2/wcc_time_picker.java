package org.walley.webcalclient2;

import java.util.Calendar;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.app.DialogFragment;
//import android.support.v4.app.DialogFragment;

public class wcc_time_picker extends DialogFragment
{
  int h = 1;
  int m = 2;

  private OnTimeSetListener mTimeSetListener;

  public wcc_time_picker()
  {
    // nothing to see here, move along
  }

  public wcc_time_picker(OnTimeSetListener callback)
  {
    mTimeSetListener = (OnTimeSetListener) callback;
  }

  public void set_time (int hh, int mm)
  {
    h = hh;
    m = mm;
  }

  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
//    Calendar cal = Calendar.getInstance();

    return new TimePickerDialog(getActivity(), mTimeSetListener, h, m ,true);
  }

}

