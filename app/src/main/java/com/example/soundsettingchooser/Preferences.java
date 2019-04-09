package com.example.soundsettingchooser;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;

/**
 * Created by Febry Dwi Putra on 09/04/19.
 */
public class Preferences {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;
    private static final String PREF_NAME = BuildConfig.APPLICATION_ID;

    public Preferences(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    private static final String IS_SET_RINGTONE = "isSetRingtone";

    public String getRingtone() {
        Uri sound = RingtoneManager.getActualDefaultRingtoneUri(_context.getApplicationContext(), RingtoneManager.TYPE_ALL);
        return pref.getString(IS_SET_RINGTONE, sound.toString());
    }

    public void setIsSetRingtone(String isSetRingtone){
        editor.putString(IS_SET_RINGTONE, isSetRingtone);
        editor.apply();
    }
}
