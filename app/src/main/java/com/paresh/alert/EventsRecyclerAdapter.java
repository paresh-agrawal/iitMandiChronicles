package com.paresh.alert;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class EventsRecyclerAdapter extends RecyclerView.Adapter<EventsRecyclerAdapter.ViewHolder> {

    public List<EventsPost>blog_list;
    public Context context;
    public FirebaseFirestore firebaseFirestore;
    public FirebaseAuth mAuth;
    public String name,user_image;



    public EventsRecyclerAdapter(List<EventsPost> blog_list){
        this.blog_list = blog_list;
    }

    @NonNull
    @Override
    public EventsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.evetns_list_item, viewGroup, false);

        context = viewGroup.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final EventsRecyclerAdapter.ViewHolder viewHolder, int i) {

        viewHolder.setIsRecyclable(false);

        final String event_post_id = blog_list.get(i).EventPostId;
        final String currentUserId = mAuth.getCurrentUser().getUid();
        final String desc_data = blog_list.get(i).getDesc();
        String image_url = blog_list.get(i).getImage_url();
        String thumb_url = blog_list.get(i).getThumb();
        final String title = blog_list.get(i).getTitle();
        String user_image_url = blog_list.get(i).getUser_image();
        final String milliTime = blog_list.get(i).getMilliTime();
        //long millisec = blog_list.get(i).getTimestamp().getTime();

        //String dateString = DateFormat.format("MM/dd/yyyy", new Date(millisec)).toString();

        /*firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    name = task.getResult().getString("name");
                    user_image = task.getResult().getString("image");
                    viewHolder.setPostTitle(name);
                    viewHolder.setUserImage(user_image);
                }else{
                    String error = task.getException().getMessage();
                    Toast.makeText(context, "Error : "+error, Toast.LENGTH_SHORT).show();
                }
            }
        });*/
        Log.d("url", image_url);
        viewHolder.setPostTitle(title);
        viewHolder.descText.setText(desc_data);
        viewHolder.setPostImage(image_url, thumb_url);
        viewHolder.setUserImage(user_image_url);
        viewHolder.setTime(milliTime);

        final int[] lineCount = new int[1];
        viewHolder.descText.post(new Runnable() {
            @Override
            public void run() {
                lineCount[0] = viewHolder.descText.getLineCount();
                // Use lineCount here
                Log.d(viewHolder.postTitle.getText().toString(), String.valueOf(lineCount[0]));
                if (lineCount[0] <=4){
                    viewHolder.seeMoreText.setVisibility(View.GONE);
                    viewHolder.seeMoreText.setClickable(false);
                    viewHolder.seeMoreText.setFocusable(false);
                }else {
                    viewHolder.seeMoreText.setVisibility(View.VISIBLE);
                    viewHolder.seeMoreText.setClickable(true);
                    viewHolder.seeMoreText.setFocusable(true);
                    viewHolder.descText.setMaxLines(4);
                }
            }
        });




        if (viewHolder.seeMoreText.isClickable()){
            viewHolder.seeMoreText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("clicked","yes");
                    String see_more_less = viewHolder.seeMoreText.getText().toString();
                    if (see_more_less.toLowerCase().contains("more")){
                        viewHolder.seeMoreText.setText(R.string.see_less);
                        viewHolder.descText.setMaxLines(lineCount[0]);
                    }else {
                        viewHolder.seeMoreText.setText(R.string.see_more);
                        viewHolder.descText.setMaxLines(4);
                    }

                }
            });

        }

        //viewHolder.setTime(dateString);

        if (mAuth.getCurrentUser()!=null) {
            firebaseFirestore.collection("Posts").document(event_post_id).collection("Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if(mAuth.getCurrentUser()!=null){
                        if(!queryDocumentSnapshots.isEmpty()){
                            int count = queryDocumentSnapshots.size();
                            viewHolder.updateLikesCount(count);
                        }else{
                            viewHolder.updateLikesCount(0);
                        }
                    }
                }
            });
        }


        if (mAuth.getCurrentUser()!=null) {
            firebaseFirestore.collection("Posts").document(event_post_id).collection("Likes").document(currentUserId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if(mAuth.getCurrentUser()!=null) {
                                if (documentSnapshot.exists()) {
                                    viewHolder.reactBtn.setImageDrawable(context.getDrawable(R.drawable.ic_favorite_red));
                                } else {
                                    viewHolder.reactBtn.setImageDrawable(context.getDrawable(R.drawable.ic_favorite_black));
                                }
                            }
                        }
                    });

        }
        viewHolder.reactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser()!=null) {




                    firebaseFirestore.collection("Posts").document(event_post_id).collection("Likes").document(currentUserId).get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (mAuth.getCurrentUser()!=null) {
                                        if (!task.getResult().exists()) {
                                            Map<String, Object> likeMap = new HashMap<>();
                                            likeMap.put("timestamp", FieldValue.serverTimestamp());

                                            firebaseFirestore.collection("Posts").document(event_post_id).collection("Likes").document(currentUserId).set(likeMap);
                                        } else {
                                            firebaseFirestore.collection("Posts").document(event_post_id).collection("Likes").document(currentUserId).delete();
                                        }
                                    }
                                }
                            });
                }


            }
        });


        if (mAuth.getCurrentUser()!=null) {
            firebaseFirestore.collection("Posts").document(event_post_id).collection("Bookmark").document(currentUserId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if(mAuth.getCurrentUser()!=null) {
                                if (documentSnapshot.exists()) {
                                    viewHolder.addBookmark.setImageDrawable(context.getDrawable(R.drawable.ic_bookmark_yellow));
                                } else {
                                    viewHolder.addBookmark.setImageDrawable(context.getDrawable(R.drawable.ic_bookmark));
                                }
                            }
                        }
                    });

        }
        viewHolder.addBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser()!=null) {




                    firebaseFirestore.collection("Posts").document(event_post_id).collection("Bookmark").document(currentUserId).get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (mAuth.getCurrentUser()!=null) {
                                        if (!task.getResult().exists()) {
                                            Map<String, Object> likeMap = new HashMap<>();
                                            likeMap.put("timestamp", FieldValue.serverTimestamp());

                                            firebaseFirestore.collection("Posts").document(event_post_id).collection("Bookmark").document(currentUserId).set(likeMap);
                                        } else {
                                            firebaseFirestore.collection("Posts").document(event_post_id).collection("Bookmark").document(currentUserId).delete();
                                        }
                                    }
                                }
                            });
                }


            }
        });

        viewHolder.addReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)!= PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_CALENDAR},1);
                    } else{
                        if(ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)!= PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_CALENDAR},1);
                        }else{
                            //Toast.makeText(SetupActivity.this, "Everything fine", Toast.LENGTH_SHORT).show();
                            setReminder(milliTime,title,desc_data);
                        }
                    }
                }else{
                    setReminder(milliTime,title,desc_data);
                }

            }
        });
    }

    private void setReminder(String milliTime, String title, String desc_data) {
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra("beginTime", Long.parseLong(milliTime));
        intent.putExtra("allDay", false);
        intent.putExtra("rule", "FREQ=DAILY");
        intent.putExtra("title", title);
        intent.putExtra("description", desc_data);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView descText, postTitle, postDate, seeMoreText, reactCount, reactCountText;
        private ImageView postImageView, postUserImageView, reactBtn, addReminder, addBookmark;
        private RelativeLayout relativeLayout;
        //private CircleImageView userImageView;
        private int width, height, lineCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            postImageView = mView.findViewById(R.id.iv_poster);
            postImageView.setPadding(0,0,0,0);
            postUserImageView = mView.findViewById(R.id.iv_user_image);
            postUserImageView.setPadding(0,0,0,0);
            postTitle = mView.findViewById(R.id.tv_title);
            postDate = mView.findViewById(R.id.tv_date);
            addReminder = mView.findViewById(R.id.iv_add_reminder);
            seeMoreText = mView.findViewById(R.id.tv_see_more);
            addBookmark = mView.findViewById(R.id.iv_add_bookmark);
            //userImageView = mView.findViewById(R.id.blog_user_image);
            descText = mView.findViewById(R.id.tv_desc);
            reactBtn = mView.findViewById(R.id.iv_love_react);
            reactCount = mView.findViewById(R.id.tv_react_count);
            relativeLayout = mView.findViewById(R.id.relativeLayout);

            /*RelativeLayout.LayoutParams params = new
                    RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            // Set the height by params
            // set height of RecyclerView
            relativeLayout.setLayoutParams(params);*/
            /*int recyclerViewWidth = 0;
            mView.measure(
                    View.MeasureSpec.makeMeasureSpec(recyclerViewWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            Log.d("width", String.valueOf(recyclerViewWidth));*/


            /*ViewTreeObserver vto = postImageView.getViewTreeObserver();
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    postImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                    width = postImageView.getMeasuredHeight();
                    return true;
                }
            });
            Log.d("h"+String.valueOf(width),"w"+String.valueOf(width));*/
        }

        public void setDescText(String text){
            descText.setText(text);
            /*descText.post(new Runnable() {
                @Override
                public void run() {
                    lineCount = descText.getLineCount();
                    // Use lineCount here
                    Log.d(postTitle.getText().toString(), String.valueOf(lineCount));
                    if (lineCount<=1){
                        seeMoreText.setVisibility(View.GONE);
                        seeMoreText.setClickable(false);
                        seeMoreText.setFocusable(false);
                    }else {
                        seeMoreText.setVisibility(View.VISIBLE);
                        seeMoreText.setClickable(true);
                        seeMoreText.setFocusable(true);
                        descText.setMaxLines(1);
                    }
                }
            });

            if (seeMoreText.isClickable()){
                seeMoreText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("clicked","yes");
                        String see_more_less = seeMoreText.getText().toString();
                        if (see_more_less.toLowerCase().contains("more")){
                            seeMoreText.setText(R.string.see_less);
                            descText.setMaxLines(lineCount);
                        }else {
                            seeMoreText.setText(R.string.see_more);
                            descText.setMaxLines(1);
                        }

                    }
                });

            }*/
        }

        public void setPostImage(final String imageUri, String thumUri){

            /*int width = postImageView.getWidth();
            postImageView.getLayoutParams().height = (int)Math.round(width*3.0/4.0);
            int height = postImageView.getHeight();
            Log.d("h"+String.valueOf(height),"w"+String.valueOf(width));
            Log.d("download ", downloadUri);*/
            RequestOptions placeHolderOption = new RequestOptions();
            placeHolderOption.placeholder(R.drawable.placeholder);
            /*Picasso.get()
                    .load(imageUri)
                    .placeholder(R.drawable.placeholder)
                    .into(postImageView);*/
            Picasso.get()
                    .load(thumUri) // thumbnail url goes here
                    .placeholder(R.drawable.placeholder)
                    .into(postImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            Picasso.get()
                                    .load(imageUri) // image url goes here
                                    .placeholder(postImageView.getDrawable())
                                    .into(postImageView);
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });


            /*Glide.with(context).applyDefaultRequestOptions(placeHolderOption).load(imageUri)
                    .thumbnail(Glide.with(context).load(thumUri)).into(postImageView);*/
            //Glide.with(context).load(downloadUri).dontAnimate().into(view);

        }

        public void setPostTitle(String name){
            postTitle.setText(name);
        }

        public void setUserImage(String userImageUrl){
            Picasso.get()
                    .load(userImageUrl)
                    .placeholder(R.drawable.ic_account_circle)
                    .into(postUserImageView);
        }

        /*public void setUserImage(String profileImageUri){
            Picasso.get().load(profileImageUri).placeholder(R.drawable.ic_account).into(userImageView);
        }*/

        public void setTime(String date){
            SimpleDateFormat formatter = new SimpleDateFormat("EEE dd-MMM-yy HH:mm ");
            String dateString = formatter.format(new Date(Long.parseLong(date)));
            //Log.d("Date", dateString);
            postDate.setText(dateString);
        }

        public void updateLikesCount(int count){
            reactCount = mView.findViewById(R.id.tv_react_count);
            //reactCountText = mView.findViewById(R.id.tv_react_count_text);
            if(count>0){
                reactCount.setVisibility(View.VISIBLE);
                if (count==1){
                    reactCount.setText(String.valueOf(count)+" Person likes this Event");
                }else{
                    reactCount.setText(String.valueOf(count)+" People like this Event");
                }


            }else {
                reactCount.setText(String.valueOf(count));
                reactCount.setVisibility(View.GONE);
            }
        }
    }
}
