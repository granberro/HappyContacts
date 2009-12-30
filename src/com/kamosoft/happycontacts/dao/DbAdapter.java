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
        private final Context mCtx;

        DatabaseHelper( Context context )
        {
            super( context, HappyContactsDb.DATABASE_NAME, null, HappyContactsDb.DATABASE_VERSION );
            mCtx = context;
        }

        @Override
        public void onCreate( SQLiteDatabase db )
        {
            // ProgressDialog progressDialog = ProgressDialog.show(mCtx, "",
            // mCtx
            // .getString(R.string.loading_data));
            Log.v( "Creating database start..." );
            try
            {
                // get file content
                String sqlCode = AndroidUtils.getFileContent( mCtx.getResources(), R.raw.db_create );
                // execute code
                for ( String sqlStatements : sqlCode.split( ";" ) )
                {
                    db.execSQL( sqlStatements );
                }
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
            finally
            {
                // progressDialog.hide();
            }
        }

        @Override
        public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion )
        {
            // ProgressDialog progressDialog = ProgressDialog.show(mCtx, "",
            // mCtx
            // .getString(R.string.updating_data));
            Log.v( "Upgrading database from version " + oldVersion + " to " + newVersion
                + ", which will destroy all old data" );
            try
            {
                // get file content
                String sqlCode = AndroidUtils.getFileContent( mCtx.getResources(), R.raw.db_update );
                // execute code
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
            finally
            {
                // progressDialog.hide();
            }
            onCreate( db );
        }
    }

    public DbAdapter( Context ctx )
    {
        mCtx = ctx;
        mDbHelper = new DatabaseHelper( mCtx );
    }

    public DbAdapter open( boolean readOnly )
        throws SQLException
    {
        mDb = readOnly ? mDbHelper.getReadableDatabase() : mDbHelper.getWritableDatabase();
        return this;
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

            mDb.query( HappyContactsDb.Feast.TABLE_NAME, new String[] { HappyContactsDb.Feast.ID,
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
        Cursor mCursor =
            mDb.query( HappyContactsDb.Feast.TABLE_NAME, new String[] { HappyContactsDb.Feast.ID,
                HappyContactsDb.Feast.DAY }, HappyContactsDb.Feast.NAME + " like '" + name + "'", null, null, null,
                       "substr(" + HappyContactsDb.Feast.DAY + ",4,2)||substr(" + HappyContactsDb.Feast.DAY + ",1,2)",
                       null );
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
        Cursor mCursor =
            mDb.query( HappyContactsDb.BlackList.TABLE_NAME, HappyContactsDb.BlackList.COLUMNS,
                       HappyContactsDb.BlackList.CONTACT_ID + "=" + contactId, null, null, null, null, null );
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
