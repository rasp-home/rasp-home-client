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
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
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
	    	   Toast.makeText(getBaseContext(), "An error occurred, please wait while you are located  ", Toast.LENGTH_SHORT).show();
	    	   
	    	   //Locate user
	    	  curRoom = MainApplication.pos.lastLocation;;
	      return;
	    }
	    else{
		 
	        // Receiving the Data
	//      room = extras.getStringArray("rooms");
	//      curRoom = extras.getString("currentRoom");
	    	
			
		}
	    
		
	   

		
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
            		startActivity(nextScreen);
                }
            	selector=true;
            return true;
            }
        };
 
		actionBar.setListNavigationCallbacks(adapter, navigationListener);

		//TODO
		//Create Linear layout with nodes and button OR value depending on nodetype
		layouting();

		}
	private void layouting(){
		
		CommunicationClass.ResponseMessage response = MainApplication.com.client.SendRequest(new CommunicationClass.RequestMessage(Method.GET, Type.Node, null, curRoom, null, null));
		if(response.status!=202){ Toast.makeText(getBaseContext(), "Sorry, an error occurred"  , Toast.LENGTH_SHORT).show(); 
		return;
		}
		
		String nodesinroom=response.value;
		TableLayout tl = (TableLayout) findViewById(R.id.LinearLayout1);
		for(int i=0; i<nodesinroom.length();i++){
			TableRow tr1 = new TableRow(this);
			tr1.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			TextView name = new TextView(this);
			name.setText(""+ nodesinroom.charAt(i));
			tr1.addView(name);
			tl.addView(tr1, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
			
			
		}
		//tl.addView... uU erst hier um alte rows nicht zu überschreiben
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

/*	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MainActivity.searchPosition();
	}*/


}
