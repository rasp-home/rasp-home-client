package de.unidue.wiwi.tdr.kn3.rasp_home;

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
		MainApplication.com.zeroconf.Start(1234);
		MainApplication.com.server.Start(8888);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		MainApplication.com.server.setAuthorizedUserPass("ich", "123");
		MainApplication.com.client.setServerIpPort("192.168.1.86", 8888);
		MainApplication.com.client.setUserPass("ich", "123");
		MainApplication.com.client.SendRequestPost("/nodes", "hallo node");
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
		// TODO Handle Zeroconfig message etc
	}
}
