package edu.illinois.jchen93.bitstampapiandroid1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TransactionDatabaseHelper extends SQLiteOpenHelper{
	
	private static final String TAG = TransactionDatabaseHelper.class.getSimpleName();

	private static TransactionDatabaseHelper mInstance = null;
	
	public static final String KEY_ID = "_id";
	public static final String KEY_TID = "tid";
	public static final String KEY_DATE = "date";
	public static final String KEY_PRICE = "price";
	public static final String KEY_AMOUNT = "amount";
	
	private static final String TRANSACTION_TABLE_NAME = "transactions";
	private static final String DATABASE_DELETE =
		    "DROP TABLE IF EXISTS " + TRANSACTION_TABLE_NAME;
	private static final String DATABASE_CREATE = 
			"CREATE TABLE " + TRANSACTION_TABLE_NAME + " ("
			+ KEY_ID + " INTEGER PRIMARY KEY,"
			+ KEY_TID + " LONG, "
			+ KEY_DATE + " STRING, "
			+ KEY_PRICE + " STRING, "
			+ KEY_AMOUNT + " STRING);";
  
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "Transaction.db";
	
	public TransactionDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
	
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }
    
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
    	Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL(DATABASE_DELETE);
        onCreate(db);
    }
    
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    } 

    public static TransactionDatabaseHelper getInstance(Context ctx) {

        // Use the application context, which will ensure that you 
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
          mInstance = new TransactionDatabaseHelper(ctx.getApplicationContext());
        }
        return mInstance;
      }

}
