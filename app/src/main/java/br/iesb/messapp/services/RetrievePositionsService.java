package br.iesb.messapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Felipe on 10/10/2016.
 */
public class RetrievePositionsService extends IntentService {

    private static final String LOCATIONS_NODE = "locations";
    private DatabaseReference mDatabase;

    public RetrievePositionsService() {
        super("RetrievePositionsService");
        setFirebase();
    }

    public RetrievePositionsService(String name) {
        super(name);
        setFirebase();
    }

    private void setFirebase(){
        mDatabase = FirebaseDatabase.getInstance().getReference(LOCATIONS_NODE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(getClass().getSimpleName(), "Iniciado serviço de recuperação de localizações.");

        int seconds = 60;
        while (true){
            try {
                //TODO codigo para recuperar localizacoes

                Thread.sleep(seconds * 1000);
            } catch (InterruptedException e){
                e.printStackTrace();
            }

        }
    }
}
