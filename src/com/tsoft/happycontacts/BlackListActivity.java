/**
 * 
 */
package com.tsoft.happycontacts;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.tsoft.happycontacts.dao.DbAdapter;
import com.tsoft.happycontacts.dao.HappyContactsDb;

/**
 * @author tom
 * 
 */
public class BlackListActivity
    extends ListActivity
{
  // private static String TAG = "BlackListActivity";
  private DbAdapter mDb;
  private Cursor c;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.blacklist);
    mDb = new DbAdapter(this);
    mDb.open();
    fillList();
  }

  @Override
  protected void onStop()
  {
    super.onStop();
    if (c != null)
    {
      c.close();
    }
    if (mDb != null)
    {
      mDb.close();
    }
  }

  private void fillList()
  {
    c = mDb.fetchAllBlackList();
    String[] from = new String[] { HappyContactsDb.BlackList.CONTACT_NAME,
        HappyContactsDb.BlackList.LAST_WISH_YEAR };
    //    int[] to = new int[] { R.id.contact_name, R.id.last_wish_year };
    //    SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this,
    //        R.layout.blacklist_element, c, from, to);
    int[] to = new int[] { android.R.id.text1, android.R.id.text2 };
    SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this,
        android.R.layout.simple_list_item_2, c, from, to);
    setListAdapter(simpleCursorAdapter);
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id)
  {
    super.onListItemClick(l, v, position, id);
    Long blackListId = c.getLong(c.getColumnIndexOrThrow(HappyContactsDb.BlackList.ID));
    mDb.deleteBlackList(blackListId);
    fillList();
  }

}
