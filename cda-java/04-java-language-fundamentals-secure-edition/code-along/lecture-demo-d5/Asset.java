package org.example;

public class Asset {

    private String name;
    private String owner;

    public Asset(String name) {
        this.name = name;
        this.owner = "unassigned";
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "Asset{name='" + name + "', owner='" + owner + "'}";
    }
}
