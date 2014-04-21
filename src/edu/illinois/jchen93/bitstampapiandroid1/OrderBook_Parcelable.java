package edu.illinois.jchen93.bitstampapiandroid1;
import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
	
/*
 * Order Book class used to bind JSON data from Order Book API
 * https://www.bitstamp.net/api/order_book/
 * example JSON data:
	{"timestamp": "1395686143", "bids": [["572.33", "0.17000000"], ["572.32", "0.17100000"],...]}
*/

public class OrderBook_Parcelable implements Parcelable{
	static final String TAG = "order_book implements parcelable";
	
	
    private String timestamp;
    private ArrayList<Price_Amount> bids;
    private ArrayList<Price_Amount> asks;
    
    public OrderBook_Parcelable() {}
    
    public String getTimestamp(){
    	return timestamp;
    }
    public ArrayList<Price_Amount> getBids(){
    	return bids;
    }
    public ArrayList<Price_Amount> getAsks(){
    	return asks;
    }
    
    public void setTimestamp(String timestamp){
    	this.timestamp = timestamp;
    }
    public void getBids(ArrayList<Price_Amount> bids){
    	this.bids = bids;
    }
    public void setAsks(ArrayList<Price_Amount> asks){
    	this.asks = asks;
    }
    
 // implementing parcelable
    public OrderBook_Parcelable(Parcel in) {
        this(); 
        
    	this.timestamp = in.readString();
    	in.readTypedList(bids, Price_Amount.CREATOR);
		in.readTypedList(asks, Price_Amount.CREATOR);
		Log.i(TAG,"1");
    }
    
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		Log.i(TAG,"2");
		return 0;		
	}
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		// TODO Auto-generated method stub 
        dest.writeString(timestamp);
		dest.writeTypedList(asks);
		dest.writeTypedList(bids);
		Log.i(TAG,"3");
	}
	
	public static final Parcelable.Creator<OrderBook_Parcelable> CREATOR = new Parcelable.Creator<OrderBook_Parcelable>() {
        public OrderBook_Parcelable createFromParcel(Parcel in) {
        	Log.i(TAG,"4");
            return new OrderBook_Parcelable(in);
        }

        public OrderBook_Parcelable[] newArray(int size) {
        	Log.i(TAG,"5");
            return new OrderBook_Parcelable[size];
        }
    };
}