package edu.illinois.jchen93.bitstampapiandroid1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class TransactionAlarmReceiver extends BroadcastReceiver{
	public static final String ACTION_REFRESH_TRANSACTION_ALARM = 
			"edu.illinois.jchen93.transation.ACTION_REFRESH_TRANSACTION_ALARM";
	
	@Override
	public void onReceive(Context context, Intent intent){
		Intent startIntent = new Intent(context, TransactionUpdateService.class);
		context.startService(startIntent);
	}
}