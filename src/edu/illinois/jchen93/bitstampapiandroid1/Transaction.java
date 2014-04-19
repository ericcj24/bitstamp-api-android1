package edu.illinois.jchen93.bitstampapiandroid1;

import android.os.Parcel;
import android.os.Parcelable;

/*
 * Transaction class used to bind JSON data from Transaction API
 * https://www.bitstamp.net/api/transactions/
 * example JSON data:
	{"date": "1395551812", "tid": 4151239, "price": "564.23", "amount": "0.05000000"}
*/
public class Transaction implements Parcelable{
    private String date;
    private long tid;
    private String price;
    private String amount;
    public Transaction() {
    }
    public Transaction(String date, long tid, String price, String amount) {
      this.date = date;
      this.tid = tid;
      this.price = price;
      this.amount = amount;
    }
    @Override
    public String toString() {
      return String.format("(date=%s, tid=%d, price=%s, amount=%s)", date, tid, price, amount);
    }
    
    public String getDate(){
    	return date;
    }
    public long getTid(){
    	return tid;
    }
    public String getPrice(){
    	return price;
    }
    public String getAmount(){
    	return amount;
    }
    
    // implementing parcelable
    private Transaction(Parcel in) {
        super(); 
        readFromParcel(in);
    }
    
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		// TODO Auto-generated method stub
		dest.writeString(date);
        dest.writeLong(tid);  
        dest.writeString(price);
		dest.writeString(amount);
	}
	public void readFromParcel(Parcel in) {
		date = in.readString();
		tid = in.readLong();
		price = in.readString();
		amount = in.readString();
	}
	
	public static final Parcelable.Creator<Transaction> CREATOR = new Parcelable.Creator<Transaction>() {
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };
	
}