package models;

import java.io.Serializable;

/**
 * Created by PaulM on 10/06/2017.
 */

public class User implements Serializable{
    private String name, email, password, country, img;
    public User(){}

    public User(String name, String email, String password, String country, String img) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.country = country;
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getCountry() {
        return country;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
