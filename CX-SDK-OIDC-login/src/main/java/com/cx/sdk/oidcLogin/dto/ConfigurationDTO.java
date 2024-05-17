package com.cx.sdk.oidcLogin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class ConfigurationDTO {

    @JsonProperty("key")
    private String key;
    
    @JsonProperty("value")
    private String value;
    
    @JsonProperty("description")
    private String description;

    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}