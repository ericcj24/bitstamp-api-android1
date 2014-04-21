package edu.illinois.jchen93.bitstampapiandroid1;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	private final static String TAG="mainActivity";
	
	
	private SlideHolder mSlideHolder;
	private XYPlot plot1;
	
	private AlarmManager alarmMgr;
	private BroadcastReceiver receiver; 
	Intent intent;
	PendingIntent pendingIntent;

	private String CHOICE = "2";
	private int REQUEST_CODE = 0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//setContentView(R.layout.activity_main);
		Button mainMenuButton1 = null;
    	Button mainMenuButton2 = null;
    	Button mainMenuButton3 = null;
    	mainMenuButton1=(Button)findViewById(R.id.btn1);
        mainMenuButton2=(Button)findViewById(R.id.btn2);
        mainMenuButton3=(Button)findViewById(R.id.btn3);
        mainMenuButton1.setOnClickListener(listener);
        mainMenuButton2.setOnClickListener(listener);
        mainMenuButton3.setOnClickListener(listener);
        
		
		mSlideHolder = (SlideHolder) findViewById(R.id.slideHolder);
		
		/*
		 * toggleView can actually be any view you want.
		 * Here, for simplicity, we're using TextView, but you can
		 * easily replace it with button.
		 * 
		 * Note, when menu opens our textView will become invisible, so
		 * it quite pointless to assign toggle-event to it. In real app
		 * consider using UP button instead. In our case toggle() can be
		 * replaced with open().
		 */
		
		View toggleView = findViewById(R.id.textView);
		//View toggleView = findViewById(R.id.plot1);
		toggleView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mSlideHolder.toggle();
			}
		});
		
		
		receiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	        	String intentAction = intent.getAction();
	        	if(intentAction == TransactionUpdateService.TRANSACTION_RESULT){
	        	
	        		ArrayList<Transaction> s = intent.getParcelableArrayListExtra(TransactionUpdateService.TRANSACTION_RESULT);
	            
	        		if (s==null){ Log.i(TAG, "broadcast received null, failed broadcast");}
	        		else{
	        			Log.i(TAG, "broadcast receive correctly, arraylist size is: "+Integer.toString(s.size()));
	        			plotTransaction(s);
	        		}
	        	}
	        	
	        	if(intentAction == OrderBookUpdateService.ORDERBOOK_RESULT){
		        	
	        		ArrayList<Price_Amount> s = intent.getParcelableArrayListExtra(OrderBookUpdateService.ORDERBOOK_RESULT);
	            
	        		if (s==null){ Log.i(TAG, "broadcast received null, failed broadcast");}
	        		else{
	        			Log.i(TAG, "orderbook broadcast receive correctly, arraylist size is: "+Integer.toString(s.size()));
	        			plotOrderBook(s);
	        		}
	        	}
	        }
	    };
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		TextView temp = (TextView)findViewById(R.id.textView);
		temp.setText("make you choice :)");
		
		LocalBroadcastManager.getInstance(this).registerReceiver((receiver), new IntentFilter(TransactionUpdateService.TRANSACTION_RESULT));
	
		Log.i(TAG, "on start");
	}
	
	private OnClickListener listener = new OnClickListener()
    {
    	public void onClick(View v){
    		Button btn=(Button)v;
    		switch (btn.getId()){
				case R.id.btn1:
					if(Integer.parseInt(CHOICE) == 1){
						//cancel trade book
						Log.i(TAG, "cancelling alarm " + CHOICE);
						pendingIntent = PendingIntent.getService(MainActivity.this, REQUEST_CODE, intent, 0);
				        alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
				        alarmMgr.cancel(pendingIntent);			        
					}
					// check isequal to same
					// check isequal to the other
					// check if empty
					// add checking for repeating press the same button!!!!!!!!!!!!!
					CHOICE = "0";
					intent = new Intent(MainActivity.this, TransactionUpdateService.class);
					intent.setData(Uri.parse(CHOICE));
					REQUEST_CODE = 101;
					pendingIntent = PendingIntent.getService(MainActivity.this, REQUEST_CODE, intent, 0);
					alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
				    alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, 2500, pendingIntent);
					break;
    			
	    		case R.id.btn2:
	    			if(Integer.parseInt(CHOICE) == 0){
						//cancel transaction
	    				Log.i(TAG, "cancel alarm "+ CHOICE);
	    				pendingIntent = PendingIntent.getService(MainActivity.this, REQUEST_CODE, intent, 0);
				        alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
				        alarmMgr.cancel(pendingIntent);
					}
	    			CHOICE = "1";
	    			intent = new Intent(MainActivity.this, OrderBookUpdateService.class);
					intent.setData(Uri.parse(CHOICE));
					REQUEST_CODE = 102;
					pendingIntent = PendingIntent.getService(MainActivity.this, REQUEST_CODE, intent, 0);
					alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
				    alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, 2500, pendingIntent);
	    			break;
	    			
	    		case R.id.btn3:
	    			if(Integer.parseInt(CHOICE) != 2){
	    				Log.i(TAG, "pressed cancel, cancel alarm "+ CHOICE);
	    				pendingIntent = PendingIntent.getService(MainActivity.this, REQUEST_CODE, intent, 0);
				        alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
				        alarmMgr.cancel(pendingIntent);
	    			}
	    			break;
    		}
    	}
    };

    @Override
    protected void onStop() {
    	super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        
        Log.i(TAG, "on stop, cancelling alarm "+ CHOICE);
        //intent = new Intent(MainActivity.this, TransactionUpdateService.class);
        //intent.setData(Uri.parse(CHOICE));
        pendingIntent = PendingIntent.getService(MainActivity.this, REQUEST_CODE, intent, 0);
        alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(pendingIntent);
    }
	
	private void plotTransaction(ArrayList<Transaction> transactionArray){
		TextView toggleView = (TextView)findViewById(R.id.textView);
		//toggleView.setText("");
        toggleView.setVisibility(0);
		plot1 = (XYPlot) findViewById(R.id.plot1);
		plot1.clear();
		
		int n = transactionArray.size();
		Log.i(TAG, "ploting transaction size is: "+n);
		Number[] time = new Number[n];
		Number[] y = new Number[n];
		int i = 0;
		for(Transaction temp : transactionArray){
			time[i] = Long.parseLong(temp.getDate());
			y[i] = Double.parseDouble(temp.getPrice());
			i++;
		}

		XYSeries series = new SimpleXYSeries(Arrays.asList(time),Arrays.asList(y),"Transactions");
		
		plot1.getGraphWidget().getGridBackgroundPaint().setColor(Color.BLACK);
        plot1.getGraphWidget().getDomainGridLinePaint().setColor(Color.WHITE);
        plot1.getGraphWidget().getDomainGridLinePaint().
                setPathEffect(new DashPathEffect(new float[]{1, 1}, 1));
        plot1.getGraphWidget().getRangeGridLinePaint().setColor(Color.WHITE);
        plot1.getGraphWidget().getRangeGridLinePaint().
                setPathEffect(new DashPathEffect(new float[]{1, 1}, 1));
        plot1.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
        plot1.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);

        // Create a formatter to use for drawing a series using LineAndPointRenderer:
        LineAndPointFormatter format = new LineAndPointFormatter(
                Color.RED,                  // line color
                Color.rgb(0, 100, 0),       // point color
                null, null);                // fill color
        
        plot1.getGraphWidget().setPaddingRight(2);
        plot1.addSeries(series, format);

        // draw a domain tick for each time:
        //plot1.setDomainStep(XYStepMode.SUBDIVIDE, time.length/400);
        plot1.setDomainStepValue(10);
        plot1.setRangeStepValue(9);

        // customize our domain/range labels
        plot1.setDomainLabel("Time");
        plot1.setRangeLabel("Price");      
        
        plot1.setDomainValueFormat(new Format() {

            // create a simple date format that draws on the year portion of our timestamp.
            // see http://download.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
            // for a full description of SimpleDateFormat.
            private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {

                // because our timestamps are in seconds and SimpleDateFormat expects milliseconds
                // we multiply our timestamp by 1000:
                long timestamp = ((Number) obj).longValue() * 1000;
                Date date = new Date(timestamp);
                return dateFormat.format(date, toAppendTo, pos);
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;

            }
        });
       
        plot1.getGraphWidget().getDomainLabelPaint().setTextSize(15);
        plot1.getGraphWidget().getRangeLabelPaint().setTextSize(15);
        //plot1.getGraphWidget().setDomainLabelOrientation(-20);
        //plot1.getGraphWidget().setRangeLabelOrientation(20);
        plot1.getGraphWidget().setPaddingRight(25);
        plot1.getGraphWidget().setPaddingTop(6);
        
        plot1.getGraphWidget().setSize(new SizeMetrics(70, SizeLayoutType.FILL, 50, SizeLayoutType.FILL));

        plot1.redraw();
        plot1.setVisibility(1);
        plot1.bringToFront();
	}

	
	private void plotOrderBook(ArrayList<Price_Amount> ob){
		TextView temp = (TextView)findViewById(R.id.textView);
		//temp.setText("");
		temp.setVisibility(0);
		plot1 = (XYPlot)findViewById(R.id.plot1);
		plot1.clear();
		
		int size = ob.size();
		int nask = Integer.parseInt(ob.get(size-1).getPrice());
		int nbid = Integer.parseInt(ob.get(size-1).getAmount());
		Number[] x1 = new Number[nask];
		Number[] y1 = new Number[nask];
		int i=0;
		for(i=0; i<nask; i++){
			x1[i] = Double.parseDouble(ob.get(i).getPrice());
			y1[i] = Double.parseDouble(ob.get(i).getAmount());
		}
		Log.i(TAG, "nask is "+nbid +"and i is "+i);
		Number[] x2 = new Number[nbid];
		Number[] y2 = new Number[nbid];
		for(int j=0; j<nbid; j++){
			x2[j] = Double.parseDouble(ob.get(i+j).getPrice());
			y2[j] = Double.parseDouble(ob.get(i+j).getAmount());
		}
		XYSeries series1 = new SimpleXYSeries(Arrays.asList(x1),Arrays.asList(y1),"Asks");
		XYSeries series2 = new SimpleXYSeries(Arrays.asList(x2),Arrays.asList(y2),"Bids");
		
		plot1.getGraphWidget().getGridBackgroundPaint().setColor(Color.BLACK);
        plot1.getGraphWidget().getDomainGridLinePaint().setColor(Color.WHITE);
        plot1.getGraphWidget().getDomainGridLinePaint().
                setPathEffect(new DashPathEffect(new float[]{1, 1}, 1));
        plot1.getGraphWidget().getRangeGridLinePaint().setColor(Color.WHITE);
        plot1.getGraphWidget().getRangeGridLinePaint().
                setPathEffect(new DashPathEffect(new float[]{1, 1}, 1));
        plot1.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
        plot1.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);

        // Create a formatter to use for drawing a series using LineAndPointRenderer:
        LineAndPointFormatter format1 = new LineAndPointFormatter(
                Color.RED,                   // line color
                Color.RED,        // point color
                Color.BLUE, null);                // fill color
        LineAndPointFormatter format2 = new LineAndPointFormatter(
                Color.YELLOW,                   // line color
                Color.YELLOW,           // point color
                Color.GREEN, null);                	// fill color
        
        plot1.getGraphWidget().setPaddingRight(2);
        plot1.addSeries(series1, format1);
        plot1.addSeries(series2, format2);

        // customize our domain/range labels
        plot1.setDomainLabel("Price");
        plot1.setRangeLabel("Value");
        
        plot1.redraw();
		plot1.setVisibility(1);
		plot1.bringToFront();
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}*/

}
