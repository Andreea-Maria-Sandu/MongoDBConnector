package com.alloy.dts.blade.mongo;

public abstract class MongoTypeDecorator {
    private final MongoTypeMetadata owner;

    protected MongoTypeDecorator(MongoTypeMetadata owner) {
        this.owner = owner;
        owner.setDecorator(this);
    }
    public MongoTypeMetadata getOwner() {
        return owner;
    }
}
