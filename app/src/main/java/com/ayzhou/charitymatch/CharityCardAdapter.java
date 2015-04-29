package com.ayzhou.charitymatch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andtinder.model.CardModel;
import com.andtinder.view.CardStackAdapter;

/**
 * Created by Alan on 3/1/2015.
 */
public class CharityCardAdapter extends CardStackAdapter {
    public CharityCardAdapter(Context context) {
        super(context);
    }

    public View getCardView(int position, CardModel m, View convertView, ViewGroup parent) {
        CharityCard model = (CharityCard) m;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.charity_card, parent, false);
            assert convertView != null;
        }

        ((ImageView) convertView.findViewById(R.id.image)).setImageDrawable(model.getCardImageDrawable());
        ((TextView) convertView.findViewById(R.id.title)).setText(model.getTitle());
        ((TextView) convertView.findViewById(R.id.description)).setText(model.getDescription());
        ((TextView) convertView.findViewById(R.id.distance)).setText(String.format("%.1f miles away,", model.getDistance()));
        ((TextView) convertView.findViewById(R.id.tagText)).setText(model.getTags());

        return convertView;
    }
}
