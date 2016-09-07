package br.iesb.messapp;

import java.util.UUID;

import io.realm.RealmObject;

/**
 * Created by Felipe on 07/09/2016.
 */
public class Contact extends RealmObject {
    private String id;
    private String email;
    private String name;
    private String phone;
    private String skypeId;
    private String avatarPicture;

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSkypeId() {
        return skypeId;
    }

    public void setSkypeId(String skypeId) {
        this.skypeId = skypeId;
    }

    public String getAvatarPicture() {
        return avatarPicture;
    }

    public void setAvatarPicture(String avatarPicture) {
        this.avatarPicture = avatarPicture;
    }

    public Contact() {
        this.id = UUID.randomUUID().toString();
    }
}
