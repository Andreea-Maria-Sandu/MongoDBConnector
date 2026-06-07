package com.alloy.dts.blade.mongo;

import com.alloy.dts.record.IValuesContainer;

import java.util.Map;

public class MongoDTSPredicate implements IValuesContainer {

    public static final String DTS_TYPE_KEY = "DTS_MONGO_PREDICATE";

    @Override
    public Map<String, Object> asMap() {
        return Map.of();
    }

    @Override
    public String getTypeKey() {
        return DTS_TYPE_KEY;
    }
}
