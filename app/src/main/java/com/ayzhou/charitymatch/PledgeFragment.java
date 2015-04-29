package com.ayzhou.charitymatch;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by Alan on 4/8/2015.
 */
public class PledgeFragment extends Fragment {
    View V;
    private ListView listView;
    private PledgeAdapter adapter;
    private int pos;
    private ViewPager viewPager;

    public PledgeFragment(ViewPager viewPager) {
        super();
        this.viewPager = viewPager;
    }
    class PledgeAdapter extends ArrayAdapter<String> {
        private int layoutResourceId;
        private Context context;
        private ArrayList<String> data;
        private ArrayList<ObjectId> idData;
        private PledgeAdapter adapter;

        public PledgeAdapter(Context context, int layoutResourceId, ArrayList<String> data, ArrayList<ObjectId> objId) {


            super(context, layoutResourceId, data);

            this.data = data;
            this.idData = objId;
            this.layoutResourceId = layoutResourceId;
            this.context = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewGroup parentV = parent;
            final View convertViewF = convertView;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.pledge_row, parent, false);
            }
            TextView textView = (TextView) convertView.findViewById(R.id.donationString);
            Button b = (Button) convertView.findViewById(R.id.doneButton);
            textView.setText(data.get(position));
            b.setTag(idData.get(position));

            b.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    ObjectId id = (ObjectId) arg0.getTag();
                    new RemovePledgeTask().execute(id);
                    viewPager.setCurrentItem(3);
                }
            });
            return convertView;
        }
    }

    static class StringId {
        String pledgeString;
        ObjectId donationId;
    }

    private class RemovePledgeTask extends AsyncTask<Object, Object, Object> {
        protected Object doInBackground(Object... params) {
            ObjectId id = (ObjectId) params[0];
            MongoClient mongoClient = new MongoClient("ec2-52-11-161-98.us-west-2.compute.amazonaws.com");
            MongoDatabase charityDb = mongoClient.getDatabase("charities");
            MongoCollection<Document> pledgesCollection = charityDb.getCollection("pledges");
            MongoCollection<Document> historyCollection = charityDb.getCollection("history");
            MongoCollection<Document> donationsCollection = charityDb.getCollection("donations");

            //get object and remove id from arrlist
            String android_id = Settings.Secure.getString(V.getContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            Document doc= pledgesCollection.find(eq("android_id", android_id)).first();
            ArrayList<ObjectId> arrList = (ArrayList<ObjectId>)doc.get("pledgeList");
            arrList.remove(id);
            doc.remove("pledgeList");
            doc.append("pledgeList", arrList);
            pledgesCollection.updateOne(eq("_id", doc.getObjectId("_id")), new Document("$set", doc));

            //update donation object
            doc = donationsCollection.find(eq("_id", id)).first();
            doc.append("dateDonated", new Date());
            donationsCollection.updateOne(eq("_id", id), new Document("$set", doc));


            //insert into history
            MongoCursor<Document> cursor = historyCollection.find(eq("android_id", android_id)).iterator();

            //create new user account
            if (!cursor.hasNext()) {
                Document document = new Document("android_id", android_id).append("historyList", new ArrayList<ObjectId>().add(id));
                historyCollection.insertOne(document);
            } else {
                Document document = cursor.next();
                ArrayList<ObjectId> histList = (ArrayList<ObjectId>)document.get("historyList");
                histList.add(id);
                document.remove("historyList");
                document.append("historyList", histList);
                historyCollection.updateOne(eq("_id", document.getObjectId("_id")), new Document("$set", document));
            }

            return id;
    }}

     class GetPledgesTask extends AsyncTask<Object, Object, Object> {


        @Override
        protected Object doInBackground(Object... params) {
            MongoClient mongoClient = new MongoClient("ec2-52-11-161-98.us-west-2.compute.amazonaws.com");
            MongoDatabase charityDb = mongoClient.getDatabase("charities");
            MongoCollection<Document> pledgesCollection = charityDb.getCollection("pledges");
            MongoCollection<Document> donationCollection = charityDb.getCollection("donations");
            MongoCollection<Document> charityCollection = charityDb.getCollection("charities");
            String android_id = Settings.Secure.getString(V.getContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            MongoCursor<Document> cursor = pledgesCollection.find(eq("android_id", android_id)).iterator();

            HashSet<StringId> donationStrings = new HashSet<StringId>();

            //create new user account
            if (!cursor.hasNext()) {
                Document doc = new Document("android_id", android_id);
                pledgesCollection.insertOne(doc);
            } else {
                Document doc = cursor.next();
                ArrayList<ObjectId> arrList = (ArrayList<ObjectId>)doc.get("pledgeList");
                for (ObjectId id : arrList) {
                    Document d = donationCollection.find(eq("_id", id)).first();
                    Document c = charityCollection.find(eq("_id", d.getObjectId("charityId"))).first();
                    Date dueDate =  new Date();
                    dueDate.setTime(new Date().getTime()+5*24*60*60*1000);
                    String donateString = "I need to donate "+d.getInteger("numNeeded") + " items to "
                            + c.getString("name") + " by "+ dueDate.getMonth() + "/" + dueDate.getDate();
                    StringId stringId = new StringId();
                    stringId.donationId = d.getObjectId("_id");
                    stringId.pledgeString = donateString;
                    donationStrings.add(stringId);

                }

            }
            return donationStrings;

        }

        public void onPostExecute(Object o) {
            ArrayList<String> arrList = new ArrayList<String>();
            ArrayList<ObjectId> objList = new ArrayList<ObjectId>();
            for (StringId s : (HashSet<StringId>) o) {
                arrList.add(s.pledgeString);
                objList.add(s.donationId);
            }
            if (adapter != null)
                adapter.clear();
            adapter = new PledgeAdapter(V.getContext(), android.R.layout.simple_list_item_1, arrList, objList);
            listView.setAdapter(adapter);

        }
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        V=inflater.inflate(R.layout.layout_fragment,container,false);

        listView = (ListView) V.findViewById(R.id.pledgeContainer);

        new GetPledgesTask().execute();

        return V;
    }

    public void update() {
        new GetPledgesTask().execute();
    }


}
