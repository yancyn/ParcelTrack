package com.muje.parcel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        try {
            // display value in setting if any
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

            String hour = sharedPreferences.getString("prefNotificationHour", "");
            if(hour.length() > 0) {
                EditTextPreference editText = (EditTextPreference)findPreference("prefNotificationHour");
                editText.setSummary(hour + getString(R.string.space) + getString(R.string.notification_hour));
            }

            // set contact developer
            Preference prefEmail = findPreference("prefEmail");
            prefEmail.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getResources().getString(R.string.contact_email)});
                    intent.setType("message/rfc822");//"application/octet-stream");
                    startActivity(Intent.createChooser(intent, "Sending email"));
                    return false;
                }
            });

            // set version
            Preference prefVersion = findPreference("prefVersion");
            String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            prefVersion.setSummary(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Preference pref = findPreference(s);
        if(pref instanceof EditTextPreference) {
            EditTextPreference editText = (EditTextPreference)pref;
            if(editText.getKey().equals("prefNotificationHour")) {
                editText.setSummary(editText.getText() + getString(R.string.space) + getString(R.string.notification_hour));
            }
        }
    }
}
