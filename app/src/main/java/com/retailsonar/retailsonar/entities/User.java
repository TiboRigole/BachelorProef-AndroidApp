package com.retailsonar.retailsonar.entities;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * User klasse, met alle attributen != login klasse
 *
 * Created by aaron on 3/12/2018.
 */

public class User {

    private long id;
    private String login;
    private String name;
    private String address;
    private String winkel;
    private String token;
    private String group;
    private String werkRegio;

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        StringBuilder sb= new StringBuilder();
        sb.append("Bearer ").append(token);
        token=sb.toString();
        this.token = token;
    }

    public void setWerkRegio(String werkRegio){
        this.werkRegio=werkRegio;
    }

    public String getWerkRegio(){
        return werkRegio;
    }

    public String getGroup(){
        return group;
    }

    public void setGroup(String group){
        this.group=group;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWinkel() {
        return winkel;
    }

    public void setWinkel(String winkel) {
        this.winkel = winkel;
    }

    public JSONObject toJSON() {

        JSONObject jo = new JSONObject();

        try {
            jo.put("login", login);
            jo.put("name", name);
            jo.put("address", address);
            jo.put("winkel", winkel);
            jo.put("token", token);
            jo.put("group", group);
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return jo;
    }

    public User(long id, String login, String name, String address, String winkel, String token, String group) {
        this.id = id;
        this.login = login;
        this.name = name;
        this.address = address;
        this.winkel = winkel;
        this.token = token;
        this.group = group;
    }

    public User(User u){
        this.id = u.getId();
        this.login = u.getLogin();
        this.name = u.getName();
        this.address = u.getAddress();
        this.winkel = u.getWinkel();
        this.token = u.getToken();
        this.group = u.getGroup();
    }
}
