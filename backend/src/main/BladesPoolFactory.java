package com.alloy.dts.blade;

import com.alloy.dts.blade.mongo.MongoDataBladesPool;

public class BladesPoolFactory implements IDataBladesPoolFactory {
    @Override
    public DataBladesPool createPool(int size) {
        return new MongoDataBladesPool(size);
    }
}
