package com.alloy.dts.blade.mongo;

import com.alloy.dts.model.ResourceIdentifier;

public class MongoAttributeMetadata {
    private final ResourceIdentifier resourceIdentifier;
    private String attributeName;
    private String attributeValue;
    private MongoObjectDecorator owner;

    public MongoAttributeMetadata(ResourceIdentifier resourceIdentifier) {
        this.resourceIdentifier = resourceIdentifier;
    }

    public ResourceIdentifier getResourceIdentifier() {
        return resourceIdentifier;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public MongoObjectDecorator getOwner() {
        return owner;
    }

    public void setOwner(MongoObjectDecorator owner) {
        this.owner = owner;
    }
}
