package com.upec.zulemie.projetlp;
// Déclaration des
import android.annotation.TargetApi;
import android.app.usage.UsageEvents;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;

import static android.R.attr.onClick;
import static java.lang.StrictMath.sqrt;

public class MainActivity extends AppCompatActivity {       // Définition de l'activité principale de l'application qui hérite de la classe AppCompatActivity


    HashMap<String,List<Mesurement>> wifilist;      // Création d'une HashMap "wifilist" qui aura comme paramètres une chaîne de caractère et une liste
    int x1,x2,x3;                                      // Création des différentes variables pour effectuer la tirangulation du point d'accès
    int y1,y2,y3;
    double d1;
    double d2;
    double d3;
    double positionX,positionY;

    // Création d'une fonction nous permettant de calculer la distance qui nous sépare du point d'accès
    public double calculateDistance(double levelInDb, double freqInMHz)    {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(levelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)

    // Déclaration de la méthode OnCreate qui va contenir pour notre part tout ce que l'application va implémenter à son démarrage
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        wifilist=new HashMap<>();               // Initialisation de la HashMap
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);     // Définit le "layout" a afficher par l'application au démarrage

        final ImageView plan = (ImageView)findViewById(R.id.plan);  // Déclaration de l'image du plan dans l'activité afin de pouvoir intéragir avec l'image

        final ImageView access = (ImageView)findViewById(R.id.access); // Déclaration de l'image du point d'accès pour pouvoir intéragir avec l'image

        // Déclaration d'un tableau qui permettra de définir les coordonnées exactes de la photo du plan (car elle ne fait pas tout l'écran)
        final int[] viewCoords = new int[2];
        plan.getLocationOnScreen(viewCoords);       // récupération des coordonnées

        // Déclaration de la méthode OnTouchListener dans laquelle on va définir toutes les actions liées au toucher de notre plan de l'IUT

        plan.setOnTouchListener(new View.OnTouchListener() {
            TextView position=(TextView)findViewById(R.id.position); // Déclaration de la zone de texte au dessus du plan qui nous donnera les coordonnées

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                int touchX = (int) e.getX();            // Enregistre dans la variable touchX la coordonnée X
                int touchY = (int) e.getY();            // Enregistre dans la variable touchY la coordonnée Y
                int imageX = touchX - viewCoords[0];    // Calcul de la position réelle de X sur le plan grâce au tableau viewCoords
                int imageY = touchY - viewCoords[1];    // Calcul de la position réelle de Y sur le plan grâce au tableau viewCoords
                final int resultX = (int) (imageX *0.1);    // Calcul de la valeur réelle en mètre de X
                final int resultY = (int) (imageY *0.1);    // Calcul de la valeur réelle en mètre de Y
                position.setText("x  : "+resultX + "   y : "+resultY);  // Affiche dans le Textview la valeur de X et Y


                // Déclaration de la classe WifiManager permettant gérer tous les aspects du WIFI dans l'application
                final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);


                // Création de l'enregistrement du scan des bornes WIFI dans l'intent registerReceiver
                registerReceiver(new BroadcastReceiver()
                {
                    @Override
                    public void onReceive(Context c, Intent intent)     // C'est ici qu'on définit les actions à effectuer lorsque l'enregistrement est lancé
                    {

                        List<ScanResult> results = wifi.getScanResults();   // On créé une liste "results" qui va contenir les infos récoltés par "wifi.getScanResults()"

                        int rssi=100;           // On initialise une variable nous donnant le RSSI
                        int chan=-1;            // On initialise une variable pour le canal utilisé
                        String mac="";          // On initialise une chaine de caractère qui va contenir l'adresse MAC


                        for (ScanResult s: results) {               // Création d'une boucle FOR qui filtre le scan Wifi uniquement sur le SSID "Étudiants-Paris12"
                            if (s.SSID.equals("Etudiants-Paris12") & Math.abs(s.level) < rssi) {
                                rssi = Math.abs(s.level);       // Obtention de la valeur du RSSI
                                mac = s.BSSID;                  // Obtention de l'adresse MAC
                                chan = s.frequency;             // Obtention du canal utilisé par le WIFI



                                // création d'une condition pour vérifier que "wifilist" contient bien la même adresse mac pour chaque ligne de la liste
                            if (wifilist.containsKey((mac))==false){
                                wifilist.put(mac,new ArrayList<Mesurement>());
                            }
                                wifilist.get(mac).add( new Mesurement(s.SSID,calculateDistance((double) rssi, chan),resultX,resultY )); //ici on enregistre nos données tour à tour dans la HashMap
                        }}

                        List<Mesurement> temp=wifilist.get(mac); // On crée une variable temp qui sera une liste et qui va contenir le contenu de "wifilist"
                        if ( temp.size()>2) {           // On créé une condition pour vérifier que la liste comprend plus de 2 mesures
                            // On récupère l'ensemble des données relatives à la position nous intéressant
                            x1 = temp.get(0).getX();
                            x2 = temp.get(1).getX();
                            x3 = temp.get(2).getX();

                            y1 = temp.get(0).getY();
                            y2 = temp.get(1).getY();
                            y3 = temp.get(2).getY();

                            d1 = temp.get(0).getDistance()*2;
                            d2 = temp.get(1).getDistance()*2;
                            d3 = temp.get(2).getDistance()*2;

                            // On résoud la matrice nous permettant d'obtenir la localisation du point d'accès par triangulation
                            positionX = ((2*(x3- x1))*(sqrt(d1)-sqrt(d3)+sqrt(x3)-sqrt(x1)+sqrt(y3)-sqrt(y1))) + ((2*(x3- x1))*(sqrt(d2)-sqrt(d3)+sqrt(x3)-sqrt(x2)+sqrt(y3)-sqrt(y2)));
                            positionY = ((2*(y3-y1))* (sqrt(d1)-sqrt(d3)+sqrt(x3)-sqrt(x1)+sqrt(y3)-sqrt(y1))) + ((2*(y3 - y2)) *(sqrt(d2)-sqrt(d3)+sqrt(x3)-sqrt(x2)+sqrt(y3)-sqrt(y2)));

                            // on positionne l'image représentant la location de notre point d'accès
                            access.setVisibility(View.VISIBLE);
                            RelativeLayout.LayoutParams l = (RelativeLayout.LayoutParams)access.getLayoutParams();
                            l.setMargins((int)positionX,(int)positionY,0,0);
                        }


                    }
                }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)); // Action qui va activer le RegisterReceiver configuré plus haut

                wifi.startScan();  // Lancement du scan WIFI par l'application

                return true;
            }

        });
      

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    // Fin de l'application



}
