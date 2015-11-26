package lac.contextnet.sddl.usernode;

import java.util.UUID;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import lac.contextnet.model.EventObject;
import lac.contextnet.model.PingObject;

/**
 * MainActivity: This is our application's MainActivity. It consists in 
 * 				 a UUID randomly generated and shown in txt_uuid, a text 
 * 				 field for the IP:PORT in et_ip, a "Ping!" button 
 * 				 (btn_ping) to send a Ping object message, a "Start 
 * 				 Service!" button (btn_startservice) to start the 
 * 				 communication service and a "Stop Service!" button 
 * 				 (btn_stopservice) to stop it.
 * 
 * @author andremd
 * 
 */
public class MainActivity extends Activity {

	/* Shared Preferences */
	private static String uniqueID = "f73e71eb-e137-4401-b927-073f0454ad12";
	private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
	private static final String PREF_SDDL_SERVER = "PREF_SDDL_SERVER";
	
	/* Static Elements */
	private TextView txt_uuid;
	private EditText et_ip;
	private Button btn_ping;
	private Button btn_startservice;
	private Button btn_stopservice;
	private RadioGroup toggleGroupContexts;
	private Button btnComputer;
	private Button btnLamp;
	private Button btnTv;
	private Button btnEvent1;
	private Button btnEvent2;
	
	/* App data */
	private String context = "computer"; //initial context
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/* GUI Elements */
		txt_uuid = (TextView) findViewById(R.id.txt_uuid);
		et_ip = (EditText) findViewById(R.id.et_ip);
		//sets initial "default" ip address
		et_ip.setText(getServerAddress(getBaseContext()));
		btn_ping = (Button) findViewById(R.id.btn_ping);
		btn_startservice = (Button) findViewById(R.id.btn_startservice);
		btn_stopservice = (Button) findViewById(R.id.btn_stopservice);
		toggleGroupContexts = (RadioGroup) findViewById(R.id.toggle_group_contexts);
		btnComputer = (Button) findViewById(R.id.btn_computer);
		btnLamp = (Button) findViewById(R.id.btn_lamp);
		btnTv = (Button) findViewById(R.id.btn_tv);
		btnEvent1 = (Button) findViewById(R.id.btn_event1);
		btnEvent2 = (Button) findViewById(R.id.btn_event2);
		txt_uuid.setText(GetUUID(getBaseContext()));
		
		/* Change context buttons listener*/
		OnClickListener contextButtonListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				for (int j = 0; j < toggleGroupContexts.getChildCount(); j++) {
	                final ToggleButton view = (ToggleButton) toggleGroupContexts.getChildAt(j);
	                view.setChecked(view.getId() == v.getId());
				}
				if (v.getId() == btnComputer.getId()) {
					context = "computer";
				} else if (v.getId() == btnLamp.getId()) {
					context = "lamp";
				} else if (v.getId() == btnTv.getId()) {
					context = "tv";
				} else {
					context = "unknown";
				}
				Toast.makeText(getBaseContext(), context, Toast.LENGTH_SHORT).show();
			}
		};
		btnComputer.setOnClickListener(contextButtonListener);
		btnLamp.setOnClickListener(contextButtonListener);
		btnTv.setOnClickListener(contextButtonListener);
		
		/* Events button listener */
		OnClickListener eventsButtonListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				EventObject newEvent = null;
				if (v.getId() == btnEvent1.getId()) {
					newEvent = new EventObject("event1", context);
				} else if (v.getId() == btnEvent2.getId()) {
					newEvent = new EventObject("event2", context);
				}
				if (newEvent != null) Toast.makeText(getBaseContext(), newEvent.toString(), Toast.LENGTH_SHORT).show();
				if(!isMyServiceRunning(CommunicationService.class))
					Toast.makeText(getBaseContext(), getResources().getText(R.string.msg_e_servicenotrunning), Toast.LENGTH_SHORT).show();
				else
				{	
					if (newEvent != null) {
						/* Calling the SendMsg action to the MsgBroadcastReceiver */
						Intent i = new Intent(MainActivity.this, CommunicationService.class);
						i.setAction("lac.contextnet.sddl.usernode.broadcastmessage." + "ActionSendMsg");
						i.putExtra("lac.contextnet.sddl.usernode.broadcastmessage." + "ExtraMsg", newEvent);
						LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(i);	
					}
				}
			}
		};
		btnEvent1.setOnClickListener(eventsButtonListener);
		btnEvent2.setOnClickListener(eventsButtonListener);
		
		/* Ping Button Listener*/
		btn_ping.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!isMyServiceRunning(CommunicationService.class))
					Toast.makeText(getBaseContext(), getResources().getText(R.string.msg_e_servicenotrunning), Toast.LENGTH_SHORT).show();
				else
				{
					PingObject ping = new PingObject();
					
					/* Calling the SendPingMsg action to the PingBroadcastReceiver */
					Intent i = new Intent(MainActivity.this, CommunicationService.class);
					i.setAction("lac.contextnet.sddl.usernode.broadcastmessage." + "ActionSendMsg");
					i.putExtra("lac.contextnet.sddl.usernode.broadcastmessage." + "ExtraMsg", ping);
					LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(i);
				}
			}
		});

		/* Start Service Button Listener*/
		btn_startservice.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				String ipPort = et_ip.getText().toString();
				
				if(!IPPort.IPRegexChecker(ipPort))
				{
					Toast.makeText(getBaseContext(), getResources().getText(R.string.msg_e_invalid_ip), Toast.LENGTH_LONG).show();
					return;
				}
				setServerAddress(getBaseContext(), ipPort);
				IPPort ipPortObj = new IPPort(ipPort);
				
				/* Starting the communication service */
				Intent intent = new Intent(MainActivity.this, CommunicationService.class);
				intent.putExtra("ip", ipPortObj.getIP());
				intent.putExtra("port", Integer.valueOf(ipPortObj.getPort()));
				intent.putExtra("uuid", txt_uuid.getText().toString());
				startService(intent); 
			}
		});

		/* Stop Service Button Listener*/
		btn_stopservice.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/* Stops the service and finalizes the connection */
				stopService(new Intent(getBaseContext(), CommunicationService.class));
			}
		});
	}
	
	//See http://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-in-android
	private boolean isMyServiceRunning(Class<?> serviceClass) {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	//See http://androidsnippets.com/generate-random-uuid-and-store-it
	public synchronized static String GetUUID(Context context) {
	    if (uniqueID == null) {
	        SharedPreferences sharedPrefs = context.getSharedPreferences(
	                PREF_UNIQUE_ID, Context.MODE_PRIVATE);
	        uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
	        if (uniqueID == null) {
	            uniqueID = UUID.randomUUID().toString();
	            Editor editor = sharedPrefs.edit();
	            editor.putString(PREF_UNIQUE_ID, uniqueID);
	            editor.commit();
	        }
	    }
	    return uniqueID;
	}
	
	public synchronized static String getServerAddress(Context context) {
    	String addr;
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_SDDL_SERVER, Context.MODE_PRIVATE);
        addr = sharedPrefs.getString(PREF_SDDL_SERVER, null);
        if (addr == null) {
            addr = "192.168.1.104:5555";
            Editor editor = sharedPrefs.edit();
            editor.putString(PREF_SDDL_SERVER, addr);
            editor.commit();
        }
	    return addr;
	}
	
	public synchronized static void setServerAddress(Context context, String addr) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_SDDL_SERVER, Context.MODE_PRIVATE);
        Editor editor = sharedPrefs.edit();
        editor.putString(PREF_SDDL_SERVER, addr);
        editor.commit();
	}
}
