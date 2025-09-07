package com.respawn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    
    private List<String> items = new ArrayList<>();
    private String employee = "";
    private String user = ""; 
    private String time = "";

    public void addItem(String name){
        items.add(name);
    }

    public void setEmployee(String e){
        employee = e;
    }

    public void setUser(String u){
        user = u;
    }

    public void setTime(String t){
        time = t;
    }

    public List<String> items(){
        return Collections.unmodifiableList(items);
    }

    public String employee(){
        return employee;
    }

    public String user(){
        return user;
    }

    public String time(){
        return time;
    }

    @Override
    public String toString(){
        return "" + items;
    }
}
