package backup;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;




public class TransactionUpdateService extends IntentService{
	
	public static String TAG = "TRANSACTION_UPDATE_SERVICE";
	
	public TransactionUpdateService(){
		super("TransactionUpdateService");
	}
	
	public TransactionUpdateService(String name){
		super(name);
	}
	
	@Override
	protected void onHandleIntent(Intent intent){
		refreshTransactions();
	}
	
	@Override
	public IBinder onBind(Intent intent){
		return null;
	}
	
	private void addNewTransaction(List<Transaction> trans){
		ContentResolver cr = getContentResolver();
		
		// construct a where clause to make sure we don't already have this transaction in the provider
		String w = TransactionsProvider.KEY_TID + "=" + trans.get(0).getTid();
		
		// if the transaction is new, insert it into the provider
		Cursor query = cr.query(TransactionsProvider.CONTENT_URI, null, w, null, null);
		
		/* still need some algorithm to make sure the data does not overlaid!
		 * still need some algorithm to make sure the data does not overlaid!
		 * still need some algorithm to make sure the data does not overlaid!
		 * still need some algorithm to make sure the data does not overlaid!
		 * still need some algorithm to make sure the data does not overlaid!
		 * still need some algorithm to make sure the data does not overlaid!
		 * */
		if(query.getCount() == 0){
			for (Transaction temp : trans){
				ContentValues values = new ContentValues();
				
				values.put(TransactionsProvider.KEY_DATE, temp.getDate());// need to change return type
				values.put(TransactionsProvider.KEY_TID, temp.getTid());
				values.put(TransactionsProvider.KEY_PRICE, temp.getPrice());
				values.put(TransactionsProvider.KEY_AMOUNT, temp.getAmount());
				
				cr.insert(TransactionsProvider.CONTENT_URI, values);
			}
		}
		query.close();
	}
	
	public void refreshTransactions(){
		String path = "https://www.bitstamp.net/api/transactions/";
        
        try {
        	URL url=new URL(path);
            HttpURLConnection c=(HttpURLConnection)url.openConnection();
            c.setRequestMethod("GET");
        	c.setReadTimeout(15000);
        	c.connect();
            
            ObjectMapper mapper = new ObjectMapper();
            List<Transaction> ltran = mapper.readValue(c.getInputStream(), new TypeReference<ArrayList<Transaction>>() { });
            
            addNewTransaction(ltran);
            
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
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		// retrieve the shared preferences
		Context context = getApplicationContext();
		
		int updateFreq = 2000;
		int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
		long timeToRefresh = SystemClock.elapsedRealtime()+updateFreq;
		alarmManager.setInexactRepeating(alarmType, timeToRefresh, updateFreq, alarmIntent);
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	private AlarmManager alarmManager;
	private PendingIntent alarmIntent;
	
	@Override
	public void onCreate(){
		super.onCreate();
		alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		
		String ALARM_ACTION = TransactionAlarmReceiver.ACTION_REFRESH_TRANSACTION_ALARM;
		Intent intentToFire = new Intent(ALARM_ACTION);
		alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, 0);
	}

	
}