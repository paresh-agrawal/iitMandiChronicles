package com.paresh.alert;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventsTodayFragment extends Fragment {


    private RecyclerView recyclerViewPost_today,recyclerViewPost_upcoming;
    private List<EventsPost> blog_list_today,blog_list_upcoming;
    private FirebaseFirestore firebaseFirestore;
    private EventsRecyclerAdapter blogRecyclerAdapter_today,blogRecyclerAdapter_upcoming;
    private FirebaseAuth mAuth;
    //private ShimmerFrameLayout mShimmerViewContainer;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;
    private TextView tvNoPostToday;

    public EventsTodayFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_events_today, container, false);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null) {

            blog_list_today = new ArrayList<>();
            blog_list_today.clear();
            recyclerViewPost_today = view.findViewById(R.id.recycler_view_post_today);
            blogRecyclerAdapter_today = new EventsRecyclerAdapter(blog_list_today);
            blogRecyclerAdapter_today.notifyDataSetChanged();
            recyclerViewPost_today.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerViewPost_today.setAdapter(blogRecyclerAdapter_today);
            tvNoPostToday = view.findViewById(R.id.tv_no_post_today);

            /*blog_list_upcoming = new ArrayList<>();
            blog_list_upcoming.clear();
            recyclerViewPost_upcoming = view.findViewById(R.id.recycler_view_post_upcoming);
            blogRecyclerAdapter_upcoming = new EventsRecyclerAdapter(blog_list_upcoming);
            blogRecyclerAdapter_upcoming.notifyDataSetChanged();
            recyclerViewPost_upcoming.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerViewPost_upcoming.setAdapter(blogRecyclerAdapter_upcoming);*/


            if (mAuth.getCurrentUser()!=null) {
                firebaseFirestore = FirebaseFirestore.getInstance();

                recyclerViewPost_today.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                        if (reachedBottom){
                            nextQuery();
                        }
                    }
                });
                /*recyclerViewPost_upcoming.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                        if (reachedBottom){
                            nextQuery();
                        }
                    }
                });*/

                Query firstQuery = firebaseFirestore.collection("Posts")
                        .orderBy("milliTime",Query.Direction.DESCENDING)
                        .limit(6);
                firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if(mAuth.getCurrentUser()!=null) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                tvNoPostToday.setVisibility(View.GONE);
                                recyclerViewPost_today.setVisibility(View.VISIBLE);
                                if(isFirstPageFirstLoad) {
                                    lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                }
                                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                    if (doc.getType() == DocumentChange.Type.ADDED) {

                                        String eventPostId = doc.getDocument().getId();
                                        EventsPost eventsPost = doc.getDocument().toObject(EventsPost.class).withId(eventPostId);
                                        //Log.d("title", eventsPost.getDesc());
                                        Date datePost=null,dateToday=null;
                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

                                        String postDateString = formatter.format(new Date(Long.parseLong(eventsPost.getMilliTime())));
                                        try {
                                            datePost = formatter.parse(postDateString);
                                        } catch (ParseException e1) {
                                            e1.printStackTrace();
                                        }

                                        Date c = Calendar.getInstance().getTime();
                                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                                        String todayDateString = df.format(c);

                                        try {
                                            dateToday = formatter.parse(todayDateString);
                                        } catch (ParseException e1) {
                                            e1.printStackTrace();
                                        }


                                        if(datePost.equals(dateToday)) {
                                            Log.d("Today", "Event");
                                            blog_list_today.add(eventsPost);
                                            /*if (isFirstPageFirstLoad) {

                                            }else {
                                                blog_list_today.add(0,eventsPost);
                                            }*/

                                        }
                                        blogRecyclerAdapter_today.notifyDataSetChanged();

                                    /*mShimmerViewContainer.stopShimmerAnimation();
                                    mShimmerViewContainer.setVisibility(View.GONE);*/
                                    }
                                }
                                isFirstPageFirstLoad = false;
                                if(blog_list_today.isEmpty()){
                                    tvNoPostToday.setVisibility(View.VISIBLE);
                                    recyclerViewPost_today.setVisibility(View.GONE);

                                }else{
                                    tvNoPostToday.setVisibility(View.GONE);
                                    recyclerViewPost_today.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                });
            }
        }

        return view;
    }

    public void nextQuery(){
        Query nextQuery = firebaseFirestore.collection("Posts")
                .orderBy("milliTime",Query.Direction.DESCENDING)
                .startAfter(lastVisible).limit(6);
        nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if(mAuth.getCurrentUser()!=null) {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String eventPostId = doc.getDocument().getId();
                                EventsPost eventsPost = doc.getDocument().toObject(EventsPost.class).withId(eventPostId);
                                Date datePost=null,dateToday=null;
                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                                String postDateString = formatter.format(new Date(Long.parseLong(eventsPost.getMilliTime())));
                                try {
                                    datePost = formatter.parse(postDateString);
                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                }

                                Date c = Calendar.getInstance().getTime();
                                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                                String todayDateString = df.format(c);

                                Log.d(postDateString, todayDateString);

                                try {
                                    dateToday = formatter.parse(todayDateString);
                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                }

                                if(datePost.equals(dateToday)) {
                                    Log.d("Today", "Event");
                                    blog_list_today.add(eventsPost);

                                }
                                blogRecyclerAdapter_today.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }
        });
    }

}
