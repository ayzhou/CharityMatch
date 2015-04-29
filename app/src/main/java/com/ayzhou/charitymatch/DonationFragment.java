package com.ayzhou.charitymatch;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import static com.mongodb.client.model.Filters.*;

import com.andtinder.model.CardModel;
import com.andtinder.model.Orientations;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.bson.BsonArray;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;

/**
 * Created by Alan on 4/6/2015.
 */
public class DonationFragment extends Fragment {

    private CardContainer mCardContainer;
    private CharityCardAdapter adapter;
    private Location mLastLocation;
    private View V;


    class addDonationTask extends AsyncTask<Object, Object, Object> {
        protected Object doInBackground(Object... args) {
            CharityCard card = (CharityCard) (args[0]);
            MongoClient mongoClient = new MongoClient("ec2-52-11-161-98.us-west-2.compute.amazonaws.com");
            MongoDatabase charityDb = mongoClient.getDatabase("charities");
            MongoCollection<Document> pledgeCollection = charityDb.getCollection("pledges");
            MongoCollection<Document> donationCollection = charityDb.getCollection("donations");

            String android_id = Settings.Secure.getString(V.getContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            MongoCursor<Document> cursor = pledgeCollection.find(eq("android_id", android_id)).iterator();

            if (!cursor.hasNext()) {
                ArrayList<ObjectId> arrList = new ArrayList<ObjectId>();
                arrList.add(card.donationID);
                Document doc = new Document("android_id", android_id).append("pledgeList", Arrays.asList(card.donationID));
                pledgeCollection.insertOne(doc);
            } else {
                Document doc = cursor.next();
                ArrayList<ObjectId> arrList = (ArrayList<ObjectId>) doc.get("pledgeList");
                arrList.add(card.donationID);
                doc.remove("pledgeList");
                doc.append("pledgeList", arrList);
                pledgeCollection.updateOne(eq("_id", doc.getObjectId("_id")), new Document("$set", doc));
            }
            return null;
                    }
    }
    class getNeededTask extends AsyncTask<Object, Object, Object> {


        protected Object doInBackground(Object... args) {
            //get mongo objects

            String[] tags =  (String[]) args[1];


            MongoClient mongoClient = new MongoClient("ec2-52-11-161-98.us-west-2.compute.amazonaws.com");
            MongoDatabase charityDb = mongoClient.getDatabase("charities");
            MongoCollection<Document> donationCollection = charityDb.getCollection("donations");
            MongoCollection<Document> charityCollection = charityDb.getCollection("charities");

            HashSet<Document> donationSet = new HashSet<Document>();
            ArrayList<CharityCard> charityCardList = new ArrayList<CharityCard>();


            int count = 0;
            for (int i = 0; i < tags.length; i++) {
                MongoCursor<Document> cursor = donationCollection.find(in("tagStrings", tags[i])).iterator();
                while (cursor.hasNext()) {
                    count ++;
                    if (count > 10) break;
                    Document d = cursor.next();
                    donationSet.add(d);
                }
                if (count > 10) break;
            }

            for (Document d : donationSet) {
                Document e = charityCollection.find(eq("_id", d.getObjectId("charityId"))).first();
                System.out.println(d);
                ArrayList<String> tagStrings = (ArrayList<String>) d.get("tagStrings");
                String tagString = "Tags: ";
                for (String s : tagStrings) {
                    System.out.println(s);
                    tagString += s + " ";
                }

                Location charityLocation = new Location(LocationManager.NETWORK_PROVIDER);
                try {
                    charityLocation.setLongitude(e.getDouble("longitude"));
                    charityLocation.setLatitude(e.getDouble("latitude"));
                }
                catch (Exception f) {
                    continue;
                }
                double distanceToCharity = charityLocation.distanceTo(mLastLocation) * 0.000621371;

                CharityCard card = new CharityCard(e.getString("name"), d.getString("description"), getResources().getDrawable(R.drawable.charity),
                        distanceToCharity, tagString);
                card.lastDonation = e.getDate("lastDonation");
                card.beginDate = d.getDate("beginDate");
                card.endDate = d.getDate("endDate");
                card.numNeeded = d.getInteger("numNeeded");
                card.donationID = d.getObjectId("_id");
                card.charityID = d.getObjectId("charityId");
                charityCardList.add(card);
            }

            for (CharityCard c : charityCardList) {
                final CharityCard d = c;
                c.n = charityCardList.size();
                c.percentangeDone = (new Date().getTime() - c.beginDate.getTime())/(c.endDate.getTime()-c.beginDate.getTime());
                c.setOnCardDimissedListener(new CardModel.OnCardDimissedListener(){

                    public void onDislike() {

                    }
                    @Override
                    public void onLike() {
                        new addDonationTask().execute(d);
                    }
                });
            }
            return charityCardList;
        }

        protected void onPostExecute(Object o) {
            ArrayList<CharityCard> arrList = (ArrayList<CharityCard>) o;

            CharityCard[] cardArr = new CharityCard[arrList.size()];
            CharityCard[] cardArrDist = new CharityCard[arrList.size()];
            CharityCard[] cardArrNeed = new CharityCard[arrList.size()];
            CharityCard[] cardArrRecent = new CharityCard[arrList.size()];

            for (int i = 0; i < arrList.size(); i++) {
                cardArr[i] = arrList.get(i);
                cardArrDist[i] = arrList.get(i);
                cardArrNeed[i] = arrList.get(i);
                cardArrRecent[i] = arrList.get(i);
            }

            Arrays.sort(cardArrDist, new Comparator<CharityCard>() {
                @Override
                public int compare(CharityCard lhs, CharityCard rhs) {
                    if (lhs.getDistance() < rhs.getDistance()) {
                        return 1;
                    }
                    if (lhs.getDistance() > rhs.getDistance()) {
                        return -1;
                    }
                    return 0;

                }
            });

            Arrays.sort(cardArrNeed, new Comparator<CharityCard>() {
                @Override
                public int compare(CharityCard lhs, CharityCard rhs) {
                    if (lhs.numNeeded > rhs.numNeeded) {
                        return 1;
                    }
                    if (lhs.numNeeded < rhs.numNeeded) {
                        return -1;
                    }
                    return 0;
                }
            });

            Arrays.sort(cardArrRecent, new Comparator<CharityCard>() {
                @Override
                public int compare(CharityCard lhs, CharityCard rhs) {
                    if (lhs.lastDonation.getTime() < rhs.lastDonation.getTime()) {
                        return 1;
                    }
                    if (lhs.lastDonation.getTime() > rhs.lastDonation.getTime()) {
                        return -1;
                    }
                    return 0;
                }
            });

            for (int i = 0; i < cardArr.length; i++) {
                cardArr[i].recentlyDonatedtoRank = i+1;
                cardArr[i].distanceRank = i+1;
                cardArr[i].numNeededRank = i+1;
            }
            Arrays.sort(cardArr, new Comparator<CharityCard>() {
                @Override
                public int compare(CharityCard lhs, CharityCard rhs) {
                    double optimalDistRank = 1.0/lhs.n*.8;
                    double optimalPercentage = 0;
                    double optimalRecent = 1.0/lhs.n;
                    double optimalNumNeeded = 1.0/lhs.n;

                    double euclideanDistance1 = Math.sqrt(Math.pow(optimalDistRank-lhs.distanceRank, 2) +
                            Math.pow(optimalPercentage-(1.0-lhs.percentangeDone), 2) + Math.pow(optimalRecent-lhs.recentlyDonatedtoRank, 2)
                            +Math.pow(lhs.numNeededRank-optimalNumNeeded, 2));

                    double euclideanDistance2 = Math.sqrt(Math.pow(optimalDistRank-rhs.distanceRank, 2) +
                            Math.pow(optimalPercentage-(1.0-rhs.percentangeDone), 2) + Math.pow(optimalRecent-rhs.recentlyDonatedtoRank, 2)
                            +Math.pow(rhs.numNeededRank-optimalNumNeeded, 2));

                    if (euclideanDistance1 < euclideanDistance2) {
                        return 1;
                    }
                    if (euclideanDistance1 > euclideanDistance2) {
                        return -1;
                    }
                    return 0;
                }
            });

            adapter = new CharityCardAdapter(getActivity());
            for (int i = 0; i < cardArr.length; i++) {
                adapter.add(cardArr[i]);
            }

            mCardContainer.setAdapter(adapter);
        }

    }

    class DonationThread extends Thread {
        public void run() {
            mCardContainer = (CardContainer) V.findViewById(R.id.cardcontainer);
            mCardContainer.setOrientation(Orientations.Orientation.Disordered);
            adapter = new CharityCardAdapter(getActivity());
            mCardContainer.setAdapter(adapter);
        }
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        V = inflater.inflate(R.layout.donation_fragment, container, false);

        new DonationThread().run();
        LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                //your code here
                mLastLocation = location;

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }


        };


        LocationManager mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);


        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,
                0, mLocationListener);

        if (mLastLocation == null) {
            mLastLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }


        return V;
    }

    public void updateCards(String[] tags) {
        new getNeededTask().execute(mLastLocation, tags);
    }

}
