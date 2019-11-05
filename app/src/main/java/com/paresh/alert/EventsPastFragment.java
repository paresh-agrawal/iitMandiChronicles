package com.paresh.alert;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

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
public class EventsPastFragment extends Fragment {


    private RecyclerView recyclerViewPost_past;
    private List<EventsPost> blog_list_past;
    private FirebaseFirestore firebaseFirestore;
    private EventsRecyclerAdapter blogRecyclerAdapter_past;
    private FirebaseAuth mAuth;
    //private ShimmerFrameLayout mShimmerViewContainer;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;

    public EventsPastFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_events_past, container, false);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null) {


            blog_list_past = new ArrayList<>();
            blog_list_past.clear();
            recyclerViewPost_past = view.findViewById(R.id.recycler_view_post_past);
            blogRecyclerAdapter_past = new EventsRecyclerAdapter(blog_list_past);
            blogRecyclerAdapter_past.notifyDataSetChanged();

            /*StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL);
            layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
            recyclerViewPost_past.setLayoutManager(layoutManager);
            */

            recyclerViewPost_past.setLayoutManager(new LinearLayoutManager(getActivity()));
            //recyclerViewPost_past.setHasFixedSize(false);
            recyclerViewPost_past.setAdapter(blogRecyclerAdapter_past);

            /*blog_list_past = new ArrayList<>();
            blog_list_past.clear();
            recyclerViewPost_past = view.findViewById(R.id.recycler_view_post_past);
            blogRecyclerAdapter_past = new EventsRecyclerAdapter(blog_list_past);
            blogRecyclerAdapter_past.notifyDataSetChanged();
            recyclerViewPost_past.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerViewPost_past.setAdapter(blogRecyclerAdapter_past);*/


            if (mAuth.getCurrentUser()!=null) {
                firebaseFirestore = FirebaseFirestore.getInstance();

                recyclerViewPost_past.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                        if (reachedBottom){
                            nextQuery();
                        }
                    }
                });
                /*recyclerViewPost_past.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                                        String pastDateString = df.format(c);

                                        try {
                                            dateToday = formatter.parse(pastDateString);
                                        } catch (ParseException e1) {
                                            e1.printStackTrace();
                                        }

                                        if(datePost.before(dateToday)) {
                                            Log.d("past", "Event");

                                            if (isFirstPageFirstLoad) {
                                                blog_list_past.add(eventsPost);
                                            }else {
                                                blog_list_past.add(0,eventsPost);
                                            }

                                        }

                                        blogRecyclerAdapter_past.notifyDataSetChanged();

                                    /*mShimmerViewContainer.stopShimmerAnimation();
                                    mShimmerViewContainer.setVisibility(View.GONE);*/
                                    }
                                }
                                //isFirstPageFirstLoad = false;
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
                                String pastDateString = df.format(c);

                                try {
                                    dateToday = formatter.parse(pastDateString);
                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                }

                                if(datePost.before(dateToday)) {
                                    Log.d("past", "Event");
                                    blog_list_past.add(eventsPost);

                                }
                                blogRecyclerAdapter_past.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }
        });
    }

}
