package com.alloy.dts.blade.mongo;

import com.alloy.dts.blade.DataBlade;
import com.alloy.dts.blade.DataBladesPool;
import com.alloy.dts.blade.IConnectionDetails;
import com.alloy.dts.model.dd.INativeMetadataProvider;

import java.util.List;

public class MongoDataBladesPool extends DataBladesPool {

    public MongoDataBladesPool(int poolSize) {
        super(poolSize);
    }

    private MongoMetadataProvider mongoMetadataProvider;
    private MongoConnectionDetails mongoConnectionDetails;

    @Override
    protected INativeMetadataProvider getMetadataProvider() {
        return mongoMetadataProvider;
    }

    @Override
    protected void setupMetadataBlade() throws Exception {
        setMetadataBlade(createDataBlade());
        mongoMetadataProvider = new MongoMetadataProvider((MongoDataBlade) getMetadataBlade());
        getMetadataBlade().setMetadataProvider(mongoMetadataProvider);
        getMetadataBlade().connect(getBladeCredentials());
    }

    @Override
    public DataBlade createDataBlade() {
        MongoDataBlade dataBlade = new MongoDataBlade();
        dataBlade.setPool(this);
        dataBlade.setMetadataProvider(mongoMetadataProvider);
        return dataBlade;
    }

    public IConnectionDetails  getConnectionDetails() {
        if(mongoConnectionDetails == null) {
            MongoConnectionDetails connectionDetails = new MongoConnectionDetails();
            connectionDetails.loadFrom(getConnectorEntry().getParameters());
            mongoConnectionDetails = connectionDetails;
        }
        return mongoConnectionDetails;
    }

    @Override
    public IConnectionDetails getBladeCredentials() {
        if(mongoConnectionDetails == null){
            MongoConnectionDetails _credentials = new MongoConnectionDetails();
            _credentials.loadFrom(getConnectorEntry().getParameters());
            mongoConnectionDetails = _credentials;
        }
        return mongoConnectionDetails;
    }

    @Override
    public void registerSpatialTypes() {

    }

    @Override
    public List<String> getTypeNames() {
        return List.of();
    }
}
