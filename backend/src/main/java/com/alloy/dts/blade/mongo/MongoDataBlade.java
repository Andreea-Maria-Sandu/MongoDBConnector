package com.alloy.dts.blade.mongo;

import com.alloy.dts.ControllerPasswordHelper;
import com.alloy.dts.DTSException;
import com.alloy.dts.blade.DataBlade;
import com.alloy.dts.blade.IConnectionDetails;
import com.alloy.dts.model.DTSCollection;
import com.alloy.dts.model.DTSPredicate;
import com.alloy.dts.model.DTSRemote;
import com.alloy.dts.model.ResourceIdentifier;
import com.alloy.dts.model.dd.DTSCollectionMapping;
import com.alloy.dts.record.ADTSRecordValuesProvider;
import com.alloy.dts.record.DTSClassFactory;
import com.alloy.dts.record.DTSRecordStream;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.util.List;

public class MongoDataBlade extends DataBlade {
    private String dbName;
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private DTSClassFactory dtsClassFactory;

    public DTSClassFactory getClassFactoryForObjectTypes() {
        if (dtsClassFactory == null) {
            dtsClassFactory = new DTSClassFactory(getTypesCluster());
        }
        return dtsClassFactory;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    public void setMongoDatabase(String dbName) {
        mongoDatabase = mongoClient.getDatabase(dbName);
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public MongoDataBlade() {
    }

    public String constructConnectionString(MongoConnectionDetails mongoConnectionDetails) throws DTSException {
        if (mongoConnectionDetails.getConnectionString() == null || mongoConnectionDetails.getConnectionString().isBlank()) {
            throw new DTSException("Connection string cannot be empty");
        }
        String connectionString = mongoConnectionDetails.getConnectionString().trim();
        if (!connectionString.matches("^.+://.*")) {
            connectionString = "mongodb://" + connectionString;
        }
        connectionString = connectionString.replace(
                "://",
                "://" + mongoConnectionDetails.getUsername() + ":" + mongoConnectionDetails.getPassword() + "@"
        );
        return connectionString;
    }

    @Override
    public void connect(IConnectionDetails iConnectionDetails) throws Exception {
        MongoConnectionDetails mongoConnectionDetails = (MongoConnectionDetails) iConnectionDetails;
        mongoClient = MongoClients.create(constructConnectionString(mongoConnectionDetails));
        mongoDatabase = mongoClient.getDatabase(mongoConnectionDetails.getDbName());
        getMetadataProvider().getMinimalAllCollectionsMetadata();
    }

    @Override
    protected void buildNativeRemoteSupportFor(DTSRemote dtsRemote) throws DTSException {

    }

    @Override
    public ADTSRecordValuesProvider getRecordValuesProviderImplementation() {
        return null;
    }

    @Override
    public String composeTypeKeyName(String name) {
        return name;
    }

    @Override
    protected void setupTypeBuilder() {
        setTypeBuilder(new MongoDTSTypeBuilder());
    }

    @Override
    protected DTSRecordStream newRecordStream(DTSCollection collection) {
        DTSRecordStream stream = new MongoRecordStream(collection);
        return stream;
    }

    @Override
    public DTSRecordStream newRecordStream(String collectionKey, DTSPredicate filter) throws DTSException {
        MongoRecordStream stream = (MongoRecordStream) newRecordStream(getCollection(collectionKey));
        try {
            stream.open(filter);
        } catch (Exception e) {
            throw new DTSException("Failed to open stream on " + stream.getOwner().getName(), e);
        }

        registerStream(stream);

        return stream;
    }
}
