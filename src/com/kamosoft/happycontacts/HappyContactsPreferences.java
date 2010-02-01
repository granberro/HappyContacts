/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import com.kamosoft.happycontacts.alarm.AlarmController;
import com.kamosoft.happycontacts.blacklist.BlackListActivity;
import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.template.MailTemplateActivity;
import com.kamosoft.happycontacts.template.SmsTemplateActivity;
import com.kamosoft.utils.AndroidUtils;
import com.kamosoft.utils.ProgressDialogHandler;

/**
 * @author tom
 * 
 */
public class HappyContactsPreferences
    extends PreferenceActivity
    implements Constants, TimePickerDialog.OnTimeSetListener
{
    private static final int TIME_DIALOG_ID = 0;

    /**
     * alarm click action
     */
    private OnPreferenceClickListener mAlarmToggleClickListener = new OnPreferenceClickListener()
    {
        public boolean onPreferenceClick( Preference preference )
        {
            boolean isChecked = ( (CheckBoxPreference) preference ).isChecked();
            mPrefs.edit().putBoolean( PREF_ALARM_STATUS, isChecked ).commit();
            if ( Log.DEBUG )
            {
                Log.v( "HappyContactsPreferences : alarm checked = " + isChecked );
            }
            if ( isChecked )
            {
                AlarmController.startAlarm( HappyContactsPreferences.this );
                preference.setSummary( R.string.pref_alarm_on );
            }
            else
            {
                AlarmController.cancelAlarm( HappyContactsPreferences.this );
                preference.setSummary( R.string.pref_alarm_off );
            }
            return true;
        }
    };

    private SharedPreferences mPrefs;

    private int mAlarmHour;

    private int mAlarmMinute;

    private Preference mAlarmTimePref;

    private CheckBoxPreference mAlarmStatePref;

    /**
     * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
     */
    @Override
    protected void onPrepareDialog( int id, Dialog dialog )
    {
        super.onPrepareDialog( id, dialog );
        switch ( id )
        {
            case TIME_DIALOG_ID:
                TimePickerDialog timePicker = (TimePickerDialog) dialog;
                timePicker.updateTime( mAlarmHour, mAlarmMinute );
                break;
        }
    }

    /**
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog( int id )
    {
        Dialog dialog;
        switch ( id )
        {
            case TIME_DIALOG_ID:
                dialog =
                    new TimePickerDialog( HappyContactsPreferences.this, this, 0, 0, DateFormat.is24HourFormat( this ) );
                break;
            default:
                dialog = null;
        }
        return dialog;
    }

    /**
     * @see android.app.TimePickerDialog.OnTimeSetListener#onTimeSet(android.widget.TimePicker, int, int)
     */
    @Override
    public void onTimeSet( TimePicker view, int hourOfDay, int minute )
    {
        mAlarmHour = hourOfDay;
        mAlarmMinute = minute;
        /* record prefs */
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt( PREF_ALARM_HOUR, mAlarmHour );
        editor.putInt( PREF_ALARM_MINUTE, mAlarmMinute );
        editor.commit();
        mAlarmTimePref.setSummary( getString( R.string.pref_time_summary, AndroidUtils.pad( mAlarmHour, mAlarmMinute ) ) );

        /* start the alarm */
        mAlarmStatePref.setSummary( R.string.pref_alarm_on );
        mAlarmStatePref.setChecked( true );
        mPrefs.edit().putBoolean( PREF_ALARM_STATUS, true ).commit();
        AlarmController.startAlarm( HappyContactsPreferences.this );
    }

    /**
     * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        if ( Log.DEBUG )
        {
            Log.v( "HappyContactsPreferences: start onCreate()" );
        }

        mPrefs = getSharedPreferences( APP_NAME, 0 );
        checkInit();
        mAlarmHour = mPrefs.getInt( PREF_ALARM_HOUR, AlarmController.DEFAULT_ALARM_HOUR );
        mAlarmMinute = mPrefs.getInt( PREF_ALARM_MINUTE, AlarmController.DEFAULT_ALARM_MINUTE );

        setPreferenceScreen( createPreferenceHierarchy() );
        if ( Log.DEBUG )
        {
            Log.v( "HappyContactsPreferences: end onCreate()" );
        }
    }

    /**
     * check if some initializations have to be done
     */
    private void checkInit()
    {
        if ( !mPrefs.contains( PREF_FIRST_RUN ) )
        {
            if ( Log.DEBUG )
            {
                Log.v( "HappyContactsPreferences: first run detected" );
            }
            /* if its the first run, alarm must be set */
            Editor editor = mPrefs.edit();
            editor.putBoolean( PREF_FIRST_RUN, false );
            editor.putBoolean( PREF_ALARM_STATUS, true );
            editor.commit();
            AlarmController.startAlarm( this );
        }

        final ProgressDialog progressDialog = new ProgressDialog( this );
        progressDialog.setTitle( R.string.please_wait );
        progressDialog.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
        progressDialog.setMessage( this.getString( R.string.loading_data ) );
        progressDialog.setCancelable( false );
        /* check if need to create or upgrade */
        DbAdapter.createOrUpdate( this, new ProgressDialogHandler( progressDialog ) );
    }

    /**
     * @return the built preferences
     */
    private PreferenceScreen createPreferenceHierarchy()
    {
        // Root
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen( this );

        /* the alarm state */
        mAlarmStatePref = new CheckBoxPreference( this );
        mAlarmStatePref.setTitle( R.string.pref_alarm );

        if ( mPrefs.getBoolean( PREF_ALARM_STATUS, false ) )
        {
            mAlarmStatePref.setSummary( R.string.pref_alarm_on );
            mAlarmStatePref.setChecked( true );
            if ( !AlarmController.isAlarmUp( this ) )
            {
                Log.e( "HappyContactsPreferences : error alarm disabled" );
            }
        }
        else
        {
            mAlarmStatePref.setSummary( R.string.pref_alarm_off );
            mAlarmStatePref.setChecked( false );
            if ( AlarmController.isAlarmUp( this ) )
            {
                Log.e( "HappyContactsPreferences : error alarm enabled" );
            }
        }
        mAlarmStatePref.setOnPreferenceClickListener( mAlarmToggleClickListener );
        root.addPreference( mAlarmStatePref );

        Preference alarmTimePref = new Preference( this );
        mAlarmTimePref = alarmTimePref;
        alarmTimePref.setTitle( R.string.pref_time );
        alarmTimePref.setSummary( getString( R.string.pref_time_summary, AndroidUtils.pad( mAlarmHour, mAlarmMinute ) ) );
        alarmTimePref.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener()
        {
            public boolean onPreferenceClick( Preference preference )
            {
                showDialog( TIME_DIALOG_ID );
                return true;
            }
        } );
        root.addItemFromInflater( alarmTimePref );

        /* database category */
        PreferenceCategory databasePrefCat = new PreferenceCategory( this );
        databasePrefCat.setTitle( R.string.pref_data_cat );
        root.addPreference( databasePrefCat );

        /* check todays name days */
        Preference checkNameDayPref = new Preference( this );
        checkNameDayPref.setTitle( R.string.pref_check_name_days );
        checkNameDayPref.setSummary( R.string.pref_check_name_days_summary );
        checkNameDayPref.setOnPreferenceClickListener( new OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick( Preference arg0 )
            {
                DayMatcherService.displayDayMatch( HappyContactsPreferences.this );
                return true;
            }
        } );
        databasePrefCat.addItemFromInflater( checkNameDayPref );

        /* name days list */
        Preference nameListPref = new Preference( this );
        nameListPref.setTitle( R.string.pref_feast_list );
        nameListPref.setSummary( R.string.pref_feast_list_summary );
        Intent intent = new Intent( this, NameListActivity.class );
        SimpleDateFormat dateFormat = new SimpleDateFormat( DAY_FORMAT );
        intent.putExtra( DATE_INTENT_KEY, dateFormat.format( new Date() ) );
        nameListPref.setIntent( intent );
        databasePrefCat.addPreference( nameListPref );

        // blacklist
        Preference blackListPref = new Preference( this );
        blackListPref.setTitle( R.string.pref_blacklist );
        blackListPref.setSummary( R.string.pref_blacklist_summary );
        blackListPref.setIntent( new Intent( this, BlackListActivity.class ) );
        databasePrefCat.addPreference( blackListPref );

        /* templates */
        PreferenceCategory templatesPrefCat = new PreferenceCategory( this );
        templatesPrefCat.setTitle( R.string.pref_templates_cat );
        root.addPreference( templatesPrefCat );

        Preference templateSmsPref = new Preference( this );
        //templateSmsPref.setKey( "templateSmsPref" );
        templateSmsPref.setTitle( R.string.pref_template_sms );
        templateSmsPref.setSummary( R.string.pref_template_sms_summary );
        templateSmsPref.setIntent( new Intent( this, SmsTemplateActivity.class ) );
        templatesPrefCat.addPreference( templateSmsPref );

        Preference templateEmailPref = new Preference( this );
        //templateEmailPref.setKey( "templateEmailPref" );
        templateEmailPref.setTitle( R.string.pref_template_email );
        templateEmailPref.setSummary( R.string.pref_template_email_summary );
        templateEmailPref.setIntent( new Intent( this, MailTemplateActivity.class ) );
        templatesPrefCat.addPreference( templateEmailPref );

        /* about preference */
        PreferenceCategory aboutPrefCat = new PreferenceCategory( this );
        aboutPrefCat.setTitle( R.string.about );
        root.addPreference( aboutPrefCat );
        Preference aboutPref = new Preference( this );
        aboutPrefCat.addPreference( aboutPref );
        aboutPref.setTitle( R.string.about );
        aboutPref.setOnPreferenceClickListener( new OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick( Preference arg0 )
            {
                new AboutDialog( HappyContactsPreferences.this ).show();
                return true;
            }
        } );

        return root;
    }
}
