package com.stratio.intelligence.dto;


import org.springframework.stereotype.Component;

@Component
public class VolumeConfiguration {
    private String instance;
    private String owner;
    private String ownerGroup;

    public VolumeConfiguration() {
    }

    public String getOwnerGroup() {
        return ownerGroup;
    }

    public void setOwnerGroup(String ownerGroup) {
        this.ownerGroup = ownerGroup;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

}
