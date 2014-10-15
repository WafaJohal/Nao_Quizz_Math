package com.example.naojeux;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.example.nao_tablette_android.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MenuOperation extends MonActivity {
	static String ip;
	static int port ;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_menuoperation);
		 // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    	
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		 ip = sharedPref.getString("pref_robotip", "192.168.0.101");
		 port = sharedPref.getInt("pref_robotport", 9559);

       

		// tacheEcoute permet de savoir quand NAO a fini de parler
		EcouteTask tacheEcoute = new EcouteTask(MenuOperation.this);
		tacheEcoute.execute();

		TextView textView = (TextView) this.findViewById(R.id.text);   
		Typeface font = Typeface.createFromAsset(getAssets(), "test.ttf");
		textView.setTypeface(font);
		textView.setText("Ecoute NAO");

		Button bCroix = (Button) this.findViewById(R.id.buttonCroix);
		bCroix.setEnabled(false); // Le boutton n'est pas cliquable mais visible
		bCroix.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				EnvoiTask tacheEnvoi = new EnvoiTask(MenuOperation.this);
				tacheEnvoi.execute("multiplication");
				Intent activityArdoise = new Intent(MenuOperation.this, Ardoise.class);
				startActivity(activityArdoise);
			}
		}); 

		Button bPlus = (Button) this.findViewById(R.id.buttonPlus);
		bPlus.setEnabled(false); // Le boutton n'est pas cliquable mais visible
		bPlus.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				EnvoiTask tacheEnvoi = new EnvoiTask(MenuOperation.this);
				tacheEnvoi.execute("addition");
				Intent activityArdoise = new Intent(MenuOperation.this, Ardoise.class);
				startActivity(activityArdoise);
			}
		}); 

		Button bMoins = (Button) this.findViewById(R.id.buttonMoins);
		bMoins.setEnabled(false); // Le boutton n'est pas cliquable mais visible
		bMoins.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				EnvoiTask tacheEnvoi = new EnvoiTask(MenuOperation.this);
				tacheEnvoi.execute("soustraction");	
				Intent activityArdoise = new Intent(MenuOperation.this, Ardoise.class);
				startActivity(activityArdoise);
			}	
		}); 

		Button bQuitter = (Button) this.findViewById(R.id.buttonQuitter);
		bQuitter.setEnabled(false); // Le boutton n'est pas cliquable mais visible
		bQuitter.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				EnvoiTask tacheEnvoi = new EnvoiTask(MenuOperation.this);
				tacheEnvoi.execute("quitter");
				finish();
				Connexion.fermerInstance();
			}
		});
	}

	static class EnvoiTask extends AsyncTask<String, Void, String> {
		MenuOperation classMenuOperation;

		public EnvoiTask(MenuOperation classMenuOperation) {
			this.classMenuOperation = classMenuOperation;
		}

		protected String doInBackground(String... params) {
			
			try {
				new DataOutputStream(Connexion.getInstance(ip,port).getOutputStream()).writeUTF(params[0]);

			} catch (Exception e) {
				return MonActivity.RECHEC;
			}
			return MonActivity.RSUCCES;
		} 

		protected void onPostExecute(String resultat) {
			
			if (resultat.equals(MonActivity.RECHEC)) {
				classMenuOperation.startActivity(new Intent(classMenuOperation, PbReception.class));	
				classMenuOperation.finish();
			}
		}
	}

	static class EcouteTask extends AsyncTask<Void, Void, String> {
		private MenuOperation classMenuOperation;

		public EcouteTask(MenuOperation classMenuOperation) {
			this.classMenuOperation = classMenuOperation;
		}

		protected String doInBackground(Void... params) {
			
			try {
				new DataInputStream(Connexion.getInstance(ip,port).getInputStream()).readUTF();
			} catch (Exception e) {
				return MonActivity.RECHEC;
			}
			return MonActivity.RSUCCES;
		} 

		protected void onPostExecute(String resultat) {
			if (resultat.equals(MonActivity.RSUCCES)) {

				TextView textView = (TextView) classMenuOperation.findViewById(R.id.text);   
				Typeface font = Typeface.createFromAsset(classMenuOperation.getAssets(), "test.ttf");
				textView.setTypeface(font);
				textView.setText("");

				Button bCroix = (Button) classMenuOperation.findViewById(R.id.buttonCroix);
				bCroix.setEnabled(true);

				Button bPlus = (Button) classMenuOperation.findViewById(R.id.buttonPlus);
				bPlus.setEnabled(true);

				Button bMoins = (Button) classMenuOperation.findViewById(R.id.buttonMoins);
				bMoins.setEnabled(true);

				Button bQuitter = (Button) classMenuOperation.findViewById(R.id.buttonQuitter);
				bQuitter.setEnabled(true);

			} else {
				classMenuOperation.startActivity(new Intent(classMenuOperation, PbReception.class));				
				classMenuOperation.finish();
			}
		}
	}
}