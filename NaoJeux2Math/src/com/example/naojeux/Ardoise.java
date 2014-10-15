package com.example.naojeux;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

import com.example.nao_tablette_android.R;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Ardoise extends MonActivity implements OnGesturePerformedListener {
	private  GestureLibrary gestureLib;
	private TextView textView;

	// Utile pour r�cup�rer le passage de param�tre dans l'activit� Resultat
	public final static String RESULTAT = "com.example.naojeux.Resultat";

	@SuppressLint({ "InlinedApi", "NewApi" })
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// tacheEcoute permet de savoir quand NAO a fini de parler
		EcouteTask tacheEcoute = new EcouteTask(Ardoise.this);
		tacheEcoute.execute();

		// Permet de mettre en place l'�criture sur l'ardoise, sur toute la surface de la tablette
		GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
		View inflate = getLayoutInflater().inflate(R.layout.activity_ardoise, null);
		gestureOverlayView.addView(inflate);
		gestureOverlayView.setBackgroundColor(Color.TRANSPARENT);
		gestureOverlayView.setUncertainGestureColor(Color.RED);
		gestureOverlayView.setGestureColor(Color.GREEN);
		gestureOverlayView.addOnGesturePerformedListener(this);
		gestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures); 
		if (!gestureLib.load()) {
			finish();
		}	
		setContentView(gestureOverlayView);

		gestureOverlayView.setLayerType(View.LAYER_TYPE_SOFTWARE, null); 
		// Pour ne pas avoir OpenGLRenderer 0x502 GL_INVALID_OPERATION

		// Le boutton valider appara�t quand NAO a fini de parler, et permet d'envoyer le r�sultat (le text de l'ardoise)
		Button bValider = (Button) this.findViewById(R.id.buttonValider);
		bValider.setVisibility(View.INVISIBLE);
		bValider.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				textView = (TextView) findViewById(R.id.text);
				EnvoiTask tacheEnvoi = new EnvoiTask(Ardoise.this);
				tacheEnvoi.execute(textView.getText().toString());

				Intent activityResultat = new Intent(Ardoise.this, Resultat.class);
				// On passe � l'activit� le r�sultat tap� par l'utilisateur, pour l'afficher dans la prochaine activit�
				activityResultat.putExtra(RESULTAT, textView.getText().toString());
				startActivity(activityResultat);
				finish();
			}
		}); 

		Button bGomme = (Button) this.findViewById(R.id.buttonGomme);	
		bGomme.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (textView != null && textView.length() != 0) {
					// Permet de supprimer le dernier caract�re
					textView.setText(textView.getText().toString().substring(0, textView.length() -1));
				}
			}
		});		

	} 

	static class EnvoiTask extends AsyncTask<String, Void, String> {
		Ardoise classArdoise;

		public EnvoiTask(Ardoise classArdoise) {
			this.classArdoise = classArdoise;
		}

		protected String doInBackground(String... params) {
			
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(new SettingsActivity());
			String ip = sharedPref.getString("pref_robotip", "192.168.0.101");
			int port = sharedPref.getInt("pref_robotport", 9559);



			try {
				new DataOutputStream(Connexion.getInstance(ip,port).getOutputStream()).writeUTF(params[0]);

			} catch (Exception e) {
				return MonActivity.RECHEC;
			}
			return MonActivity.RSUCCES;
		}

		protected void onPostExecute(String resultat) {
			if (resultat.equals(MonActivity.RECHEC)) {
				classArdoise.startActivity(new Intent(classArdoise, PbReception.class));				
				classArdoise.finish();
			}
		}
	}

	static class EcouteTask extends AsyncTask<Void, Void, String> {
		private Ardoise classArdoise;

		public EcouteTask(Ardoise classArdoise) {
			this.classArdoise = classArdoise;
		}

		protected String doInBackground(Void... params) {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(new SettingsActivity());
			String ip = sharedPref.getString("pref_robotip", "192.168.0.101");
			int port = sharedPref.getInt("pref_robotport", 9559);
			try {
				new DataInputStream(Connexion.getInstance(ip,port).getInputStream()).readUTF();
			} catch (Exception e) {
				return MonActivity.RECHEC;
			}
			return MonActivity.RSUCCES;
		} 

		protected void onPostExecute(String resultat) {
			if (resultat.equals(MonActivity.RSUCCES)) {
				Button bValider = (Button) classArdoise.findViewById(R.id.buttonValider);	
				bValider.setVisibility(View.VISIBLE);

			} else {
				classArdoise.startActivity(new Intent(classArdoise, PbReception.class));				
				classArdoise.finish();
			}
		}
	}

	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		// M�thode appel�e � chaque fois que l'on �crit

		// Charge tout les mouvements d�clar�s dans le fichier gesture, dans un tableau
		ArrayList<Prediction> predictions = gestureLib.recognize(gesture);

		textView = (TextView) this.findViewById(R.id.text);   
		Typeface font = Typeface.createFromAsset(getAssets(), "test.ttf");
		textView.setTypeface(font);

		// On r�cup�re le premier mouvement, car c'est celui qui a le plus de chance de correspondre
		// avec la saisie de l'utilisateur
		String detect = predictions.get(0).name;

		if (detect.equals("-")) {
			// Le signe moins doit �tre le premier caract�re
			if (textView.length() == 0) {
				textView.append(detect);
			} else { // Si le signe moins n'est pas le premier caract�re, on informe l'utilisateur par un Toast
				LayoutInflater inflater = getLayoutInflater();
				View layout = inflater.inflate(R.layout.custom_toast_2, (ViewGroup) findViewById(R.id.custom_toast_layout));
				TextView text = (TextView) layout.findViewById(R.id.textToShow);
				Toast toast = new Toast(getApplicationContext());
				toast.setView(layout);
				toast.setDuration(Toast.LENGTH_LONG);
				text.setText("Le signe moins doit �tre plac� au d�but du nombre");
				toast.show();
			}
		} else {
			if (Integer.valueOf(textView.getText().toString() + detect).compareTo(100) <= 0) {
				if (textView.length() > 2) { 
					// Dans le cas o� il y aurait 000, il ne faut pas que l'on puisse entrer une infinit� de 0
					LayoutInflater inflater = getLayoutInflater();
					View layout = inflater.inflate(R.layout.custom_toast_2, (ViewGroup) findViewById(R.id.custom_toast_layout));
					TextView text = (TextView) layout.findViewById(R.id.textToShow);
					Toast toast = new Toast(getApplicationContext());
					toast.setView(layout);
					toast.setDuration(Toast.LENGTH_LONG);
					text.setText("L'ardoise ne peut contenir plus de 3 caract�res");
					toast.show();
				} else {
					textView.append(detect);
				}
			} else {
				LayoutInflater inflater = getLayoutInflater();
				View layout = inflater.inflate(R.layout.custom_toast_2, (ViewGroup) findViewById(R.id.custom_toast_layout));
				TextView text = (TextView) layout.findViewById(R.id.textToShow);
				Toast toast = new Toast(getApplicationContext());
				toast.setView(layout);
				toast.setDuration(Toast.LENGTH_LONG);
				text.setText("Le r�sultat n'est pas un nombre plus grand que 100");
				toast.show();
			}
		}
	}
} 