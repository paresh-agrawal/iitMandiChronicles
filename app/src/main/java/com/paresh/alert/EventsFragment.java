package com.paresh.alert;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventsFragment extends Fragment {


    private EventsTodayFragment eventsTodayFragment;
    private EventsUpcomingFragment eventsUpcomingFragment;
    private EventsPastFragment eventsPastFragment;
    private BottomNavigationView mainBottomNav;
    private FirebaseAuth mAuth;
    private FragmentActivity myContext;

    public EventsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_events, container, false);
        ((MainActivity) getActivity())
                .setActionBarTitle("Events");
        mAuth = FirebaseAuth.getInstance();
        mainBottomNav = view.findViewById(R.id.bottom_nav_main);

        if(mAuth.getCurrentUser()!=null) {

            eventsTodayFragment = new EventsTodayFragment();
            eventsUpcomingFragment = new EventsUpcomingFragment();
            eventsPastFragment = new EventsPastFragment();
            /*notificationFragment = new NotificationFragment();
            accountFragment = new AccountFragment();*/

            replaceFragment(eventsTodayFragment);

            mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    switch (menuItem.getItemId()) {
                        case R.id.bottom_nav_home:
                            replaceFragment(eventsTodayFragment);
                            return true;

                        case R.id.bottom_nav_notification:
                            replaceFragment(eventsUpcomingFragment);
                            return true;

                        case R.id.bottom_nav_account:
                            replaceFragment(eventsPastFragment);
                            return true;

                        default:
                            return false;
                    }
                }
            });
        }


        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = myContext.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.events_container,fragment);
        fragmentTransaction.commit();
    }


}
