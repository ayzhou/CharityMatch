package com.ayzhou.charitymatch;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import android.provider.Settings.Secure;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import static com.mongodb.client.model.Filters.*;


import org.bson.Document;
import org.bson.types.ObjectId;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Alan on 4/8/2015.
 */
public class HistoryFragment extends Fragment {
    View V;
    private ListView listView;

    private class GetHistoryTask extends AsyncTask<Object, Object, Object>{


        @Override
        protected Object doInBackground(Object... params) {
            MongoClient mongoClient = new MongoClient("ec2-52-11-161-98.us-west-2.compute.amazonaws.com");
            MongoDatabase charityDb = mongoClient.getDatabase("charities");
            MongoCollection<Document> historyCollection = charityDb.getCollection("history");
            MongoCollection<Document> donationCollection = charityDb.getCollection("donations");
            MongoCollection<Document> charityCollection = charityDb.getCollection("charities");
            String android_id = Secure.getString(V.getContext().getContentResolver(),
                    Secure.ANDROID_ID);
            MongoCursor<Document> cursor = historyCollection.find(eq("android_id", android_id)).iterator();

            ArrayList<String> historyStrings = new ArrayList<String>();

            //create new user account
            if (!cursor.hasNext()) {
                Document doc = new Document("android_id", android_id).append("historyList", new ArrayList<ObjectId>());
                historyCollection.insertOne(doc);
            }
            else {
                Document doc = cursor.next();
                ArrayList<ObjectId> arrList = (ArrayList<ObjectId>)doc.get("historyList");
                for (ObjectId id : arrList) {
                    Document d = donationCollection.find(eq("_id", id)).first();
                    Document c = charityCollection.find(eq("_id", d.getObjectId("charityId"))).first();
                    Date date = d.getDate("dateDonated");
                    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                    String historyString = "I donated "+d.getInteger("numNeeded")+ " items to "+c.getString("name")+"on "+format.format(date)+"!";
                    historyStrings.add(historyString);

                }
            }
        return historyStrings;
        }

        public void onPostExecute(Object o) {
            ArrayList<String> arrList = (ArrayList<String>) o;
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(V.getContext(), android.R.layout.simple_list_item_1, arrList);
            listView.setAdapter(adapter);

        }
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        V=inflater.inflate(R.layout.history_fragment,container,false);

        listView = (ListView) V.findViewById(R.id.realHistoryContainer);
        new GetHistoryTask().execute();

        return V;
    }

    public void update() {
        new GetHistoryTask().execute();
    }


}
