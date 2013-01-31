package de.unidue.wiwi.tdr.kn3.rasp_home;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class CommunicationService extends IntentService implements Observer<CommunicationClass.Message> {

	public Observable<CommunicationClass.Message> observer;
	
	private CommunicationClass com;
	
	public CommunicationService() {
		super("CommunicationService");
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(MainApplication.RH_TAG, "Start CommunicationService");
		observer = new Observable<CommunicationClass.Message>();
		com = new CommunicationClass(this);
		com.observer.addObserver(this);
		//com.zeroconfig.Start(1234);
		com.server.Start(8888);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		com.server.setAuthorizedUserPass("ich", "123");
		com.client.setServerIpPort("192.168.1.86", 8888);
		com.client.setUserPass("ich", "123");
		com.client.SendRequestPost("/nodes", "hallo node");
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
		com.observer.deleteObserver(this);
		//com.zeroconfig.Stop();
		com.server.Stop();
	}

	@Override
	public void update(Observable<CommunicationClass.Message> o, CommunicationClass.Message arg) {
		Log.d(MainApplication.RH_TAG, "Get message: " + arg.type.toString() + " " + arg.title + " " + arg.content);
		observer.notifyObservers(arg);
	}
}
