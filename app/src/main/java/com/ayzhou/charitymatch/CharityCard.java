package com.ayzhou.charitymatch;

import android.graphics.drawable.Drawable;

import com.andtinder.model.CardModel;

import org.bson.types.ObjectId;

import java.util.Date;

/**
 * Created by Alan on 3/1/2015.
 */
public class CharityCard extends CardModel {
    private double distance;
    private String tags;
    public Date beginDate;
    public Date endDate;
    public Date lastDonation;
    public int n;
    public int numNeeded;
    public double recentlyDonatedtoRank;
    public double distanceRank;
    public double numNeededRank;
    public double percentangeDone;
    public ObjectId charityID;
    public ObjectId donationID;


    public CharityCard(String title, String description, Drawable image, double d, String tags) {
        super(title, description, image);
        distance = d;
        this.tags = tags;
    }

    public void setDistance(double d) {
        distance = d;
    }

    public double getDistance() {
        return distance;
    }

    public String getTags() {
        return tags;
    }

    public void setBeginDate(Date d) {
        this.beginDate = d;
    }

    public void setEndDate(Date d) {
        this.endDate = d;
    }

    public void setLastDonation(Date d) {
        this.lastDonation = d;
    }
    public void setN(int n) {
        this.n = n;
    }

    public void setNumNeeded(int n) {
        this.numNeeded = n;
    }


}
