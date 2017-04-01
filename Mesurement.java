package com.upec.zulemie.projetlp;

/**
 * Created by Rudy on 23/03/2017.
 */

// définition de la classe mesurement permettant de stocker nos données à chaque clic
public class Mesurement {
    String SSID;
    double distance;
    int x,y;


    public Mesurement( String SSID, double distance, int x, int y) {
        this.y = y;
        this.SSID = SSID;
        this.distance = distance;
        this.x = x;
    }

    // Création de fonction pour la récolte des données du point d'accès
    public String getSSID() {
        return SSID;
    }

    public double getDistance() {
        return distance;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
