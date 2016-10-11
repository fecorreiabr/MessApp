package br.iesb.messapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import br.iesb.messapp.model.Location;

/**
 * Created by Felipe on 08/10/2016.
 */
public class SendPositionService extends IntentService {

    private static final String LOCATIONS_NODE = "locations";
    private DatabaseReference mDatabase;

    public SendPositionService() {
        super("SendPositionService");
        setFirebase();
    }

    public SendPositionService(String name) {
        super(name);
        setFirebase();
    }

    private void setFirebase(){
        mDatabase = FirebaseDatabase.getInstance().getReference(LOCATIONS_NODE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("TAGService", "Intent service acionado...");
        String uId = intent.getStringExtra("uid");
        LatLng loc = intent.getParcelableExtra("location");

        if (uId != null && loc != null){
            Location location = new Location(uId, loc.latitude, loc.longitude);
            mDatabase.child(uId).setValue(location);
        }

    }
}
