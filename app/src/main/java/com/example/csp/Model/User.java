package com.example.csp.Model;

public class User {

    private String uid, name, profileImage;

    //for FireBase
    public User(){

    }

    public User(String uid, String name, String profileImage) {
        this.uid = uid;
        this.name = name;
        this.profileImage = profileImage;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
