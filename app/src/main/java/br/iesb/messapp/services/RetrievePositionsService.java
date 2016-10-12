package br.iesb.messapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import br.iesb.messapp.MapsActivity;
import br.iesb.messapp.model.Location;

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
    protected void onHandleIntent(final Intent intent) {
        Log.i(getClass().getSimpleName(), "Iniciado serviço de recuperação de localizações.");

        final String uId = intent.getStringExtra("uid");
        final List<Location> userLocations = new ArrayList<>();
        int seconds = 60;
        if (uId != null) {
            while (true) {
                try {

                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> locations = dataSnapshot.getChildren();

                            for (DataSnapshot data : locations) {
                                String locationUId = data.getKey();
                                if (!uId.equals(locationUId)) {
                                    Location location = data.getValue(Location.class);
                                    location.setId(locationUId);
                                    userLocations.add(location);
                                }
                            }

                            //TODO carregar localizações para o mapa

                            Intent mapUpdateIntent = new Intent(MapsActivity.MAP_UPDATE);
                            mapUpdateIntent.putExtra("locations", (ArrayList)userLocations);
                            //mapUpdateIntent.putParcelableArrayListExtra("locations", userLocations);
                            sendBroadcast(mapUpdateIntent);
                            Log.i("POSITION", "Locations retrieved to map");
                            Log.i("POSITION", userLocations.toArray().toString());

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    Thread.sleep(seconds * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

            }
        }
    }
}
