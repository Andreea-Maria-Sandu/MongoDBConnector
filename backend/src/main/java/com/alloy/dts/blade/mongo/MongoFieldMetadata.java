package com.alloy.dts.blade.mongo;

import com.alloy.dts.blade.DataBlade;
import com.alloy.dts.model.ResourceIdentifier;
import com.alloy.dts.model.dd.NativeFieldMetadata;

public class MongoFieldMetadata extends NativeFieldMetadata {
    private String bsonType;
    private Boolean required;
    private String itemsType;

    public MongoFieldMetadata(ResourceIdentifier identifier, ResourceIdentifier type, DataBlade dataSource) {
        super(identifier, type, dataSource);
    }

    public void setItemsType(String type){
        if(isArray()){
            itemsType = type;
        }
    }

    @Override
    public String getDataLength() {
        return itemsType;
    }

    @Override
    public String getDataPrecision() {
        return "";
    }

    @Override
    public String getDataScale() {
        return "";
    }

    @Override
    public String getDefaultValue() {
        return "";
    }

    @Override
    public Integer getPosition() {
        return 0;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getBsonType() {
        return bsonType;
    }

    public void setBsonType(String bsonType) {
        this.bsonType = bsonType;
    }
}
