/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts;

import java.util.Calendar;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.dao.HappyContactsDb;

/**
 * Display date list for a name
 * @author Tom
 *
 * @since 30 dec. 2009
 */
public class DateListActivity
    extends DateNameListOptionsMenu
{
    private DbAdapter mDb;

    private DateListCursorAdapter mCursorAdapter;

    private Cursor mCursorDaysForName;

    private String mName;

    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        if ( Log.DEBUG )
        {
            Log.v( "DateListActivity: start onCreate" );
        }
        super.onCreate( savedInstanceState );
        setContentView( R.layout.datelist );

        mDb = new DbAdapter( this );

        if ( Log.DEBUG )
        {
            Log.v( "DateListActivity: end onCreate" );
        }
    }

    @Override
    protected void onResume()
    {
        if ( Log.DEBUG )
        {
            Log.v( "DateListActivity: start onResume" );
        }
        super.onResume();
        mName = getIntent().getExtras().getString( NAME_INTENT_KEY );
        mDb.open( true );
        Calendar calendar = Calendar.getInstance();
        mYear = calendar.get( Calendar.YEAR );
        mMonthOfYear = calendar.get( Calendar.MONTH );
        mDayOfMonth = calendar.get( Calendar.DAY_OF_MONTH );
        fillList();
        if ( Log.DEBUG )
        {
            Log.v( "DateListActivity: end onResume" );
        }
    }

    @Override
    protected void onStop()
    {
        if ( Log.DEBUG )
        {
            Log.v( "DateListActivity: start onStop" );
        }
        super.onStop();
        if ( mDb != null )
        {
            mDb.close();
        }
        if ( Log.DEBUG )
        {
            Log.v( "DateListActivity: end onStop" );
        }
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        if ( Log.DEBUG )
        {
            Log.v( "DateListActivity: start onRestart" );
        }
    }

    protected void fillList()
    {
        setTitle( getString( R.string.date_list_title, mName ) );
        mCursorDaysForName = mDb.fetchDayForName( mName );
        startManagingCursor( mCursorDaysForName );

        if ( mCursorAdapter == null )
        {
            mCursorAdapter = new DateListCursorAdapter( this, mCursorDaysForName );
            setListAdapter( mCursorAdapter );
        }
        else
        {
            mCursorAdapter.changeCursor( mCursorDaysForName );
        }
    }

    /**
     * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
     */
    @Override
    protected void onListItemClick( ListView l, View v, int position, long id )
    {
        super.onListItemClick( l, v, position, id );
        mCursorDaysForName.moveToPosition( position );
        Intent intent = new Intent( this, NameListActivity.class );
        intent.putExtra( DATE_INTENT_KEY,
                         mCursorDaysForName.getString( mCursorDaysForName.getColumnIndex( HappyContactsDb.Feast.DAY ) ) );
        startActivity( intent );
    }

}
