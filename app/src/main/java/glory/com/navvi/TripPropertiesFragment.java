package glory.com.navvi;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.TextView;

/**
 * Created by Glory on 5/9/2017.
 */

public class TripPropertiesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        String dist = getArguments().getString("dist");
        View myInflatedView=inflater.inflate(R.layout.fragment_tripproperties, parent, false);
        TextView t = (TextView) myInflatedView.findViewById(R.id.total_distance);
        t.setText(dist);

        return myInflatedView;
    }

}
