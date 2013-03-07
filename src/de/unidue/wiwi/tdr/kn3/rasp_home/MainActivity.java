package de.unidue.wiwi.tdr.kn3.rasp_home;


import java.util.List;

import de.unidue.wiwi.tdr.kn3.rasp_home.CommunicationClass.RequestMessage.Method;
import de.unidue.wiwi.tdr.kn3.rasp_home.CommunicationClass.RequestMessage.Type;
import de.unidue.wiwi.tdr.kn3.rasp_home.DatabaseClass.Room;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
//import android.widget.SpinnerAdapter;
import android.widget.Toast;

public class MainActivity extends Activity {

	
	boolean selector = false;
	String[] room = new String[1];

	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		room[0]="Kitchen";
	//	CommunicationClass.ResponseMessage response = MainApplication.com.client.SendRequest(new CommunicationClass.RequestMessage(Method.GET, Type.Room, null, null, null, null));
		
		//MainApplication.database.rooms.
	//	MainApplication.database.rooms.
	//room = response value
		
		
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, room);
		
 
		actionBar.setListNavigationCallbacks(adapter, navigationListener);
	

		}
	
	ActionBar.OnNavigationListener navigationListener = new OnNavigationListener() {
		 
        @Override
        public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        	if(selector){
            Toast.makeText(getBaseContext(), "You selected : " + room[itemPosition]  , Toast.LENGTH_SHORT).show();
            

            Intent nextScreen = new Intent(getApplicationContext(), RoomControl.class);
            
          //Intent mit den Daten füllen
    /*     nextScreen.putExtra("Rooms", room);
         nextScreen.putExtra("currentRoom", room[itemPosition]);*/
nextScreen.putExtra("currentRoom", "test");

            // Intent starten und zur zweiten Activity wechseln
            startActivity(nextScreen);}
        	selector=true;
     
            
            return true;
        }
    };
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
