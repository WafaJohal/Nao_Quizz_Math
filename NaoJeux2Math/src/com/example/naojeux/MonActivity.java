package com.example.naojeux;

import android.app.Activity;


public abstract class MonActivity extends Activity{
	protected final static String NAOFINIPARLER = "NAO a fini de parler";
	protected final static String RSUCCES = "La r�ception est r�ussie";
	protected final static String RECHEC = "La r�ception � �chou�";
	protected final static String CSUCCES = "Connexion avec NAO r�ussie";
	protected final static String CECHEC = "Connexion avec NAO �chou�";

	public void onBackPressed() {	
		// Impossible de fermer l'application avec le boutton retour de la tablette
	}
}

