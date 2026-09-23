package com.cybersoft.vehicle.model;
import java.util.Arrays;
/** Immutable object containing sensitive encrypted bytes. */
public final class CustomerProfile {
    private final long id; private final String name; private final byte[] encryptedLicense;
    public CustomerProfile(long id,String name,byte[] encryptedLicense){ if(id<0) throw new IllegalArgumentException("id"); if(name==null||name.isBlank()) throw new IllegalArgumentException("name"); if(encryptedLicense==null||encryptedLicense.length==0) throw new IllegalArgumentException("license"); this.id=id; this.name=name.trim(); this.encryptedLicense=Arrays.copyOf(encryptedLicense,encryptedLicense.length); }
    public long id(){return id;} public String name(){return name;} public byte[] encryptedLicense(){return Arrays.copyOf(encryptedLicense,encryptedLicense.length);}
}
