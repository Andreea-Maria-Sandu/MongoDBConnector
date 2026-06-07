package com.alloy.dts.blade.mongo;

import com.alloy.dts.record.IValuesContainer;

import java.util.Map;

public class MongoDataCommand implements IValuesContainer {
    @Override
    public Map<String, Object> asMap() {
        return Map.of();
    }

    @Override
    public String getTypeKey() {
        return "";
    }
}
