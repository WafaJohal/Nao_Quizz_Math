package com.example.naojeux;

import java.io.DataOutputStream;

import com.example.nao_tablette_android.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;



public class Demarrage extends MonActivity {
	static String ip;
	static int port;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demarrage);
		 // Display the fragment as the main content.
        
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		 ip = sharedPref.getString("pref_robotip", "192.168.0.101");
		 port = sharedPref.getInt("pref_robotport", 9559);

		// R�cup�re le textView du XML pour lui appliquer un changement de police
		TextView textView = (TextView) this.findViewById(R.id.text);   
		Typeface font = Typeface.createFromAsset(getAssets(), "test.ttf");
		textView.setTypeface(font);

		// Android interdit l'utilisation de socket en dehors d'un thread. tache permet l'ouverture d'une connexion avec 
		// le serveur
		ConnexionTask tache = new ConnexionTask(Demarrage.this);
		// execute() lance doInBackground()
		tache.execute();

		// Le boutton continuer est invisible. Une fois que NAO a fini de parler et que la connexion � r�ussie
		// il devient visible. Le clique sur ce boutton
		// envoi (par l'interm�diaire de ContinuerTask) au serveur le message continuer
		Button bContinuer = (Button) this.findViewById(R.id.buttonContinuer);
		bContinuer.setVisibility(View.INVISIBLE);
		bContinuer.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				EnvoiTask tache = new EnvoiTask(Demarrage.this);
				tache.execute("continuer");
				// Cela permet de passer � une autre activit� qui est MenuOperation
				Intent activityMenuOperation = new Intent(Demarrage.this, MenuOperation.class);
				startActivity(activityMenuOperation);
				// On termine l'activit� courante (Demarrage)
				finish();
			}
		});

		// Si la connexion �choue, alors le boutton quitter devient visible
		Button bQuitter = (Button) this.findViewById(R.id.buttonQuitter);
		bQuitter.setVisibility(View.INVISIBLE);
		bQuitter.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});	
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intetact=new Intent(this,SettingsActivity.class);
			startActivity(intetact);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	static class EnvoiTask extends AsyncTask<String, Void, String> {
		private Demarrage classDemarrage;

		public EnvoiTask(Demarrage classDemarrage) {
			this.classDemarrage = classDemarrage;
		}

		protected String doInBackground(String... params) {
			// params == "continuer"
			
			try {
				new DataOutputStream(Connexion.getInstance(ip,port).getOutputStream()).writeUTF(params[0]);

			} catch (Exception e) {
				return MonActivity.RECHEC;
			}

			return MonActivity.RSUCCES;
		} 

		protected void onPostExecute(String resultat) {
			// Une fois que doInBackground() ce termine, on v�rifie que doInBackground n'a pas lev� une exception
			// si c'est le cas on lance l'activit� PbReception
			if (resultat.equals(MonActivity.RECHEC)) {
				classDemarrage.startActivity(new Intent(classDemarrage, PbReception.class));	
				classDemarrage.finish();
			}
		}
		
		
		
		
	}

	static class ConnexionTask extends AsyncTask<Void, Void, String> {
		private Demarrage classDemarrage;

		public ConnexionTask(Demarrage classDemarrage) {
			this.classDemarrage = classDemarrage;
		}

		protected String doInBackground(Void... params) {
			
			
			try {
				Connexion.getInstance(ip,port);

			} catch (Exception e) {
				return MonActivity.CECHEC;
			}
			return MonActivity.CSUCCES;		
		} 

		protected void onPostExecute(String resultat) {
			TextView textView = (TextView) classDemarrage.findViewById(R.id.text);   
			Typeface font = Typeface.createFromAsset(classDemarrage.getAssets(), "test.ttf");
			textView.setTypeface(font);	

			if (resultat.equals(MonActivity.CSUCCES)) {
				textView.setText(MonActivity.CSUCCES);
				Button bContinuer = (Button) classDemarrage.findViewById(R.id.buttonContinuer);				
				bContinuer.setVisibility(View.VISIBLE);
			} else {
				textView.setText(MonActivity.CECHEC);
				Button bQuitter = (Button) classDemarrage.findViewById(R.id.buttonQuitter);
				bQuitter.setVisibility(View.VISIBLE);
			}
		}
	}
}
