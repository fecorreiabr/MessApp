package br.iesb.messapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import br.iesb.messapp.adapters.ContactsAdapter;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    public final static int REQUEST_NEW_CONTACT = 9999;
    public final static int REQUEST_EDIT_CONTACT = 9998;
    protected final static int CONTACTS_PAGE_POSITION = 0;
    protected final static int CHATS_PAGE_POSITION = 1;
    protected final static int TOTAL_PAGES = 2;

    private static String userId;
    protected static List<Contact> contactList;
    private Realm realm;
    private RealmConfiguration realmConfig;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userId = getIntent().getStringExtra("id");

        firebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference(userId);

        realmConfig = new RealmConfiguration.Builder(this).build();
        realm = Realm.getInstance(realmConfig);
        loadContactList();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        loadContactListFromFirebase();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MapsActivity.MAP_REQUEST_FINE_LOCATION_PERMISSION);
        } else {
            Utility.createAlarm(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                loadContactList();
                updateContactsRecyclerView();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_logout:
                Utility.saveLogout(this);
                firebaseAuth.signOut();
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.action_settings:
                return true;
            case R.id.action_bluetooth:
                intent = new Intent(this, BluetoothActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_map:
                intent = new Intent(this, MapsActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadContactList() {
        RealmResults<Contact> results = realm.where(Contact.class).equalTo("owner", userId).findAll();
        contactList = results;
    }

    private void updateContactsRecyclerView(){
        PlaceholderFragment contactsFragment =
                (PlaceholderFragment) mSectionsPagerAdapter.getFragment(CONTACTS_PAGE_POSITION);
        contactsFragment.mainAdapter.notifyDataSetChanged();
    }

    private void loadContactListFromFirebase() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                realm.executeTransactionAsync(new Realm.Transaction() {
                      @Override
                      public void execute(Realm realm) {
                          Iterable<DataSnapshot> dataSnapshots = dataSnapshot.getChildren();

                          for (DataSnapshot data : dataSnapshots) {
                              String key = data.getKey();
                              if (!key.equals("email") && !key.equals("name")) {
                                  Contact contact = data.getValue(Contact.class);
                                  contact.setId(key);
                                  contact.setOwner(userId);
                                  realm.copyToRealmOrUpdate(contact);
                              }
                          }

                      }
                  }, new Realm.Transaction.OnSuccess() {
                      @Override
                      public void onSuccess() {
                          Log.i(MainActivity.class.getSimpleName(), "Lista de contatos atualizada.");
                          loadContactList();
                          updateContactsRecyclerView();
                          Toast.makeText(MainActivity.this, getString(R.string.contact_list_updated), Toast.LENGTH_SHORT).show();
                      }
                  }, new Realm.Transaction.OnError() {
                      @Override
                      public void onError(Throwable error) {
                          String TAG = MainActivity.class.getSimpleName();
                          Log.e(TAG, "Erro ao carregar contatos do Firebase.");
                          error.printStackTrace();
                      }
                  }
                );
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String TAG = MainActivity.class.getSimpleName();
                Log.i(TAG, databaseError.getMessage());
                Log.i(TAG, databaseError.getDetails());
            }
        });
    }

    public void onClickNewContact(View view) {
        Intent intent = new Intent(this, ContactActivity.class);
        intent.putExtra("userId", userId);
        startActivityForResult(intent, REQUEST_NEW_CONTACT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MapsActivity.MAP_REQUEST_FINE_LOCATION_PERMISSION:
                boolean permissionsGranted = true;
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            permissionsGranted = false;
                            break;
                        }
                    }
                } else {
                    permissionsGranted = false;
                }

                if (permissionsGranted) {
                    Utility.createAlarm(this);
                } else {
                    Utility.alertMsg(
                            this,
                            getString(R.string.title_permission_necessary),
                            getString(R.string.msg_permission_location_update)
                    );
                    finish();
                }

                break;
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private RecyclerView mainRecyclerView;
        private RecyclerView.Adapter mainAdapter;
        private RecyclerView.LayoutManager mainLayoutManager;

        private Context context;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(Context context, int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            fragment.context = context;
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            View rootView;

            if (sectionNumber == 1) {
                rootView = inflater.inflate(R.layout.fragment_main, container, false);

                mainRecyclerView = (RecyclerView) rootView.findViewById(R.id.main_recycler_view);
                mainRecyclerView.setHasFixedSize(true);
                //mainRecyclerView.setLongClickable(true);
                mainLayoutManager = new LinearLayoutManager(getActivity());
                mainRecyclerView.setLayoutManager(mainLayoutManager);
                mainAdapter = new ContactsAdapter(context, contactList) {
                    @Override
                    public void onItemLongClickListener(int position) {
                        editContact(position);
                    }
                };


                mainRecyclerView.setAdapter(mainAdapter);
            } else {
                rootView = inflater.inflate(R.layout.fragment_chats, container, false);
            }

            //TextView textView = (TextView) rootView.findViewById(R.id.section_label);

            //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }

        private void editContact(int position) {
            String contactId = contactList.get(position).getId();
            Intent intent = new Intent(this.getContext(), ContactActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("contactId", contactId);
            startActivityForResult(intent, REQUEST_EDIT_CONTACT);
        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private Fragment[] fragments = new Fragment[TOTAL_PAGES];

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            fragments[position] = PlaceholderFragment.newInstance(MainActivity.this, position + 1);
            return fragments[position];
            //return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return TOTAL_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case CONTACTS_PAGE_POSITION:
                    return getResources().getString(R.string.contacts_tab);
                case CHATS_PAGE_POSITION:
                    return getResources().getString(R.string.chats_tab);
            }
            return null;
        }

        public Fragment getFragment(int position) {
            return fragments[position];
        }
    }
}
