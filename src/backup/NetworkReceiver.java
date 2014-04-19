package backup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;



public class NetworkReceiver extends BroadcastReceiver{
	public static final String TAG = "NetworkReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent){
		
		boolean isNetworkDown = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
		
		if(isNetworkDown){
			Log.d(TAG, "onReceive: NOT connected, stopping UpdateService");
		}else{
			Log.d(TAG, "onReceive: connected, starting UpdateService");
			context.startService(new Intent(context, TransactionUpdateService.class));
			// needs to choose between two
		}
	}
}