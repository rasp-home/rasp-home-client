package de.unidue.wiwi.tdr.kn3.rasp_home;

import de.unidue.wiwi.tdr.kn3.rasp_home.CommunicationClass.RequestMessage.Method;
import de.unidue.wiwi.tdr.kn3.rasp_home.CommunicationClass.RequestMessage.Type;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.ListAdapter;



public class AdminAct extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_admin);
	
CommunicationClass.ResponseMessage response2 = MainApplication.com.client.SendRequest(new CommunicationClass.RequestMessage(Method.GET, Type.Room, null, null, null, null));
		
		if(response2.status!=202){ Toast.makeText(getBaseContext(), "Sorry, an error occurred"  , Toast.LENGTH_SHORT).show(); 
		return;
		}
		
		final String [] roomArray = new String[response2.value.length()];
		
		CommunicationClass.ResponseMessage response = MainApplication.com.client.SendRequest(new CommunicationClass.RequestMessage(Method.GET, Type.Node, null, null, null, null));
		
		if(response.status!=202){ Toast.makeText(getBaseContext(), "Sorry, an error occurred"  , Toast.LENGTH_SHORT).show(); 
		return;
		}
		String[] data = new String[response.value.length()];
	for(int i=0;i<response.value.length();i++){
		data[i] = response.value;}
		
		ListView list = new ListView(this);
		ArrayAdapter adapter =
			new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
		list.setAdapter(adapter);
		setContentView(list);
 
		list.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
					AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
				builder.setTitle("Set room and name");

				ListView modeList = new ListView(getApplicationContext());
				
				ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, android.R.id.text1, roomArray);
				modeList.setAdapter(modeAdapter);

				builder.setView(modeList);
				final Dialog dialog = builder.create();

				dialog.show();
			}
		});	
}
}