package edu.illinois.jchen93.bitstampapiandroid1;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Price_Amount implements Parcelable{
	static final String TAG = "price_amount implements parcelable";
	
    private String price;
    private String amount;
    
    public Price_Amount() {}
    
    public Price_Amount(String price, String amount) {
    	this.price = price;
    	this.amount = amount;
    }
    
    public String getPrice(){
    	return price;
    }
    public String getAmount(){
    	return amount;
    }

 // implementing parcelable
    public Price_Amount(Parcel in) {
        //super(); 
        //readFromParcel(in);
    	price= in.readString();
		amount = in.readString();
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
        dest.writeString(price);
		dest.writeString(amount);
		Log.i(TAG,"3");
	}
	
	public static final Parcelable.Creator<Price_Amount> CREATOR = new Parcelable.Creator<Price_Amount>() {
        public Price_Amount createFromParcel(Parcel in) {
        	Log.i(TAG,"4");
            return new Price_Amount(in);
        }

        public Price_Amount[] newArray(int size) {
        	Log.i(TAG,"5");
            return new Price_Amount[size];
        }
    };
}