package com.alloy.dts.blade.mongo;
import com.alloy.dts.model.DTSPredicate;
import com.alloy.dts.project.ConnectorProjectEntry;
import com.alloy.dts.project.Project;
import com.alloy.dts.record.DTSRecord;
import com.alloy.dts.record.DTSRecordStream;
import com.alloy.dts.record.DTSStreamRecordContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.alloy.dts.blade.mongo.MongoDataBladesPoolTest.initProducerContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MongoDataBladeTest {
    private static MongoDataBlade mongoDataBlade;

    @BeforeAll
    public static void before() throws Exception {
        ConnectorProjectEntry connectorProjectEntry = Project.json.fromJson(
                new InputStreamReader(
                        Objects.requireNonNull(
                                MongoDataBladeTest.class.getResourceAsStream("../../../../../testmongo.json"))),
                ConnectorProjectEntry.class);
        connectorProjectEntry.setName("mongoconnector");
        MongoDataBladesPool pool = new MongoDataBladesPool(1);
        initProducerContext("MONGO", pool);
        pool.setupConnectorTypes(connectorProjectEntry);
        pool.createDataBlades();
        mongoDataBlade = (MongoDataBlade) pool.getDataBlade(1);
    }

    @Test
    public void connectionTest() throws Exception
    {
        MongoConnectionDetails  mongoConnectionDetails = new MongoConnectionDetails();
        mongoConnectionDetails.setHost("localhost");
        mongoConnectionDetails.setPort("27017");
        mongoConnectionDetails.setUsername("dbUser");
        mongoConnectionDetails.setPassword("dbUser1234");
        mongoConnectionDetails.setDbName("sample_mflix");
        mongoConnectionDetails.setConnectionString("cluster00.azftj4t.mongodb.net/?appName=Cluster00");
        mongoDataBlade.connect(mongoConnectionDetails);
    }
    //before+testRecordStream
    @Test
    public void testRecordStream() throws Exception {
        DTSRecordStream dtsRecordStream = mongoDataBlade.newRecordStream("movies",null);
        assertNotNull(dtsRecordStream);
        Boolean hasMore = true;
        List<Object> list = new ArrayList<>();
        int counter = 0;
        while(hasMore) {
            DTSStreamRecordContainer<?> dtsStreamRecordContainer = dtsRecordStream.getRecordsInContainer(1000);
            list.addAll(dtsStreamRecordContainer.getRecords());
            hasMore = dtsStreamRecordContainer.hasMoreToGet();
            counter+=dtsStreamRecordContainer.getRecords().size();
        }
        assertEquals(21252,counter);
        dtsRecordStream.close();
    }

    @Test
    public void testRecordStreamEQ() throws Exception {
        DTSRecordStream dtsRecordStream = mongoDataBlade.newRecordStream("movies",DTSPredicate.eq("title","The Blue Bird"));
        assertNotNull(dtsRecordStream);
        DTSStreamRecordContainer<?> dtsStreamRecordContainer = dtsRecordStream.getRecordsInContainer(5);
        assertEquals(1,dtsStreamRecordContainer.getRecords().size());
        assertEquals("{\"title\": \"The Blue Bird\"}",((MongoRecordStream)dtsRecordStream).getMongoQuery().getQueryString());
        dtsRecordStream.close();
    }

    @Test
    public void testRecordStreamEQID() throws Exception {
        DTSRecordStream dtsRecordStream = mongoDataBlade.newRecordStream("movies",DTSPredicate.eq("_id","573a1390f29313caabcd63d6"));
        assertNotNull(dtsRecordStream);
        DTSStreamRecordContainer<?> dtsStreamRecordContainer = dtsRecordStream.getRecordsInContainer(5);
        assertEquals(1,dtsStreamRecordContainer.getRecords().size());
        assertEquals("{\"_id\": {\"$oid\": \"573a1390f29313caabcd63d6\"}}",((MongoRecordStream)dtsRecordStream).getMongoQuery().getQueryString());
        dtsRecordStream.close();
    }

    @Test
    public void testRecordStreamEQ2() throws Exception {
        DTSRecordStream dtsRecordStream = mongoDataBlade.newRecordStream("users",DTSPredicate.eq("name","Jon Snow"));
        assertNotNull(dtsRecordStream);
        DTSStreamRecordContainer<?> dtsStreamRecordContainer = dtsRecordStream.getRecordsInContainer(5);
        assertEquals(1,dtsStreamRecordContainer.getRecords().size());
        assertEquals("{\"name\": \"Jon Snow\"}",((MongoRecordStream)dtsRecordStream).getMongoQuery().getQueryString());
        dtsRecordStream.close();
    }

    @Test
    public void testRecordStreamEQ3() throws Exception {
        DTSRecordStream dtsRecordStream = mongoDataBlade.newRecordStream("movies",DTSPredicate.eq("awards.wins","1"));
        assertNotNull(dtsRecordStream);
        DTSStreamRecordContainer<?> dtsStreamRecordContainer = dtsRecordStream.getRecordsInContainer(5000);
        assertEquals(4786,dtsStreamRecordContainer.getRecords().size());
        assertEquals("{\"awards.wins\": 1.0}",((MongoRecordStream)dtsRecordStream).getMongoQuery().getQueryString());
        dtsRecordStream.close();
    }
    @Test
    public void testRecordStreamGT2() throws Exception {
        DTSRecordStream dtsRecordStream = mongoDataBlade.newRecordStream("movies",DTSPredicate.gt("imdb.rating","9"));
        assertNotNull(dtsRecordStream);
        DTSStreamRecordContainer<?> dtsStreamRecordContainer = dtsRecordStream.getRecordsInContainer(30);
        assertEquals(20,dtsStreamRecordContainer.getRecords().size());
        assertEquals("{\"imdb.rating\": {\"$gt\": 9.0}}",((MongoRecordStream)dtsRecordStream).getMongoQuery().getQueryString());
        dtsRecordStream.close();
    }

    @Test
    public void testRecordStreamGT() throws Exception {
        DTSRecordStream dtsRecordStream = mongoDataBlade.newRecordStream("movies",DTSPredicate.gt("year","2013"));
        assertNotNull(dtsRecordStream);
        DTSStreamRecordContainer<?> dtsStreamRecordContainer = dtsRecordStream.doGetRecordsInContainer(2000);
        assertEquals(1500,dtsStreamRecordContainer.getRecords().size());
        assertEquals("{\"year\": {\"$gt\": 2013.0}}",((MongoRecordStream)dtsRecordStream).getMongoQuery().getQueryString());
        dtsRecordStream.close();
    }

    @Test
    public void testRecordStreamGTE() throws Exception {
        DTSRecordStream dtsRecordStream = mongoDataBlade.newRecordStream("movies",DTSPredicate.gteq("year","2013"));
        assertNotNull(dtsRecordStream);
        DTSStreamRecordContainer<?> dtsStreamRecordContainer = dtsRecordStream.doGetRecordsInContainer(3000);
        assertEquals(2603,dtsStreamRecordContainer.getRecords().size());
        assertEquals("{\"year\": {\"$gte\": 2013.0}}",((MongoRecordStream)dtsRecordStream).getMongoQuery().getQueryString());
        dtsRecordStream.close();
    }

    @Test
    public void testRecordStreamLT() throws Exception {
        DTSRecordStream dtsRecordStream = mongoDataBlade.newRecordStream("movies",DTSPredicate.lt("year","1980"));
        assertNotNull(dtsRecordStream);
        DTSStreamRecordContainer<?> dtsStreamRecordContainer = dtsRecordStream.doGetRecordsInContainer(4000);
        assertEquals(3229,dtsStreamRecordContainer.getRecords().size());
        assertEquals("{\"year\": {\"$lt\": 1980.0}}",((MongoRecordStream)dtsRecordStream).getMongoQuery().getQueryString());
        dtsRecordStream.close();
    }

    @Test
    public void testRecordStreamLTE() throws Exception {
        DTSRecordStream dtsRecordStream = mongoDataBlade.newRecordStream("movies",DTSPredicate.lteq("year","1980"));
        assertNotNull(dtsRecordStream);
        DTSStreamRecordContainer<?> dtsStreamRecordContainer = dtsRecordStream.doGetRecordsInContainer(4000);
        assertEquals(3396,dtsStreamRecordContainer.getRecords().size());
        assertEquals("{\"year\": {\"$lte\": 1980.0}}",((MongoRecordStream)dtsRecordStream).getMongoQuery().getQueryString());
        dtsRecordStream.close();
    }

    @Test
    public void testRecordStreamLikeStarts() throws Exception {
        //records with title starting with Jurassic
        DTSRecordStream dtsRecordStream = mongoDataBlade.newRecordStream("movies",DTSPredicate.like("title","^Jurassic"));
        assertNotNull(dtsRecordStream);
        DTSStreamRecordContainer<?> dtsStreamRecordContainer = dtsRecordStream.doGetRecordsInContainer(2000);
        assertEquals(4,dtsStreamRecordContainer.getRecords().size());
        assertEquals("{\"title\": {\"$regularExpression\": {\"pattern\": \"^Jurassic\", \"options\": \"\"}}}",((MongoRecordStream)dtsRecordStream).getMongoQuery().getQueryString());
        dtsRecordStream.close();
    }

    @Test
    public void testRecordStreamLikeEnds() throws Exception {
        //records with title ending with d
        DTSRecordStream dtsRecordStream = mongoDataBlade.newRecordStream("movies",DTSPredicate.like("title","d$"));
        assertNotNull(dtsRecordStream);
        DTSStreamRecordContainer<?> dtsStreamRecordContainer = dtsRecordStream.doGetRecordsInContainer(2000);
        assertEquals(1236,dtsStreamRecordContainer.getRecords().size());
        assertEquals("{\"title\": {\"$regularExpression\": {\"pattern\": \"d$\", \"options\": \"\"}}}",((MongoRecordStream)dtsRecordStream).getMongoQuery().getQueryString());
        dtsRecordStream.close();
    }

    @Test
    public void testRecordStreamLikeAnywhere() throws Exception {
        //records with title contains Jurassic word anywhere
        DTSRecordStream dtsRecordStream = mongoDataBlade.newRecordStream("movies",DTSPredicate.like("title",".*Jurassic.*"));
        assertNotNull(dtsRecordStream);
        DTSStreamRecordContainer<?> dtsStreamRecordContainer = dtsRecordStream.doGetRecordsInContainer(2000);
        assertEquals(5,dtsStreamRecordContainer.getRecords().size());
        DTSRecord record = (DTSRecord) dtsStreamRecordContainer.getRecords().getFirst();
        assertEquals("Jurassic Park",record.getFieldValue("title"));
        assertEquals("{\"title\": {\"$regularExpression\": {\"pattern\": \".*Jurassic.*\", \"options\": \"\"}}}",((MongoRecordStream)dtsRecordStream).getMongoQuery().getQueryString());
        dtsRecordStream.close();
    }
    @Test
    public void testRecordStreamAnd() throws Exception {
        // title begins with T and ends with p
        DTSRecordStream dtsRecordStream = mongoDataBlade.newRecordStream("movies",DTSPredicate.and(DTSPredicate.like("title","^T"),DTSPredicate.like("title","p$")));
        assertNotNull(dtsRecordStream);
        DTSStreamRecordContainer<?> dtsStreamRecordContainer = dtsRecordStream.doGetRecordsInContainer(2000);
        assertEquals(56,dtsStreamRecordContainer.getRecords().size());
        assertEquals("{\"$and\": [{\"title\": {\"$regularExpression\": {\"pattern\": \"^T\", \"options\": \"\"}}}, {\"title\": {\"$regularExpression\": {\"pattern\": \"p$\", \"options\": \"\"}}}]}",((MongoRecordStream)dtsRecordStream).getMongoQuery().getQueryString());
        dtsRecordStream.close();
    }

    @Test
    public void testRecordStreamOr() throws Exception {
        // title begins with A or T
        DTSRecordStream dtsRecordStream = mongoDataBlade.newRecordStream("movies",DTSPredicate.or(DTSPredicate.like("title","^A"),DTSPredicate.like("title","^T")));
        assertNotNull(dtsRecordStream);
        DTSStreamRecordContainer<?> dtsStreamRecordContainer = dtsRecordStream.doGetRecordsInContainer(10000);
        assertEquals(6381,dtsStreamRecordContainer.getRecords().size());
        assertEquals("{\"$or\": [{\"title\": {\"$regularExpression\": {\"pattern\": \"^A\", \"options\": \"\"}}}, {\"title\": {\"$regularExpression\": {\"pattern\": \"^T\", \"options\": \"\"}}}]}",((MongoRecordStream)dtsRecordStream).getMongoQuery().getQueryString());
        dtsRecordStream.close();
    }

    @Test
    public void testRecordStreamNEQ() throws Exception {
        DTSRecordStream dtsRecordStream = mongoDataBlade.newRecordStream("movies",DTSPredicate.neq("year","2015"));
        assertNotNull(dtsRecordStream);
        DTSStreamRecordContainer<?> dtsStreamRecordContainer = dtsRecordStream.doGetRecordsInContainer(30000);
        assertEquals(20820,dtsStreamRecordContainer.getRecords().size());
        assertEquals("{\"year\": {\"$ne\": 2015.0}}",((MongoRecordStream)dtsRecordStream).getMongoQuery().getQueryString());
        dtsRecordStream.close();
    }

    @Test
    public void testRecordStreamUNLIKE() throws Exception {
        DTSRecordStream dtsRecordStream = mongoDataBlade.newRecordStream("movies",DTSPredicate.unlike("title","^Star"));
        assertNotNull(dtsRecordStream);
        DTSStreamRecordContainer<?> dtsStreamRecordContainer = dtsRecordStream.doGetRecordsInContainer(30000);
        assertEquals(21201,dtsStreamRecordContainer.getRecords().size());
        assertEquals("{\"title\": {\"$not\": {\"$regularExpression\": {\"pattern\": \"^Star\", \"options\": \"\"}}}}",((MongoRecordStream)dtsRecordStream).getMongoQuery().getQueryString());
        dtsRecordStream.close();
    }

    @Test
    public void testRecordStreamNAND() throws Exception {
        //Mongo does not support negation of AND => NOT(AND(A,B)) => NOT(A) OR NOT(B)
        DTSRecordStream dtsRecordStream = mongoDataBlade.newRecordStream("movies",DTSPredicate.nand(DTSPredicate.like("title","Star"),DTSPredicate.like("title","Wars")));
        assertNotNull(dtsRecordStream);
        DTSStreamRecordContainer<?> dtsStreamRecordContainer = dtsRecordStream.doGetRecordsInContainer(30000);
        assertEquals(21243,dtsStreamRecordContainer.getRecords().size());
        assertEquals("{\"$or\": [{\"title\": {\"$not\": {\"$regularExpression\": {\"pattern\": \"Star\", \"options\": \"\"}}}}, {\"title\": {\"$not\": {\"$regularExpression\": {\"pattern\": \"Wars\", \"options\": \"\"}}}}]}",((MongoRecordStream)dtsRecordStream).getMongoQuery().getQueryString());
        dtsRecordStream.close();
    }

    @Test
    public void testRecordStreamNOR() throws Exception {
        DTSRecordStream dtsRecordStream = mongoDataBlade.newRecordStream("movies",DTSPredicate.nor(DTSPredicate.gt("year","2010"),DTSPredicate.gt("runtime","100")));
        assertNotNull(dtsRecordStream);
        DTSStreamRecordContainer<?> dtsStreamRecordContainer = dtsRecordStream.doGetRecordsInContainer(30000);
        assertEquals(8517,dtsStreamRecordContainer.getRecords().size());
        assertEquals("{\"$nor\": [{\"year\": {\"$gt\": 2010.0}}, {\"runtime\": {\"$gt\": 100.0}}]}",((MongoRecordStream)dtsRecordStream).getMongoQuery().getQueryString());
        dtsRecordStream.close();
    }
}
