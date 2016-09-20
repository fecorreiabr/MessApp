package br.iesb.messapp.model;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Felipe on 17/09/2016.
 */
public class Device extends RealmObject {
    @PrimaryKey
    private String mac;
    private String name;

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj == null || obj.getClass() != getClass()) {
            result = false;
        } else {
            Device device = (Device) obj;
            if (this.mac.equals(device.getMac())){
                result = true;
            }
        }
        return result;
    }
}
