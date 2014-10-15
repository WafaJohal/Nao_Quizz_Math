package com.example.naojeux;
import java.net.Socket;

public class Connexion {
	private static Socket socket = null;

	private Connexion(String ip, int port) throws Exception {
		// Connexion au socket du serveur
		
		//Connexion.socket = new Socket("192.168.0.100", 1244);
		Connexion.socket = new Socket(ip,port);
	}
 
	public static Socket getInstance(String ip, int port) throws Exception {
		// Comme le passage des objets entre les activit�s sont difficile sous Android, et que l'on veut garder une connexion
		// pendant toute la dur�e de l'application (plusieurs activit�s), on va retourner tout le temps le socket qui a �t�
		// initialis�, sauf au d�but (avant la connexion) o� il vaut null (connexion pas encore �tablie), et donc on va 
		// l'initiliser
		if(socket == null){
			// Ouverture de la connexion (dans l'activit� Demarrage)
			new Connexion(ip, port);
		}
		return socket; 
	} 

	public static void fermerInstance() {
		// Fermeture du socket
		try {
			Connexion.socket.close();
		} catch (Exception e) {
		}
	}
}