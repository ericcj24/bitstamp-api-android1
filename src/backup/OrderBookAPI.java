package backup;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Vector;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OrderBookAPI{
	private final static String TAG="debug info";
	//{"timestamp": "1395686143", "bids": [["572.33", "0.17000000"], ["572.32", "0.17100000"],...]}

    public static Vector<Vector<Double>> HttpGetOrderBook() throws Exception {
        
    	Vector<Vector<Double>> v = new Vector<Vector<Double>>();
    	
        try {
        	String path = "https://www.bitstamp.net/api/order_book/";
           
            // The underlying HTTP connection is still held by the response object
            // to allow the response content to be streamed directly from the network socket.
            // In order to ensure correct deallocation of system resources
            // the user MUST either fully consume the response content  or abort request
            // execution by calling CloseableHttpResponse#close().


            	URL url=new URL(path);
            	HttpURLConnection c=(HttpURLConnection)url.openConnection();
            	c.setRequestMethod("GET");
            	c.setReadTimeout(15000);
            	c.connect();
               
                ObjectMapper mapper = new ObjectMapper();
                OrderBook ob = mapper.readValue(c.getInputStream(), OrderBook.class);
                long dateLong = Long.parseLong(ob.getTimestamp())*1000;
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            	String formattedDate =  sdf.format(dateLong);
                Log.i(TAG, "order book:"+formattedDate);
                
                Vector<Double> x = new Vector<Double>();
                Vector<Double> y = new Vector<Double>();
                Log.i(TAG, "order book bids size is: "+ob.getBids().size());
                for(int i=0; i<ob.getBids().size(); i++){
                	//System.out.println(" price "+ob.getBids().get(i).get(0)+" amount "+ob.getBids().get(i).get(1));
                	double a0 = Double.parseDouble(ob.getBids().get(i).get(0));
                	double a1 = Double.parseDouble(ob.getBids().get(i).get(0)) * Double.parseDouble(ob.getBids().get(i).get(1));
                	if(a0 < 1000 && a1<10000 ){
                		x.add(a0);
                		y.add(a1);
                	}
                	//x.add(Double.parseDouble(ob.getBids().get(i).get(0)));
                	//y.add(Double.parseDouble(ob.getBids().get(i).get(0)) * Double.parseDouble(ob.getBids().get(i).get(1)));
                }
                v.add(x);
                v.add(y);
                
                Log.i(TAG, "order book asks size is: "+ob.getAsks().size());
                Vector<Double> x1 = new Vector<Double>();
                Vector<Double> y1 = new Vector<Double>();
                for(int i=0; i<ob.getAsks().size(); i++){
                	//System.out.println(" price "+ob.getAsks().get(i).get(0)+" amount "+ob.getAsks().get(i).get(1));
                	double a0 = Double.parseDouble(ob.getAsks().get(i).get(0));
                	double a1 = Double.parseDouble(ob.getAsks().get(i).get(0)) * Double.parseDouble(ob.getAsks().get(i).get(1));
                	
                	if(a0 < 1000 && a1<10000 ){
                		x1.add(a0);
                		y1.add(a1);
                	}
                }
                v.add(x1);
                v.add(y1);
                
        }catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return v;
    }
}