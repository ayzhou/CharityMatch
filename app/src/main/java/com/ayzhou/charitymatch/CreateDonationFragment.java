package com.ayzhou.charitymatch;

import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Alan on 3/28/2015.
 */
public class CreateDonationFragment extends Fragment {

    private TableLayout con;
    private TableRow currentRow;
    private int numRows;
    private int numElementsInRow;
    private String[] tags;
    private ViewPager viewPager;
    private View V;

    public CreateDonationFragment(ViewPager viewPager) {
        super();
        this.viewPager = viewPager;
    }

    public CreateDonationFragment() {
        super();
    }

    class getTagsTask extends AsyncTask<Object, Object, Object> {
        protected Object doInBackground(Object... args) {
            //get mongo objects
            MongoClient mongoClient = new MongoClient("ec2-52-11-161-98.us-west-2.compute.amazonaws.com");
            MongoDatabase charityDb = mongoClient.getDatabase("charities");

            MongoCollection<Document> charities = charityDb.getCollection("tags");

            MongoCursor<Document> cursor = charities.find().iterator();

            ArrayList<String> list = new ArrayList<String>();
            System.out.println("here");

            try {
                while (cursor.hasNext()) {
                    Document d = cursor.next();
                    list.add(d.getString("name"));
                }

            } finally {
                cursor.close();
            }

            tags = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                tags[i] = list.get(i);
            }
            return list;
        }

        protected void onPostExecute(Object o) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_dropdown_item_1line, tags);
                System.out.println(Arrays.toString(tags));

                AutoCompleteTextView autocomplete = (AutoCompleteTextView) V.findViewById(R.id.autocomplete);
                autocomplete.setAdapter(adapter);


        }

    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        V = inflater.inflate(R.layout.createdonation_fragment, container, false);
        new getTagsTask().execute();


        //adds the tags into the table
        con = (TableLayout) V.findViewById(R.id.tagscontainer);
        con.addView(new TableRow(getActivity()));
        currentRow = (TableRow) con.getChildAt(0);
        Button button = (Button) V.findViewById(R.id.addtag);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                AutoCompleteTextView autocomplete = (AutoCompleteTextView) V.findViewById(R.id.autocomplete);
                String s = autocomplete.getText().toString();

                TextView tV = new TextView(getActivity());
                tV.setPadding(30, 0, 30, 0);
                tV.setBackgroundResource(R.drawable.backtext);
                tV.setText(s);

                if (numElementsInRow > 5) {
                    con.addView(new TableRow(getActivity()));
                    currentRow = (TableRow) con.getChildAt(++numRows);
                    numElementsInRow = 0;
                }

                currentRow.addView(tV);
                numElementsInRow++;
            }
        });

        //button to match with charities
        Button matchbutton = (Button)V.findViewById(R.id.matchbutton);
        matchbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DonationFragment f = (DonationFragment) getActivity().getSupportFragmentManager().findFragmentByTag(getFragmentTag(1));

                ArrayList<String> tags = new ArrayList<String>();
                for (int i = 0; i < con.getChildCount(); i++) {
                    View child = con.getChildAt(i);
                    if (child instanceof TableRow) {
                        for (int j = 0; j < ((TableRow) child).getChildCount(); j++) {
                            View tvChild = (TableRow) child;
                            View tV = ((TableRow)tvChild).getChildAt(j);
                            if (tV instanceof TextView) {
                                tags.add(((TextView) tV).getText().toString());
                                }
                            }
                        }

                 }
                String[] tagsArr = new String[tags.size()];
                for (int i = 0; i < tagsArr.length; i++) {
                    tagsArr[i] = tags.get(i);
                }
                f.updateCards(tagsArr);
                viewPager.setCurrentItem(1);
                }


        });



        return V;
    }
    private String getFragmentTag(int pos){
        return "android:switcher:"+R.id.pager+":"+pos;
    }


}
