package com.ayzhou.charitymatch;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar.Tab;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andtinder.model.CardModel;
import com.andtinder.model.Orientations;
import com.andtinder.view.SimpleCardStackAdapter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.ParallelScanOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import static com.mongodb.client.model.Filters.*;

import com.mirko.tbv.TabBarView;

import java.util.Locale;


public class MainActivity extends ActionBarActivity

 {
     private TabBarView tabBarView;
     ViewPager mViewPager;

     SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.tabbarview, null);
        tabBarView = (TabBarView) v.findViewById(R.id.tab_bar);
        tabBarView.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 2) {
                    System.out.println("2 selected");
                    PledgeFragment f =  (PledgeFragment) getSupportFragmentManager().findFragmentByTag(getFragmentTag(2));
                    f.update();
                }
                if (position == 3) {
                    System.out.println("3 selected");
                    HistoryFragment f =  (HistoryFragment) getSupportFragmentManager().findFragmentByTag(getFragmentTag(3));
                    f.update();
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(v);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabBarView.setViewPager(mViewPager);
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

     public class SectionsPagerAdapter extends FragmentPagerAdapter implements TabBarView.IconTabProvider {

         private int[] tab_icons={R.drawable.yes, R.drawable.yes, R.drawable.yes, R.drawable.yes
         };


         public SectionsPagerAdapter(FragmentManager fm) {
             super(fm);
         }

         @Override
         public Fragment getItem(int position) {
             // getItem is called to instantiate the fragment for the given page.
             // Return a PlaceholderFragment (defined as a static inner class
             // below).
             switch(position) {
                 case 0: return new CreateDonationFragment(mViewPager);
                 case 1: return new DonationFragment();
                 case 2: return new PledgeFragment(mViewPager);
                 case 3: return new HistoryFragment();
             }
             return new Fragment();
         }

         @Override
         public int getCount() {
             // Show 3 total pages.
             return tab_icons.length;
         }

         @Override
         public int getPageIconResId(int position) {
             return tab_icons[position];
         }

         @Override
         public CharSequence getPageTitle(int position) {
             Locale l = Locale.getDefault();
             switch (position) {
                 case 0:
                     return getString(R.string.donation).toUpperCase(l);
                 case 1:
                     return getString(R.string.donation).toUpperCase(l);
                 case 2:
                     return "Donate!".toUpperCase(l);
                 case 3:
                     return "Donate!".toUpperCase(l);
             }
             return null;
         }
     }

     private String getFragmentTag(int pos){
         return "android:switcher:"+R.id.pager+":"+pos;
     }

     public static void main(String[] args) {
        MongoClient mongoClient = new MongoClient("ec2-52-11-161-98.us-west-2.compute.amazonaws.com");
        MongoDatabase charityDb = mongoClient.getDatabase("test");

        MongoCollection<Document> charities = charityDb.getCollection("charities");

        Document doc = charities.find(eq("name", "The Crisis Ministry of Princeton")).first();
    }
}
