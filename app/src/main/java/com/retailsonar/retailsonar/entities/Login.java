package com.retailsonar.retailsonar.entities;

/**
 * mini login klasse, enkel login en paswoord
 * Created by aaron on 3/12/2018.
 */
public class Login {
    private String user;
    private String password;

    public Login(String user, String password){
        this.user=user;
        this.password=password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
