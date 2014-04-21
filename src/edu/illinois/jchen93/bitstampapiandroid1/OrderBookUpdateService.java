package edu.illinois.jchen93.bitstampapiandroid1;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

// too many data, query database toooooo slow, consider trim the data upon receiving it

public class OrderBookUpdateService extends IntentService{
	static final String TAG = "OrderBookUpdateService";
	
	static final String TPATH = "https://www.bitstamp.net/api/order_book/";
	static final String KEY_ID = "_id";
	static final String KEY_TIMESTAMP = "timestamp";
	static final String KEY_KIND = "kind"; // bid or ask
	static final String KEY_PRICE = "price";
	static final String KEY_AMOUNT = "amount";
	static final String ORDERBOOK_TABLE_NAME = "orderbook";
	static final String NAME = "edu.illinois.jchen93.bitstampapiandroid1.OrderBookUpdateService";
	static final public String ORDERBOOK_RESULT = "edu.illinois.jchen93.bitstampapiandroid1.OrderBookUpdateService.PROCESSED";
	static boolean isFirst = true;
	
	private LocalBroadcastManager localBroadcaster = LocalBroadcastManager.getInstance(this);
	/**
	   * A constructor is required, and must call the super IntentService(String)
	   * constructor with a name for the worker thread.
	   */
	public OrderBookUpdateService() {
		super("OrderBookUpdateService");
	}
	
	
	@Override
    protected void onHandleIntent(Intent workIntent) {
		
		// Gets data from the incoming Intent
        String dataString = workIntent.getDataString();
        int choice = Integer.parseInt(dataString);
        
        switch (choice){
        	case 0:
        		
        		break;
        	case 1:
        		int orderbookCount = fetchOrderBook();
        		Log.i(TAG, "new order book count: " + Integer.toString(orderbookCount));
        		if(orderbookCount>0 || isFirst){
        			isFirst = false;
        			/*
        			 * new tradebook, database changed!
        		     * Creates a new Intent containing a Uri object
        		     * BROADCAST_ACTION is a custom Intent action
        		     */
        			// in the last postion of arraylist, contains a pair of string descripe the size of asks, bids
        			ArrayList<Price_Amount> newOrderBookList = fetchOrderBookFromDatabase();
        			Log.i(TAG, "fetched size from database "+Integer.toString(newOrderBookList.size()));
        			
        		    Intent localIntent =
        		            new Intent(ORDERBOOK_RESULT)
        		            // Puts the status into the Intent
        		            .putParcelableArrayListExtra(ORDERBOOK_RESULT, newOrderBookList);
        		    // Broadcasts the Intent to receivers in main UI thread.
        		    localBroadcaster.sendBroadcast(localIntent);
        		}
        		break;
        	default:
        		break;
        
        }
	}
	
	
	private int fetchOrderBook(){
		int count = 0;
        
        try {
        	URL url=new URL(TPATH);
            HttpURLConnection c=(HttpURLConnection)url.openConnection();
            c.setRequestMethod("GET");
        	c.setReadTimeout(15000);
        	c.connect();
            
        	int responseCode = c.getResponseCode();
        	Log.i(TAG, "order book response code: " + Integer.toString(responseCode));
        	if (responseCode == 200){
        		ObjectMapper mapper = new ObjectMapper();
                OrderBook ob = mapper.readValue(c.getInputStream(), OrderBook.class);
                
                long dateLong = Long.parseLong(ob.getTimestamp())*1000;
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            	String formattedDate =  sdf.format(dateLong);
                //Log.i(TAG, "order book's constructor worked! timestamp:"+formattedDate);
                
            	// trim your data here!!!!
            	
        		count = addNewOrderBook(ob);
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
	
	private int addNewOrderBook(OrderBook orderBook){
		int count = 0;
		
		OrderBookDatabaseHelper tDbHelper = OrderBookDatabaseHelper.getInstance(this);
		SQLiteDatabase db = tDbHelper.getWritableDatabase();
		
	
		// cursor check the newest entry so far prior to update		
		String sortOrder = null;
		String[] projection = {KEY_TIMESTAMP};
		String selection = null;
		String[] selectionArgs = null;
		Cursor cursor = db.query(ORDERBOOK_TABLE_NAME,
							projection,                               // The columns to return
							selection,                                // The columns for the WHERE clause
						    selectionArgs,                            // The values for the WHERE clause
						    null,                                     // don't group the rows
						    null,                                     // don't filter by row groups
						    sortOrder                                 // The sort order
							);
		try{
			// limit database size by deleting order rows
			int size = cursor.getCount();
			Log.i(TAG, "database size: " + size);
			
			Long databaseTimestamp = (long) 0;
			if(size>0){
				cursor.moveToFirst();
				databaseTimestamp = cursor.getLong(cursor.getColumnIndex(KEY_TIMESTAMP));
				// shrinking the database if too big??
				//Log.i(TAG, "databasetimestamp is: " + databaseTimestamp);
			}
			
			String timestamp = orderBook.getTimestamp();
			Long timeNow = Long.parseLong(timestamp);
			if(timeNow > databaseTimestamp){
				// new timestamp
				// for loop go through two arraylist
				int askSize = orderBook.getAsks().size();
				int bidSize = orderBook.getBids().size();
				for(ArrayList<String> temp : orderBook.getAsks()){
					ContentValues values = new ContentValues();
					values.put(KEY_TIMESTAMP, timeNow);
					values.put(KEY_KIND, "ASK");
					values.put(KEY_PRICE, temp.get(0));
					values.put(KEY_AMOUNT, temp.get(1));
					db.insert(ORDERBOOK_TABLE_NAME, null, values);
					count++;
				}
				
				for(ArrayList<String> temp : orderBook.getBids()){
					ContentValues values = new ContentValues();
					values.put(KEY_TIMESTAMP, timeNow);
					values.put(KEY_KIND, "BID");
					values.put(KEY_PRICE, temp.get(0));
					values.put(KEY_AMOUNT, temp.get(1));
					db.insert(ORDERBOOK_TABLE_NAME, null, values);
					count++;
				}
			}						
		}finally{
			cursor.close();
		}
		return count;
	}
	
	private ArrayList<Price_Amount> fetchOrderBookFromDatabase(){
		OrderBookDatabaseHelper tDbHelper = OrderBookDatabaseHelper.getInstance(this);
		SQLiteDatabase db = tDbHelper.getWritableDatabase();
		
		
		// Define a projection that specifies which columns from the database
		// you will actually use after this query.
		
		//peek into the first one, get TIMESTAMP, then pull all the same timestamp + ask
		// pull all the same timestamp + bid
		
		String[] projection = {KEY_TIMESTAMP, KEY_KIND, KEY_PRICE, KEY_AMOUNT};
		String selection = KEY_TIMESTAMP + "= (SELECT MAX("+ KEY_TIMESTAMP + "));";
		String[] selectionArgs = null;
		String sortOrder = KEY_KIND + " DESC";
		Cursor cursor = db.query(ORDERBOOK_TABLE_NAME,
							projection,                               // The columns to return
							selection,                                // The columns for the WHERE clause
						    selectionArgs,                            // The values for the WHERE clause
						    null,                                     // don't group the rows
						    null,                                     // don't filter by row groups
						    sortOrder                                 // The sort order
							);
		
		ArrayList<Price_Amount> rt = new ArrayList<Price_Amount>();
		try{
			cursor.moveToFirst();
			int sizeAsks = 0;
			int sizeBids = 0;
			
			while (cursor.isAfterLast() == false){
				String type = cursor.getString(cursor.getColumnIndex(KEY_KIND));
				//Log.i(TAG, "type is: "+type);
				if(type == "ASK"){
					sizeAsks++;
				}
				if(type == "BID"){
					sizeBids++;
				}
				Price_Amount temp = new Price_Amount(cursor.getString(cursor.getColumnIndex(KEY_PRICE)),
													 cursor.getString(cursor.getColumnIndex(KEY_AMOUNT)));
				rt.add(temp);
				cursor.moveToNext();
			}
			Price_Amount temp = new Price_Amount(String.valueOf(sizeAsks), String.valueOf(sizeBids));
			rt.add(temp);
		}finally{
			cursor.close();
		}
		return rt;
	}
	
	
}