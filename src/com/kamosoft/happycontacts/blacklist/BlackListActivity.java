/**
 * 
 */
package com.kamosoft.happycontacts.blacklist;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.happycontacts.dao.HappyContactsDb;

/**
 * @author tom
 * 
 */
public class BlackListActivity
    extends ListActivity
{
    // private static String TAG = "BlackListActivity";
    private DbAdapter mDb;

    private Cursor mCursorBlakListed;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        if ( Log.DEBUG )
        {
            Log.v( "BlackListActivity: start onCreate" );
        }
        super.onCreate( savedInstanceState );
        setContentView( R.layout.blacklist );
        mDb = new DbAdapter( this );
        if ( Log.DEBUG )
        {
            Log.v( "BlackListActivity: end onCreate" );
        }
    }

    @Override
    protected void onResume()
    {
        if ( Log.DEBUG )
        {
            Log.v( "BlackListActivity: start onResume" );
        }
        super.onResume();
        mDb.open( false );
        fillList();
        if ( Log.DEBUG )
        {
            Log.v( "BlackListActivity: end onResume" );
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if ( mDb != null )
        {
            mDb.close();
        }
    }

    private void fillList()
    {
        if ( Log.DEBUG )
        {
            /* display only in debug mode to avoid user to see them
             * we display only the black listed setted direclty by the user */
            mCursorBlakListed = mDb.fetchAllBlackList();
        }
        else
        {
            mCursorBlakListed = mDb.fetchAllTimeBlackListed();
        }
        startManagingCursor( mCursorBlakListed );
        setListAdapter( new BlackListCursorAdapter( this, mCursorBlakListed ) );
    }

    @Override
    protected void onListItemClick( ListView l, View v, int position, long id )
    {
        super.onListItemClick( l, v, position, id );
        Long blackListId = mCursorBlakListed.getLong( mCursorBlakListed
            .getColumnIndexOrThrow( HappyContactsDb.BlackList.ID ) );
        mDb.deleteBlackList( blackListId );
        fillList();
    }

}
