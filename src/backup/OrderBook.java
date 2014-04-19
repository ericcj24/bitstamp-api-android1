package backup;
import java.util.ArrayList;
/*
 * Order Book class used to bind JSON data from Order Book API
 * https://www.bitstamp.net/api/order_book/
 * example JSON data:
	{"timestamp": "1395686143", "bids": [["572.33", "0.17000000"], ["572.32", "0.17100000"],...]}
*/
	
public class OrderBook {
    private String timestamp;
    private ArrayList<ArrayList<String>> bids;
    private ArrayList<ArrayList<String>> asks;
    public OrderBook() {}
    
    public String getTimestamp(){
    	return timestamp;
    }
    public ArrayList<ArrayList<String>> getBids(){
    	return bids;
    }
    public ArrayList<ArrayList<String>> getAsks(){
    	return asks;
    }
}