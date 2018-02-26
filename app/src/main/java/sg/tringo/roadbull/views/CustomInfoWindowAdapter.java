package sg.tringo.roadbull.views;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

import sg.tringo.roadbull.R;
import sg.tringo.roadbull.model.Place;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Activity context;
    private List<Place> places;

    public CustomInfoWindowAdapter(Activity context, List<Place> places) {
        this.context = context;
        this.places = places;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = context.getLayoutInflater().inflate(R.layout.custom_info_window, null);

        TextView tvFrom = (TextView) view.findViewById(R.id.tv_from);
        TextView tvAddress = (TextView) view.findViewById(R.id.tv_address);
        TextView tvPostalCode = (TextView) view.findViewById(R.id.tv_postal_code);
        TextView tvParcels = (TextView) view.findViewById(R.id.tv_parcels);
        TextView tvRemarks = (TextView) view.findViewById(R.id.tv_remarks);

        for (Place place : places) {
            if (isPositionEqual(marker.getPosition(), place.getGeo()) ) {
                tvFrom.setText("From: " + place.getFrom());
                tvAddress.setText("Address: " + place.getAddress());
                tvPostalCode.setText("Postal code: " + place.getPostalCode());
                tvParcels.setText("Total parcels: " + place.getParcels());
                tvRemarks.setText("Remark: " + place.getRemark());
            }
        }

        return view;
    }

    private boolean isPositionEqual(LatLng lhs, LatLng rhs) {
        return lhs.latitude == rhs.latitude && lhs.longitude == rhs.longitude;
    }
}
