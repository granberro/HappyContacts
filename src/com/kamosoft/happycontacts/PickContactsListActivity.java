/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.kamosoft.utils.AndroidUtils;

/**
 * Displayed when user do a contact search from NameListActivity
 * <p><i>not used</i>
 * @author tom
 * created 11 janv. 2010
 */
public class PickContactsListActivity
    extends ListActivity
    implements Constants
{
    private Cursor mCursor;

    private AutoCompleteTextView mAutoCompleteTextView;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        if ( Log.DEBUG )
        {
            Log.v( "ContactFeastPopupActivity: start onCreate" );
        }
        super.onCreate( savedInstanceState );
        setContentView( R.layout.contactlist );
        mAutoCompleteTextView = (AutoCompleteTextView) findViewById( R.id.autocomplete );
        mAutoCompleteTextView.setThreshold( 1 );
        mAutoCompleteTextView.setCompletionHint( getString( R.string.pick_contact ) );

        fillList();

        if ( Log.DEBUG )
        {
            Log.v( "ContactFeastPopupActivity: end onCreate" );
        }
    }

    private void fillList()
    {
        String[] projection = new String[] { People._ID, People.NAME, People.DISPLAY_NAME };
        mCursor = AndroidUtils.avoidEmptyName( this.getContentResolver().query( People.CONTENT_URI, projection, null,
                                                                                null, People.NAME + " ASC" ) );

        String[] from = { People.NAME };
        int[] to = { android.R.id.text1 };
        SimpleCursorAdapter simpleAdapter = new SimpleCursorAdapter( this, android.R.layout.simple_list_item_1,
                                                                     mCursor, from, to );
        setListAdapter( simpleAdapter );       
        
        SimpleCursorAdapter dropDowCursorAdapter = new SimpleCursorAdapter(
                                                                           this,
                                                                           android.R.layout.simple_dropdown_item_1line,
                                                                           mCursor, from, to );
       mAutoCompleteTextView.setAdapter( dropDowCursorAdapter );
    }

    @Override
    protected void onListItemClick( ListView l, View v, int position, long id )
    {
        super.onListItemClick( l, v, position, id );
        Intent intent = new Intent( this, PickNameDayListActivity.class );
        mCursor.moveToPosition( position );
        intent.putExtra( CONTACTID_INTENT_KEY, mCursor.getLong( mCursor.getColumnIndex( People._ID ) ) );
        intent.putExtra( CONTACTNAME_INTENT_KEY, mCursor.getString( mCursor.getColumnIndex( People.NAME ) ) );
    }
}
