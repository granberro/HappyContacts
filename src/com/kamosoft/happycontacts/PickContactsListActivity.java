/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.kamosoft.happycontacts.contacts.ContactUtils;

/**
 * Allow user to select a contact in order to link it with a nameday (usefull for nickname)
 * @author tom
 * created 11 janv. 2010
 */
public class PickContactsListActivity
    extends ListActivity
    implements Constants, TextWatcher
{
    private Cursor mCursor;

    private EditText mEditText;

    private Class<?> mNextActivity;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        if ( Log.DEBUG )
        {
            Log.v( "PickContactsListActivity: start onCreate" );
        }
        super.onCreate( savedInstanceState );
        setContentView( R.layout.contactlist );
        mEditText = (EditText) findViewById( R.id.autocomplete );
        mEditText.addTextChangedListener( this );

        mNextActivity = (Class<?>) getIntent().getExtras().getSerializable( NEXT_ACTIVITY_INTENT_KEY );
        String pickContactLabel = getIntent().getExtras().getString( PICK_CONTACT_LABEL_INTENT_KEY );
        TextView textView = (TextView) findViewById( R.id.pick_contact_label );
        textView.setText( pickContactLabel );
        fillList( null );

        if ( Log.DEBUG )
        {
            Log.v( "PickContactsListActivity: end onCreate" );
        }
    }

    private void fillList( String text )
    {
        if ( text == null || text.length() == 0 )
        {
            mCursor = ContactUtils.doQuery( this, ContactUtils.getNameColumn() + " is not null" );
        }
        else
        {
            mCursor = ContactUtils.doQuery( this, ContactUtils.getNameColumn() + " like \"" + text + "%\"" );
        }
        startManagingCursor( mCursor );
        String[] from = { ContactUtils.getNameColumn() };
        int[] to = { android.R.id.text1 };
        SimpleCursorAdapter simpleAdapter =
            new SimpleCursorAdapter( this, android.R.layout.simple_list_item_1, mCursor, from, to );
        setListAdapter( simpleAdapter );
    }

    @Override
    protected void onListItemClick( ListView l, View v, int position, long id )
    {
        super.onListItemClick( l, v, position, id );
        onContactClick( position );
    }

    private void onContactClick( int position )
    {
        mCursor.moveToPosition( position );
        Intent intent = new Intent( this, mNextActivity );
        intent.putExtra( CONTACTID_INTENT_KEY, mCursor.getLong( mCursor.getColumnIndex( ContactUtils.getIdColumn() ) ) );
        intent.putExtra( CONTACTNAME_INTENT_KEY,
                         mCursor.getString( mCursor.getColumnIndex( ContactUtils.getNameColumn() ) ) );
        if ( getIntent().hasExtra( CALLED_FOR_RESULT_INTENT_KEY ) )
        {
            setResult( Activity.RESULT_OK, intent );
            finish();
        }
        else
        {
            startActivity( intent );
        }
    }

    /**
     * @see android.text.TextWatcher#afterTextChanged(android.text.Editable)
     */
    public void afterTextChanged( Editable arg0 )
    {
        //nothing
    }

    /**
     * @see android.text.TextWatcher#beforeTextChanged(java.lang.CharSequence, int, int, int)
     */
    public void beforeTextChanged( CharSequence arg0, int arg1, int arg2, int arg3 )
    {
        //nothing
    }

    /**
     * @see android.text.TextWatcher#onTextChanged(java.lang.CharSequence, int, int, int)
     */
    public void onTextChanged( CharSequence arg0, int arg1, int arg2, int arg3 )
    {
        String text = mEditText.getText().toString();
        fillList( text );
    }

}
