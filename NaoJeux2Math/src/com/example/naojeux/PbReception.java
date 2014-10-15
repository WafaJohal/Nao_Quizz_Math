package com.example.naojeux;


import com.example.nao_tablette_android.R;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PbReception extends MonActivity {
	public final static String OPERATION = "com.example.naojeux.OPERATION";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_pbreception);
		 // Display the fragment as the main content.
        
		Button bRetour = (Button) this.findViewById(R.id.buttonRetour);
		bRetour.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Connexion.fermerInstance();
				finish();
			}
		});

		TextView textView = (TextView) this.findViewById(R.id.text);   
		Typeface font = Typeface.createFromAsset(this.getAssets(), "test.ttf");
		textView.setTypeface(font);
		textView.setText("Probl�me de r�ception avec NAO");
	}
}
