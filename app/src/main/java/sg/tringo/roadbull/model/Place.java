package sg.tringo.roadbull.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by tringo on 2/8/18.
 */

public class Place {

    private String from;
    private String address;
    private String postalCode;
    private String parcels;
    private String remark;
    private LatLng geo;

    public Place(String from, String address, String postalCode, String parcels, String remark, LatLng geo) {
        this.from = from;
        this.address = address;
        this.postalCode = postalCode;
        this.parcels = parcels;
        this.remark = remark;
        this.geo = geo;
    }

    public String getFrom() {
        return from;
    }

    public String getAddress() {
        return address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getParcels() {
        return parcels;
    }

    public String getRemark() {
        return remark;
    }

    public LatLng getGeo() {
        return geo;
    }
}
