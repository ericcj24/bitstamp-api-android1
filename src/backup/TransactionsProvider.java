package backup;

import java.util.HashMap;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;


public class TransactionsProvider extends ContentProvider{
	public static final Uri CONTENT_URI = Uri.parse("content://edu.illinois.jchen93.transactionsprovider");
	
	//column names
	public static final String KEY_ID = "_id";
	public static final String KEY_TID = "tid";
	public static final String KEY_DATE = "date";
    public static final String KEY_PRICE = "price";
    public static final String KEY_AMOUNT = "amount";
    
    TransactionDatabaseHelper dbHelper;
    
    @Override
    public boolean onCreate(){
    	Context context = getContext();
    	
    	dbHelper = new TransactionDatabaseHelper(context, 
    			TransactionDatabaseHelper.DATABASE_NAME, null, 
    			TransactionDatabaseHelper.DATABASE_VERSION);
    	
    	return true;
    }
    
    private static final HashMap<String, String> SEARCH_PROJECTION_MAP;
    static {
    	SEARCH_PROJECTION_MAP = new HashMap<String, String>();
    	SEARCH_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_1, KEY_TID +
    			" AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
    	SEARCH_PROJECTION_MAP.put("_id", KEY_ID +
    			" AS " + "_id");
    }
    
    //Create the constants used to differentiate between the different URI
    //requests.
    private static final int TRANSACTIONS = 1;
    private static final int TRANSACTION_ID = 2;
    private static final int SEARCH = 3;
    
    private static final UriMatcher uriMatcher;
    
	//Allocate the UriMatcher object, where a URI ending in 'transactions' will
	//correspond to a request for all transactions, and 'transactions' with a
	//trailing '/[rowID]' will represent a single transaction row.
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI("edu.illinois.jchen93.transactionsprovider", "transactions", TRANSACTIONS);
		uriMatcher.addURI("edu.illinois.jchen93.transactionsprovider", "transactions/#", TRANSACTION_ID);
		uriMatcher.addURI("edu.illinois.jchen93.transactionsprovider", SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH);
		uriMatcher.addURI("edu.illinois.jchen93.transactionsprovider", SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH);
		uriMatcher.addURI("edu.illinois.jchen93.transactionsprovider", SearchManager.SUGGEST_URI_PATH_SHORTCUT, SEARCH);
		uriMatcher.addURI("edu.illinois.jchen93.transactionsprovider", SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", SEARCH);
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
			case TRANSACTIONS  : return "vnd.android.cursor.dir/vnd.illinois.transaction";
			case TRANSACTION_ID: return "vnd.android.cursor.item/vnd.illinois.transaction";
			case SEARCH  : return SearchManager.SUGGEST_MIME_TYPE;
			default: throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}
    
    @Override
    public Cursor query(Uri uri,
    					String[] projection,
    					String selection,
    					String[] selectionArgs,
    					String sort){
    	SQLiteDatabase database = dbHelper.getWritableDatabase();
    	SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    	
    	qb.setTables(TransactionDatabaseHelper.TRANSACTION_TABLE);
    	
    	// if this is a row query, limit the result set to the passed in row
    	switch(uriMatcher.match(uri)){
    		case TRANSACTION_ID: 
    			qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
    			break;
    		case SEARCH:
    			qb.appendWhere(KEY_TID + " LIKE \"%" + uri.getPathSegments().get(1) + "%\"");
    			qb.setProjectionMap(SEARCH_PROJECTION_MAP);
    			break;
    		default:
    			break;
    	}
    	
    	// if no sort order is specified, sort by date/time
    	String orderBy;
    	if(TextUtils.isEmpty(sort)){
    		orderBy = KEY_DATE;
    	}else{
    		orderBy = sort;
    	}
    	
    	// apply the query to the underlying database
    	Cursor c = qb.query(database, projection, selection, selectionArgs, null, null, orderBy);
    	
    	// register the contexts ContentResolver to be notified if the cursor result set changes
    	c.setNotificationUri(getContext().getContentResolver(), uri);
    	
    	// return a cursor to the query result
    	return c;
    }
    
	@Override
	public Uri insert(Uri _uri, ContentValues _initialValues){
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		
		// insert new row, the call to database.insert will return the row number if it is successful
		long rowID = database.insert(TransactionDatabaseHelper.TRANSACTION_TABLE, "transaction", _initialValues);
		
		// return a URI to the newly inserted row on success
		if(rowID >0){
			Uri uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}
		
		throw new SQLException("Faild to insert row into "+_uri);
	}
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs){
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		
		int count;
		switch (uriMatcher.match(uri)){
			case TRANSACTIONS:
				count = database.delete(TransactionDatabaseHelper.TRANSACTION_TABLE, where, whereArgs);
				break;
			case TRANSACTION_ID:
				String segment = uri.getPathSegments().get(1);
				count = database.delete(TransactionDatabaseHelper.TRANSACTION_TABLE,
						KEY_ID + "="
						+segment
						+(!TextUtils.isEmpty(where)? " AND ("
						+where + ')' : ""), whereArgs);
				break;
			default: throw new IllegalArgumentException("Unsupported URI: "+uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs){
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		
		int count;
		switch(uriMatcher.match(uri)){
			case TRANSACTIONS:
				count = database.update(TransactionDatabaseHelper.TRANSACTION_TABLE, values, where, whereArgs);
				break;
			case TRANSACTION_ID:
				String segment = uri.getPathSegments().get(1);
				count = database.update(TransactionDatabaseHelper.TRANSACTION_TABLE,
										values, KEY_ID
										+ "=" + segment
										+ (!TextUtils.isEmpty(where)? " AND ("
										+ where + ')' : ""), whereArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI "+uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
       
    
    //helper class for opening, creating, and managing database version control
    private static class TransactionDatabaseHelper extends SQLiteOpenHelper{
    	private static final String TAG = "TransactionsProvider";
    	
    	private static final String DATABASE_NAME = "Transactions.db";
    	private static final int DATABASE_VERSION = 1;
    	private static final String TRANSACTION_TABLE = "transactions";
    	
    	private static final String DATABASE_CREATE = 
    			"create table" + TRANSACTION_TABLE + " ("
    			+ KEY_ID + " integer primary key, "
    			+ KEY_TID + " INTEGER, "
    			+ KEY_DATE + " STRING, "
    			+ KEY_PRICE + " STRING, "
    			+ KEY_AMOUNT + "STRING);";
    	
    	//the underlying database
    	private SQLiteDatabase transactionDB;
    	
    	public TransactionDatabaseHelper(Context context, String name, CursorFactory factory, int version){
    		super(context, name, factory, version);
    	}
    	
    	@Override
    	public void onCreate(SQLiteDatabase db){
    		db.execSQL(DATABASE_CREATE);
    	}
    	
    	@Override
    	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
    		db.execSQL("DROP TABLE IF EXISTS "+TRANSACTION_TABLE);
    		onCreate(db);
    	}
    }
    
}
