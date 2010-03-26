/**
 * Copyright (C) Kamosoft 2010
 */
package com.kamosoft.happycontacts;

public class Log
{
    public final static String LOGTAG = "HappyContacts";

    public static final boolean DEBUG = true;

    public static void v( String msg )
    {
        android.util.Log.v( LOGTAG, msg );
    }

    public static void d( String msg )
    {
        android.util.Log.d( LOGTAG, msg );
    }

    public static void e( String msg )
    {
        android.util.Log.e( LOGTAG, msg );
    }

    public static void e( String msg, Throwable tr )
    {
        android.util.Log.e( LOGTAG, msg, tr );
    }
}