package de.unidue.wiwi.tdr.kn3.rasp_home;

import de.unidue.wiwi.tdr.kn3.rasp_home.CommunicationClass.Message.Type;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class CommunicationService extends IntentService implements Observer<CommunicationClass.Message> {

	public CommunicationService() {
		super("CommunicationService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(MainApplication.RH_TAG, "Start CommunicationService");
		MainApplication.com.zeroconf.observer.addObserver(this);
		MainApplication.com.server.observer.addObserver(this);
		MainApplication.com.client.setUserPass(MainApplication.pref.getString("pref_communication_user", ""), MainApplication.pref.getString("pref_communication_pass", ""));
		MainApplication.com.zeroconf.Start(MainApplication.pref.getInt("pref_communication_zeroconfport", 1234));
		MainApplication.com.server.Start(MainApplication.pref.getInt("pref_communication_serverport", 8888));
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		synchronized (this) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(MainApplication.RH_TAG, "Stop CommunicationService");
		MainApplication.com.zeroconf.observer.deleteObserver(this);
		MainApplication.com.server.observer.deleteObserver(this);
		MainApplication.com.zeroconf.Stop();
		MainApplication.com.server.Stop();
	}

	@Override
	public void update(Observable<CommunicationClass.Message> o, CommunicationClass.Message arg) {
		Log.d(MainApplication.RH_TAG, "Get message: " + arg.type.toString() + " " + arg.title + " " + arg.content);
		if (arg.type == Type.zeroconfig) {
			MainApplication.com.client.setServerIpPort(arg.content);
		}
		//MainApplication.com.server.setAuthorizedUserPass("ich:123");
		//MainApplication.com.client.SendRequestPost("/nodes", "hallo node");
	}
}
