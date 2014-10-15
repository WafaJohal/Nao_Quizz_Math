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

public class Resultat extends MonActivity {
	private Intent donnee;
	static String ip;
	static int port;

	public void onBackPressed() {
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		EcouteTask tache = new EcouteTask(Resultat.this);
		tache.execute();
		
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		 ip = sharedPref.getString("pref_robotip", "192.168.0.101");
		 port = sharedPref.getInt("pref_robotport", 9559);


		// R�cup�raion du param�tre pass� par l'activit� Ardoise
		donnee = this.getIntent();
		String resultat = donnee.getStringExtra(Ardoise.RESULTAT);

		setContentView(R.layout.activity_resultat);

		TextView textView = (TextView) this.findViewById(R.id.text);   
		Typeface font = Typeface.createFromAsset(getAssets(), "test.ttf");
		textView.setTypeface(font);
		textView.setText("Ta r�ponse:" + resultat + " Ecoute NAO");

		Button bRejouer = (Button) this.findViewById(R.id.buttonRejouer);		
		bRejouer.setEnabled(false);

		bRejouer.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				EnvoiTask tacheEnvoi = new EnvoiTask(Resultat.this);
				tacheEnvoi.execute("rejouer");
				// Lancement de l'activit� Ardoise
				Intent activityResultat = new Intent(Resultat.this, Ardoise.class);
				startActivity(activityResultat);
				finish();
			}
		}); 

		Button bNonRejouer = (Button) this.findViewById(R.id.buttonNonRejouer);
		bNonRejouer.setEnabled(false);
		bNonRejouer.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				EnvoiTask tacheEnvoi = new EnvoiTask(Resultat.this);
				tacheEnvoi.execute("nonRejouer");
				finish();
			}
		}); 
	}

	static class EnvoiTask extends AsyncTask<String, Void, Boolean> {
		Resultat classResultat;

		public EnvoiTask(Resultat classResultat) {
			this.classResultat = classResultat;
		}

		protected Boolean doInBackground(String... params) {
			
			try {
				new DataOutputStream(Connexion.getInstance(ip,port).getOutputStream()).writeUTF(params[0]);

			} catch (Exception e) {
				return false;
			}
			return true;		
		} 

		protected void onPostExecute(Boolean resultat) {
			if (!resultat) {
				classResultat.startActivity(new Intent(classResultat, PbReception.class));				
				classResultat.finish();
			}
		}
	}

	static class EcouteTask extends AsyncTask<Void, Void, String> {
		private Resultat classResultat;

		public EcouteTask(Resultat classResultat) {
			this.classResultat = classResultat;
		}

		protected String doInBackground(Void... params) {
			
			try {
				String resultat = new DataInputStream(Connexion.getInstance(ip,port).getInputStream()).readUTF(); // R�cup�re le r�sultat
				new DataInputStream(Connexion.getInstance(ip,port).getInputStream()).readUTF(); // Attendre que NAO ait fini de parler

				return resultat;

			} catch (Exception e) {
				return "ERREUR";
			}
		} 

		protected void onPostExecute(String resultat) {
			if (!resultat.equals("ERREUR")) {
				TextView textView = (TextView) classResultat.findViewById(R.id.text);   
				Typeface font = Typeface.createFromAsset(classResultat.getAssets(), "test.ttf");
				textView.setTypeface(font);
				String resultatRecu = classResultat.donnee.getStringExtra(Ardoise.RESULTAT);
				if (resultatRecu.equals(resultat)) {
					textView.setText("Bravo, c'est la bonne r�ponse");
				} else {
					textView.setText("Dommage, la r�ponse est " + resultat);
				}

				Button bRejouer = (Button) classResultat.findViewById(R.id.buttonRejouer);
				bRejouer.setEnabled(true);

				Button bNonRejouer = (Button) classResultat.findViewById(R.id.buttonNonRejouer);
				bNonRejouer.setEnabled(true);

			} else {
				classResultat.startActivity(new Intent(classResultat, PbReception.class));				
				classResultat.finish();
			}
		}
	}
}