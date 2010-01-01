/**
 * 
 */
package com.kamosoft.happycontacts.dao;

import java.io.IOException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.kamosoft.happycontacts.Log;
import com.kamosoft.happycontacts.R;
import com.kamosoft.utils.AndroidUtils;

/**
 * @author tom
 * 
 */
public class DbAdapter
{
    private DatabaseHelper mDbHelper;

    private SQLiteDatabase mDb;

    private final Context mCtx;

    private static class DatabaseHelper
        extends SQLiteOpenHelper
    {
        private final Context mContext;

        /**
         * Handler for updating a progress dialog while 
         * creating or updating the database
         */
        private Handler mHandler;

        DatabaseHelper( Context context, Handler handler )
        {
            super( context, HappyContactsDb.DATABASE_NAME, null, HappyContactsDb.DATABASE_VERSION );
            mContext = context;
            mHandler = handler;
        }

        public boolean needUpgrade()
        {
            SQLiteDatabase db = mContext.openOrCreateDatabase( HappyContactsDb.DATABASE_NAME, 0, null );
            boolean needUpgrade = HappyContactsDb.DATABASE_VERSION > db.getVersion();
            db.close();
            return needUpgrade;
        }

        @Override
        public void onCreate( SQLiteDatabase db )
        {
            Log.v( "Creating database start..." );

            try
            {
                /* get file content */
                String sqlCode = AndroidUtils.getFileContent( mContext.getResources(), R.raw.db_create );
                /* parsing sql */
                String[] sqlStatements = sqlCode.split( ";" );
                int nbStatements = sqlStatements.length;
                Handler handler = mHandler;
                int lastPercent = 0;
                /* execute code */
                for ( int i = 0; i < nbStatements; i++ )
                {
                    db.execSQL( sqlStatements[i] );

                    /* update handler */
                    int percent = (int) ( ( i / (float) nbStatements ) * 100 );
                    if ( percent > lastPercent )
                    {
                        Log.v( "sending handler " + percent );
                        Message msg = handler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putInt( "percent", percent );
                        msg.setData( bundle );
                        handler.sendMessage( msg );
                        lastPercent = percent;
                    }
                }
                /* send last message to the handler */
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putInt( "percent", 100 );
                msg.setData( bundle );
                handler.sendMessage( msg );
                Log.v( "Creating database done." );
            }
            catch ( IOException e )
            {
                // Should never happen!
                Log.e( "Error reading sql file " + e.getMessage(), e );
                throw new RuntimeException( e );
            }
            catch ( SQLException e )
            {
                Log.e( "Error executing sql code " + e.getMessage(), e );
                throw new RuntimeException( e );
            }
        }

        @Override
        public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion )
        {
            Log.v( "Upgrading database from version " + oldVersion + " to " + newVersion
                + ", which will destroy all old data" );
            try
            {
                /* get file content */
                String sqlCode = AndroidUtils.getFileContent( mContext.getResources(), R.raw.db_update );
                /* execute code */
                for ( String sqlStatements : sqlCode.split( ";" ) )
                {
                    db.execSQL( sqlStatements );
                }
                Log.v( "Updating database done." );
            }
            catch ( IOException e )
            {
                // Should never happen!
                Log.e( "Error reading sql file " + e.getMessage() );
                throw new RuntimeException( e );
            }
            catch ( SQLException e )
            {
                Log.e( "Error executing sql code " + e.getMessage() );
                throw new RuntimeException( e );
            }
            onCreate( db );
        }
    }

    public DbAdapter( Context ctx )
    {
        mCtx = ctx;
        mDbHelper = new DatabaseHelper( mCtx, null );
    }

    /**
     * Create or update the database in a thread, in order to allow to display a progress bar
     * @param context
     * @param handler
     * @param checkUpgrade
     */
    public static void createOrUpdate( final Context context, final Handler handler, final boolean checkUpgrade )
    {
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                /* a simple call to getReadableDatabase() proceed to db create or upgrade */
                DatabaseHelper db = new DatabaseHelper( context, handler );
                if ( checkUpgrade && !db.needUpgrade() )
                {
                    /* no need to upgrade */
                    Message msg = handler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putInt( "percent", -1 );
                    msg.setData( bundle );
                    handler.sendMessage( msg );
                    return;
                }
                db.getReadableDatabase();
                db.close();
            }
        };
        thread.start();
    }

    public DbAdapter open( boolean readOnly )
        throws SQLException
    {
        mDb = readOnly ? mDbHelper.getReadableDatabase() : mDbHelper.getWritableDatabase();
        return this;
    }

    public boolean needUpgrade()
    {
        return mDbHelper.needUpgrade();
    }

    public void close()
    {
        mDbHelper.close();
    }

    /**
     * Retourne les lignes pour le jour donné et qui n'ont pas encore ete
     * souhaite
     * 
     * @param day
     *            format dd/MM
     * @return
     */
    public Cursor fetchNamesForDay( String day )
    {
        Cursor mCursor =

        mDb.query( HappyContactsDb.Feast.TABLE_NAME, new String[] {
            HappyContactsDb.Feast.ID,
            HappyContactsDb.Feast.NAME }, HappyContactsDb.Feast.DAY + "='" + day + "'", null, null, null,
                   HappyContactsDb.Feast.NAME, null );
        // mDb.query(HappyContactsDb.Feast.TABLE_NAME, new String[] {
        // HappyContactsDb.Feast.ID,
        // HappyContactsDb.Feast.NAME, HappyContactsDb.Feast.LAST_WISH_YEAR },
        // HappyContactsDb.Feast.DAY + "='" + day + "' and " +
        // HappyContactsDb.Feast.LAST_WISH_YEAR
        // + " != '" + year + "'", null, null, null, null, null);
        if ( mCursor != null )
        {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor fetchDayForName( String name )
    {
        Cursor mCursor = mDb.query( HappyContactsDb.Feast.TABLE_NAME, new String[] {
            HappyContactsDb.Feast.ID,
            HappyContactsDb.Feast.DAY }, HappyContactsDb.Feast.NAME + " like '" + name + "'", null, null, null,
                                    "substr(" + HappyContactsDb.Feast.DAY + ",4,2)||substr("
                                        + HappyContactsDb.Feast.DAY + ",1,2)", null );
        if ( mCursor != null )
        {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /**
     * @param contactId
     * @param contactName
     * @param date
     * @return
     */
    private boolean insertBlackList( long contactId, String contactName, String date )
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put( HappyContactsDb.BlackList.CONTACT_ID, contactId );
        initialValues.put( HappyContactsDb.BlackList.CONTACT_NAME, contactName );
        if ( date != null )
        {
            initialValues.put( HappyContactsDb.BlackList.LAST_WISH_DATE, date );
        }

        return mDb.insert( HappyContactsDb.BlackList.TABLE_NAME, null, initialValues ) > 0;
    }

    public boolean deleteBlackList( long id )
    {
        return mDb.delete( HappyContactsDb.BlackList.TABLE_NAME, HappyContactsDb.BlackList.ID + "=" + id, null ) > 0;
    }

    /**
     * @return all lines
     */
    public Cursor fetchAllBlackList()
    {

        return mDb.query( HappyContactsDb.BlackList.TABLE_NAME, HappyContactsDb.BlackList.COLUMNS, null, null, null,
                          null, null );
    }

    /**
     * @return only the lines with lastWishDate=null, meaning the contact is black listed all the time.
     */
    public Cursor fetchAllTimeBlackListed()
    {
        return mDb.query( HappyContactsDb.BlackList.TABLE_NAME, HappyContactsDb.BlackList.COLUMNS,
                          HappyContactsDb.BlackList.LAST_WISH_DATE + " = null", null, null, null, null );
    }

    public Cursor fetchBlackList( long contactId )
        throws SQLException
    {
        Cursor mCursor = mDb.query( HappyContactsDb.BlackList.TABLE_NAME, HappyContactsDb.BlackList.COLUMNS,
                                    HappyContactsDb.BlackList.CONTACT_ID + "=" + contactId, null, null, null, null,
                                    null );
        if ( mCursor != null )
        {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean isBlackListed( long contactId, String date )
        throws SQLException
    {
        Cursor c = fetchBlackList( contactId );
        if ( c == null )
        {
            return false;
        }
        if ( c.getCount() == 0 )
        {
            c.close();
            return false;
        }
        if ( date != null )
        {
            /* check if its black listed for this year only */
            String lastWishedDate = c.getString( c.getColumnIndexOrThrow( HappyContactsDb.BlackList.LAST_WISH_DATE ) );
            c.close();
            return ( lastWishedDate == null || lastWishedDate.equals( date ) );
        }
        c.close();
        return true;
    }

    public boolean updateContactFeast( long contactId, String contactName, String date )
    {
        if ( Log.DEBUG )
        {
            Log.v( "start updateContactFeast for contact " + contactName + " with date " + date );
        }
        if ( isBlackListed( contactId, null ) )
        {
            ContentValues args = new ContentValues();
            args.put( HappyContactsDb.BlackList.LAST_WISH_DATE, date );
            return mDb.update( HappyContactsDb.BlackList.TABLE_NAME, args, HappyContactsDb.BlackList.CONTACT_ID + "="
                + contactId, null ) > 0;
        }
        else
        {
            return insertBlackList( contactId, contactName, date );
        }
    }
}
