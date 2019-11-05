package com.paresh.alert;

//import java.sql.Timestamp;

import java.util.Date;

public class EventsPost extends EventPostId{

    public String title, image_url, desc, thumb, user_image,milliTime;
    //public Date timestamp;






    public EventsPost(){}


    public EventsPost(String title, String image_url, String desc, String thumb, String user_image, String milliTime) {
        this.title = title;
        this.image_url = image_url;
        this.desc = desc;
        this.user_image = user_image;
        this.thumb = thumb;
        this.milliTime = milliTime;
        //this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUser_image(){return user_image;}

    public void setUser_image(String user_image){this.user_image = user_image;}

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getMilliTime(){return milliTime;}

    public void setMilliTime(){this.milliTime = milliTime;}

    /*public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }*/

    /*public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }*/


}
