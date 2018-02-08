package sg.tringo.roadbull.views;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import sg.tringo.roadbull.R;
import sg.tringo.roadbull.model.Place;

/**
 * Created by tringo on 2/6/18.
 */

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Activity context;
    private Place place;

    public CustomInfoWindowAdapter(Activity context, Place place) {
        this.context = context;
        this.place = place;
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

        tvFrom.setText("From: " + place.getFrom());
        tvAddress.setText("Address: " + place.getAddress());
        tvPostalCode.setText("Postal code: " + place.getPostalCode());
        tvParcels.setText("Total parcels: " + place.getParcels());
        tvRemarks.setText("Remark: " + place.getRemark());

        return view;
    }
}
