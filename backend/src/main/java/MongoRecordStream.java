package com.alloy.dts.blade.mongo;
import com.alloy.dts.model.DTSPredicate;
import com.alloy.dts.model.IStreamOwner;
import com.alloy.dts.model.ResourceIdentifier;
import com.alloy.dts.record.ADTSRecordValuesProvider;
import com.alloy.dts.record.DTSRecord;
import com.alloy.dts.record.DTSRecordStream;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MongoRecordStream extends DTSRecordStream {
    private static MongoDataBlade mongoDataBlade;
    private final ADTSRecordValuesProvider dataProvider;
    private MongoCursor<Document> cursor;
    private boolean fullObject = false;
    private boolean addProp = true;
    private MongoQuery mongoQuery;

    public MongoQuery getMongoQuery() {
        return mongoQuery;
    }

    public MongoRecordStream(IStreamOwner owner) {
        super(owner);
        mongoDataBlade = (MongoDataBlade) owner.getDataSource();
        this.dataProvider = owner.getDataSource().getRecordValuesProviderImplementation();
        Map<String, Object> details = mongoDataBlade.getMetadataProvider().getCollectionMetadata(new ResourceIdentifier(getOwner().getName())).getDetails();
        if (details.containsKey("validationSchema") && details.get("valid").equals(true)) {
            fullObject = true;
        }
        if(details.get("additionalProperties").equals("In schema and false")){
            addProp = false;
        }
    }

    @Override
    protected DTSRecord doGet() throws Exception {
        Document document = cursor.next();
        if (fullObject) {
            String id = document.remove("_id").toString();
            String content = document.toJson();
            DTSRecord record = (DTSRecord) mongoDataBlade.getPayloadFactory().getJson().fromJson(document.toJson(), this.getOwner().getRecordExemplar());
            getOwner().getRecordExemplar().getDeclaredField("_id").set(record, id);
            if(addProp) {
                getOwner().getRecordExemplar().getDeclaredField(MongoMetadataProvider.MONGO_FIELD_NAME).set(record, content);
            }
            return record;
        } else {
            String id = document.getObjectId("_id").toHexString();
            String content = document.toJson();
            Object[] values = {id, content};
            DTSRecord record = (DTSRecord) this.getOwner().getRecordExemplar().getDeclaredConstructor().newInstance();
            record.readValues(values);
            return record;
        }
    }

    @Override
    protected List<DTSRecord> doGetUpTo(int n) throws Exception {
        int count = 0;
        List<DTSRecord> records = new ArrayList<>();
        while (cursor.hasNext() && count < n) {
            Document document = cursor.next();
            if (fullObject) {
                records.add((DTSRecord) mongoDataBlade.getPayloadFactory().getJson().fromJson(document.toJson(), this.getOwner().getRecordExemplar()));
            } else {
                String id = document.getObjectId("_id").toHexString();
                String content = document.toJson();
                Object[] values = {id, content};
                DTSRecord record = (DTSRecord) this.getOwner().getRecordExemplar().getDeclaredConstructor().newInstance();
                record.readValues(values);
                records.add(record);
            }
            count++;
        }
        return records;
    }

    @Override
    protected void doOpen(DTSPredicate query) throws Exception {
        if(query != null) {
            mongoQuery = new MongoQuery(mongoDataBlade.getCollection(getOwner().getName()),query);
            cursor = mongoDataBlade.getMongoDatabase().getCollection(getOwner().getName()).find(mongoQuery.buildFilter(query)).iterator();
        }else {
            cursor = mongoDataBlade.getMongoDatabase().getCollection(getOwner().getName()).find().iterator();
        }
    }

    @Override
    protected void doClose() throws Exception {
        cursor.close();
    }

    @Override
    protected boolean canGetMoreData() {
        return cursor.hasNext();
    }
}
