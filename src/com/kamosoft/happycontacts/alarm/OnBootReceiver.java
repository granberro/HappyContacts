/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kamosoft.happycontacts.Constants;
import com.kamosoft.happycontacts.Log;

/**
 * Reset the alarm if necessary
 * @author tom
 *
 * @since 29 déc. 2009
 */
public class OnBootReceiver
    extends BroadcastReceiver
    implements Constants
{
    /**
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public void onReceive( Context context, Intent intent )
    {
        if ( Log.DEBUG )
        {
            Log.v( "OnBootReceiver: start onReceive()" );
        }
        AlarmService.start( context );
        if ( Log.DEBUG )
        {
            Log.v( "OnBootReceiver: end onReceive()" );
        }
    }
}
