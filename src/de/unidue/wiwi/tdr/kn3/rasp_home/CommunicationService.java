package de.unidue.wiwi.tdr.kn3.rasp_home;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class CommunicationService extends IntentService implements Observer<String> {

	public Observable<String> observer;
	
	private static final int SERVER_PORT = 8008;
	
	private CommunicationClass com;
	
	public CommunicationService() {
		super("CommunicationService");
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(MainApplication.RH_TAG, "Start CommunicationService");
		observer = new Observable<String>();
		com = new CommunicationClass(this);
		com.observer.addObserver(this);
		com.StartServer(SERVER_PORT);
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
		com.observer.deleteObserver(this);
		com.StopServer();
	}

	@Override
	public void update(Observable<String> o, String arg) {
		Log.d(MainApplication.RH_TAG, "Get message: " + arg);
		observer.notifyObservers(arg);
	}
}
