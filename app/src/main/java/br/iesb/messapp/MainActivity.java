package br.iesb.messapp;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userId = getIntent().getStringExtra("id");
        realmConfig = new RealmConfiguration.Builder(this).build();
        realm = Realm.getInstance(realmConfig);
        LoadContactList();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                LoadContactList();
                PlaceholderFragment contactsFragment =
                        (PlaceholderFragment) mSectionsPagerAdapter.getFragment(CONTACTS_PAGE_POSITION);
                contactsFragment.mainAdapter.notifyDataSetChanged();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void LoadContactList() {
        RealmResults<Contact> results = realm.where(Contact.class).equalTo("owner", userId).findAll();
        contactList = results;
    }

    public void onClickNewContact(View view) {
        Intent intent = new Intent(this, ContactActivity.class);
        intent.putExtra("userId", userId);
        startActivityForResult(intent, REQUEST_NEW_CONTACT);
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

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
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
                mainAdapter = new ContactsAdapter(contactList) {
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
            fragments[position] = PlaceholderFragment.newInstance(position + 1);
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
