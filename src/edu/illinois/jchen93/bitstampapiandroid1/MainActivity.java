package edu.illinois.jchen93.bitstampapiandroid1;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setContentView(R.layout.activity_main);
		Button mainMenuButton1 = null;
    	Button mainMenuButton2 = null;
    	mainMenuButton1=(Button)findViewById(R.id.btn1);
        mainMenuButton2=(Button)findViewById(R.id.btn2);
        mainMenuButton1.setOnClickListener(listener);
        mainMenuButton2.setOnClickListener(listener);
        
		
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
	            ArrayList<Transaction> s = intent.getParcelableArrayListExtra(TransactionUpdateService.TRANSACTION_RESULT);
	            // do something here.
	            
	            if (s==null){ Log.i(TAG, "broadvast received null, failed broadcast");}
	            else{
	            	Log.i(TAG, "broadcast receive correctly, arraylist size is: "+Integer.toString(s.size()));
	            	plotTransaction(s);}
	        }
	    };
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		TextView temp = (TextView)findViewById(R.id.textView);
		temp.setText("make you choice :)");
		
		LocalBroadcastManager.getInstance(this).registerReceiver((receiver), new IntentFilter(TransactionUpdateService.TRANSACTION_RESULT));
	}
	
	private OnClickListener listener = new OnClickListener()
    {
    	public void onClick(View v){
    		Button btn=(Button)v;
    		switch (btn.getId()){
				case R.id.btn1:
					intent = new Intent(MainActivity.this, TransactionUpdateService.class);
					intent.setData(Uri.parse("0"));
					PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 101, intent, 0);
					alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
				    alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, 2500, pendingIntent);
					break;
    			
	    		case R.id.btn2:
	
	    			break;
    		}
    	}
    };

    @Override
    protected void onStop() {
    	super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        
        intent = new Intent(MainActivity.this, TransactionUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 101, intent, 0);
        alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(pendingIntent);
        Log.i(TAG, "on stop");
    }
	
	private void plotTransaction(ArrayList<Transaction> transactionArray){
		TextView toggleView = (TextView)findViewById(R.id.textView);
		//toggleView.setText("");
        toggleView.setVisibility(0);
		plot1 = (XYPlot) findViewById(R.id.plot1);
		plot1.clear();
		
		int n = transactionArray.size();
		Log.i(TAG, "transaction size is: "+n);
		Number[] time = new Number[n];
		Number[] y = new Number[n];
		int i = 0;
		for(Transaction temp : transactionArray){
			time[i] = Long.parseLong(temp.getDate());
			y[i] = Double.parseDouble(temp.getPrice());
			//Log.i(TAG, "time is: "+time[i]);
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
                Color.RED,                   // line color
                Color.rgb(0, 100, 0),                   // point color
                null, null);                // fill color
        
        plot1.getGraphWidget().setPaddingRight(2);
        plot1.addSeries(series, format);

        // draw a domain tick for each time:
        //plot1.setDomainStep(XYStepMode.SUBDIVIDE, time.length/400);
        plot1.setDomainStepValue(10);

        // customize our domain/range labels
        plot1.setDomainLabel("Time");
        plot1.setRangeLabel("Value");

        plot1.setDomainValueFormat(new Format() {

            // create a simple date format that draws on the year portion of our timestamp.
            // see http://download.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
            // for a full description of SimpleDateFormat.
            private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

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
       
        plot1.redraw();
        plot1.setVisibility(1);
        plot1.bringToFront();
	}

	
	private void plotTradeBook(Vector<Vector<Double>> vec){
		TextView temp = (TextView)findViewById(R.id.textView);
		//temp.setText("");
		temp.setVisibility(0);
		plot1 = (XYPlot)findViewById(R.id.plot1);
		plot1.clear();
		
		int nbid = vec.get(0).size();
		int nask = vec.get(2).size();
		Number[] x1 = new Number[nbid];
		Number[] y1 = new Number[nbid];
		for(int i=0; i<nbid; i++){
			x1[i] = vec.get(0).get(i);
			y1[i] = vec.get(1).get(i);
		}
		Number[] x2 = new Number[nask];
		Number[] y2 = new Number[nask];
		for(int i=0; i<nask; i++){
			x2[i] = vec.get(2).get(i);
			y2[i] = vec.get(3).get(i);
		}
		XYSeries series1 = new SimpleXYSeries(Arrays.asList(x1),Arrays.asList(y1),"Bids");
		XYSeries series2 = new SimpleXYSeries(Arrays.asList(x2),Arrays.asList(y2),"Asks");
		
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
