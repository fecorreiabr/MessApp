package br.iesb.messapp;

import java.io.Serializable;
import java.util.UUID;

import io.realm.RealmObject;

/**
 * Created by Felipe on 04/09/2016.
 */
public class User extends RealmObject {

    private String id;
    private String email;
    private String name;
    private String pwd;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

}
