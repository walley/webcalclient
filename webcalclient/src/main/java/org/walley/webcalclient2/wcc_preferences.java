package org.walley.webcalclient2;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class wcc_preferences extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
