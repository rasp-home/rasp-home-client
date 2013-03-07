package de.unidue.wiwi.tdr.kn3.rasp_home;




import de.unidue.wiwi.tdr.kn3.rasp_home.CommunicationClass.RequestMessage.Method;
import de.unidue.wiwi.tdr.kn3.rasp_home.CommunicationClass.RequestMessage.Type;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class RoomControl  extends Activity {

	

	String[] room = null;
	boolean selector = false;
	String curRoom = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_roomcontrol);
			
	Bundle extras = getIntent().getExtras();
	    if (extras == null) {
	    	   Toast.makeText(getBaseContext(), "An error occured, please wait while you are located  ", Toast.LENGTH_SHORT).show();
	    	   //TODO
	    	   //Locate user
	    	  curRoom ="Located_room";
	      return;
	    }
	    else{
		 
	        // Receiving the Data
	//      room = extras.getStringArray("rooms");
	//      curRoom = extras.getString("currentRoom");
	    	
			
		}
	    
		
	    CommunicationClass.ResponseMessage response = MainApplication.com.client.SendRequest(new CommunicationClass.RequestMessage(Method.GET, Type.Node, null, curRoom, null, null));

	//	MainApplication.database.rooms.
	//room = response value
	//TODO
		//Create Linear layout with nodes and button OR value depending on nodetype
		
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, room);
		ActionBar.OnNavigationListener navigationListener = new OnNavigationListener() {
			 
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
            	
            	if(selector){
                Toast.makeText(getBaseContext(), "You selected : " + room[itemPosition]  , Toast.LENGTH_SHORT).show();
     Intent nextScreen = new Intent(getApplicationContext(), RoomControl.class);
                
                //Intent mit den Daten füllen
                nextScreen.putExtra("Rooms", room);
                nextScreen.putExtra("currentRoom", room[itemPosition]);
 
               
 
                // Intent starten und zur zweiten Activity wechseln
                startActivity(nextScreen);}
            	selector=true;
                return true;
            }
        };
 
		actionBar.setListNavigationCallbacks(adapter, navigationListener);



		}
	
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
