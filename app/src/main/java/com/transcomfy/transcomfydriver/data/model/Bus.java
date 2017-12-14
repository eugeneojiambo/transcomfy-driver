package com.transcomfy.transcomfydriver.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Bus implements Parcelable {

    private String id;
    private String numberPlate;
    private int maxCapacity;
    private int availableSpace;
    private Location location;
//    private List<Request> requests;
    private String driverId;

    public Bus(){

    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNumberPlate(String numberPlate) {
        this.numberPlate = numberPlate;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public void setAvailableSpace(int availableSpace) {
        this.availableSpace = availableSpace;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

//    public void setRequests(List<Request> requests) {
//        this.requests = requests;
//    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getId() {
        return id;
    }

    public String getNumberPlate() {
        return numberPlate;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public int getAvailableSpace() {
        return availableSpace;
    }

    public Location getLocation() {
        return location;
    }
//
//    public List<Request> getRequests() {
//        return requests;
//    }

    public String getDriverId() {
        return driverId;
    }

    public Bus(Parcel in){
        id = in.readString();
        numberPlate = in.readString();
        maxCapacity = in.readInt();
        availableSpace = in.readInt();
        location = in.readParcelable(Location.class.getClassLoader());
//        requests = new ArrayList<>();
//        in.readList(requests, Request.class.getClassLoader());
        driverId = in.readString();
    }

    public static final Creator CREATOR = new Creator() {
        public Bus createFromParcel(Parcel in) {
            return new Bus(in);
        }
        public Bus[] newArray(int size) {
            return new Bus[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(numberPlate);
        dest.writeInt(maxCapacity);
        dest.writeInt(availableSpace);
        dest.writeParcelable(location, flags);
//        dest.writeList(requests);
        dest.writeString(driverId);
    }

}
