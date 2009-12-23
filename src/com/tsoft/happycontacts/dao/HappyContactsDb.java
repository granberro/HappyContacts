/**
 * 
 */
package com.tsoft.happycontacts.dao;

/**
 * @author tom
 * 
 */
public final class HappyContactsDb {
	public static String DATABASE_NAME = "happy_contacts";
	public static int DATABASE_VERSION = 20;

	public static final class Feast {
		public static String TABLE_NAME = "feast";
		public static String ID = "_id";
		public static String DAY = "day";
		public static String NAME = "name";

		public static String[] COLUMNS = { ID, DAY, NAME };
	}

	public static final class BlackList {
		public static String TABLE_NAME = "blackList";
		public static String ID = "_id";
		public static String CONTACT_ID = "contactId";
		public static String CONTACT_NAME = "contactName";
		public static String LAST_WISH_YEAR = "lastWishYear";
		public static String[] COLUMNS = { ID, CONTACT_ID, CONTACT_NAME,
				LAST_WISH_YEAR };
	}

	public static final String DB_CREATE = "insert into feast (name, day) values (\"Th�a\", \"19/12\")";
}