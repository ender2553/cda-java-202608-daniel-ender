package org.example;

import java.util.Objects;

public class Asset {

    private String name;
    private String owner;


    //Constructor
    public Asset(){  //Default/empty param constructor

    }

    public Asset(String name) {
        this.name = name;
        this.owner = "unassigned";
    }

    //Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    // custom methods



    @Override
    public String toString() {
        String formattedOutput = "Asset{name='" + name + "', owner='" + owner + "'}";
        return formattedOutput;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Asset asset = (Asset) o;
        return Objects.equals(name, asset.name) && Objects.equals(owner, asset.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, owner);
    }


}
