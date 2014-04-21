package edu.illinois.jchen93.bitstampapiandroid1;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


public class TransactionUpdateService extends IntentService{
	static final String TAG = "TransactionUpdateService";
	
	static final String TPATH = "https://www.bitstamp.net/api/transactions/";
	static final String KEY_ID = "_id";
	static final String KEY_TID = "tid";
	static final String KEY_DATE = "date";
	static final String KEY_PRICE = "price";
	static final String KEY_AMOUNT = "amount";
	static final String TRANSACTION_TABLE_NAME = "transactions";
	static final String NAME = "edu.illinois.jchen93.bitstampapiandroid1.TransactionUpdateService";
	static final public String TRANSACTION_RESULT = "edu.illinois.jchen93.bitstampapiandroid1.TransactionUpdateService.PROCESSED";
	static boolean isFirst = true;
	
	private LocalBroadcastManager localBroadcaster = LocalBroadcastManager.getInstance(this);
	/**
	   * A constructor is required, and must call the super IntentService(String)
	   * constructor with a name for the worker thread.
	   */
	public TransactionUpdateService() {
		super("TransactionUpdateService");
	}
	
	
	@Override
    protected void onHandleIntent(Intent workIntent) {
		
		// Gets data from the incoming Intent
        String dataString = workIntent.getDataString();
        int choice = Integer.parseInt(dataString);
        
        switch (choice){
        	case 0:
        		int transactionCount = fetchTransactions();
        		Log.i(TAG, "new transaction count: " + Integer.toString(transactionCount));
        		if(transactionCount>0 || isFirst){
        			isFirst = false;
        			/*
        			 * new transaction, database changed!
        		     * Creates a new Intent containing a Uri object
        		     * BROADCAST_ACTION is a custom Intent action
        		     */
        			ArrayList<Transaction> newTransaction = fetchTransactionFromDatabase();
        			Log.i(TAG, "fetched size from database "+Integer.toString(newTransaction.size()));
        			
        		    Intent localIntent =
        		            new Intent(TRANSACTION_RESULT)
        		            // Puts the status into the Intent
        		            .putParcelableArrayListExtra(TRANSACTION_RESULT, newTransaction);
        		    // Broadcasts the Intent to receivers in this app.
        		    localBroadcaster.sendBroadcast(localIntent);
        		}
        		break;
        	case 1:
        		
        		break;
        	default:
        		break;
        
        }
	}
	
	
	private int fetchTransactions(){
		int count = 0;
        
        try {
        	URL url=new URL(TPATH);
            HttpURLConnection c=(HttpURLConnection)url.openConnection();
            c.setRequestMethod("GET");
        	c.setReadTimeout(15000);
        	c.connect();
            
        	int responseCode = c.getResponseCode();
        	Log.i(TAG, "response code: " + Integer.toString(responseCode));
        	if (responseCode == 200){
        		ObjectMapper mapper = new ObjectMapper();
        		List<Transaction> transactionList = mapper.readValue(c.getInputStream(), new TypeReference<ArrayList<Transaction>>() { });
            
        		count = addNewTransaction(transactionList);
        	}
            
        }catch(java.net.ConnectException e){
        	Log.e(TAG, e.toString());        	
        }catch(java.net.UnknownHostException e){
        	Log.e(TAG, e.toString());
        }catch (Exception e) {
			// TODO Auto-generated catch block
        	Log.e(TAG, e.toString());
		}finally{
			//c.disconnect();
		}
        
		return count;
	}
	
	private int addNewTransaction(List<Transaction> lt){
		int count = 0;
		
		TransactionDatabaseHelper tDbHelper = TransactionDatabaseHelper.getInstance(this);
		SQLiteDatabase db = tDbHelper.getWritableDatabase();
		
		Log.i(TAG, "internet size: " + lt.size());
	
		// cursor check the newest entry so far prior to update		
		String sortOrder = KEY_TID + " DESC";
		String[] projection = {KEY_TID};
		String selection = null;
		String[] selectionArgs = null;
		Cursor c = db.query(TRANSACTION_TABLE_NAME,
							projection,                               // The columns to return
							selection,                                // The columns for the WHERE clause
						    selectionArgs,                            // The values for the WHERE clause
						    null,                                     // don't group the rows
						    null,                                     // don't filter by row groups
						    sortOrder                                 // The sort order
							);
		try{
			// limit database size by deleting order rows
			int size = c.getCount();
			Log.i(TAG, "database size: " + size);
			
			int databaseTid = 0;
			if(size>0){
				c.moveToFirst();
				databaseTid = c.getInt(c.getColumnIndex(KEY_TID));
				// shrinking the database if too big
				
			}
			
			for (Transaction temp : lt){
				// if the transaction is new, insert it into the provider
				//Log.i(TAG, "tempTid: " + temp.getTid());
				
				if(temp.getTid() > databaseTid){
					ContentValues values = new ContentValues();
					values.put(KEY_TID, temp.getTid());
					values.put(KEY_DATE, temp.getDate());
					values.put(KEY_PRICE, temp.getPrice());
					values.put(KEY_AMOUNT, temp.getAmount());
					db.insert(TRANSACTION_TABLE_NAME, null, values);
					count++;
				}
			}			
		}finally{
			c.close();
		}
		return count;
	}
	
	private ArrayList<Transaction> fetchTransactionFromDatabase(){
		TransactionDatabaseHelper tDbHelper = TransactionDatabaseHelper.getInstance(this);
		SQLiteDatabase db = tDbHelper.getWritableDatabase();
		
		// Define a projection that specifies which columns from the database
		// you will actually use after this query.	
		
		String[] projection = {KEY_TID, KEY_DATE, KEY_PRICE, KEY_AMOUNT};
		String selection = null;
		String[] selectionArgs = null;
		String sortOrder = KEY_TID + " DESC";
		Cursor c = db.query(TRANSACTION_TABLE_NAME,
							projection,                               // The columns to return
							selection,                                // The columns for the WHERE clause
						    selectionArgs,                            // The values for the WHERE clause
						    null,                                     // don't group the rows
						    null,                                     // don't filter by row groups
						    sortOrder                                 // The sort order
							);
		
		ArrayList<Transaction> rt = new ArrayList<Transaction>();
		try{
			c.moveToFirst();
			
			while (c.isAfterLast() == false){
				Transaction temp = new Transaction(c.getString(c.getColumnIndex(KEY_DATE)),
													c.getInt(c.getColumnIndex(KEY_TID)),
													c.getString(c.getColumnIndex(KEY_PRICE)),
													c.getString(c.getColumnIndex(KEY_AMOUNT)));
				rt.add(temp);
				c.moveToNext();
			}
		}finally{
			c.close();
		}

		return rt;
	}
	
	private int fetchTradeBook(){
		int count = 0;
		
		return count;
	}
	
	
}