package glory.com.navvi;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by Glory on 5/8/2017.
 */

public class GetDirectionsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        View myInflatedView=inflater.inflate(R.layout.fragment_getdirections, parent, false);
        Button buttonOne = (Button) myInflatedView.findViewById(R.id.button1);
        buttonOne.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                MapActivity mapActivity=(MapActivity) getActivity();
                mapActivity.setNavigating(true);
                mapActivity.startLocationUpdates();
            }
        });

        return myInflatedView;
    }

}
