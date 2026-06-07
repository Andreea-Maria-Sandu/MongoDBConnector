package com.alloy.dts.blade.mongo;

import com.alloy.dts.camel.context.ProducerContext;
import com.alloy.dts.component.DTSComponent;
import com.alloy.dts.model.ResourceIdentifier;
import com.alloy.dts.model.dd.NativeCollectionMetadata;
import com.alloy.dts.model.dd.NativeFieldMetadata;
import com.alloy.dts.model.dd.NativeTypeMetadata;
import com.alloy.dts.project.ConnectorProjectEntry;
import com.alloy.dts.project.TypeResource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MongoMetadataProviderTest {
    private static MongoDataBlade mongoDataBlade = new MongoDataBlade();

    @BeforeAll
    public static void before() throws Exception {
        MongoDataBladesPool pool = new MongoDataBladesPool(1);
        ConnectorProjectEntry connEntry = new ConnectorProjectEntry();
        connEntry.setType("MONGO");
        connEntry.setName("mongo-metadata-test");
        pool.setConnectorEntry(connEntry);
        DTSComponent component = new DTSComponent();
        component.setCategory("MONGO");
        ProducerContext ctx = new ProducerContext(component);
        ctx.ensureDefaultRedisEndpoint();
        ctx.setDataBladesPool(pool);
        mongoDataBlade = new MongoDataBlade();
        mongoDataBlade.setMetadataProvider(new MongoMetadataProvider(mongoDataBlade));
        mongoDataBlade.setPool(pool);
        Map<String,String> details = new HashMap<>();
        details.put("username", "dbUser");
        details.put("password", "dbUser1234");
        details.put("dbName", "sample_mflix");
        details.put("connectionString","mongodb+srv://cluster00.azftj4t.mongodb.net/?appName=Cluster00");
        MongoConnectionDetails connectionDetails = new MongoConnectionDetails();
        connectionDetails.loadFrom(details);
        mongoDataBlade.connect(connectionDetails);
    }

    @Test
    public void testGetCollectionNames() throws Exception {
        List<ResourceIdentifier> collections = mongoDataBlade.getMetadataProvider().getCollectionNames(null);
        assertNotNull(collections);
        assertEquals(6, collections.size());
        assertEquals("comments", collections.get(0).getName());
        assertEquals("movies", collections.get(1).getName());
        assertEquals("theaters", collections.get(2).getName());
        assertEquals("sessions", collections.get(3).getName());
        assertEquals("users", collections.get(4).getName());
        assertEquals("embedded_movies", collections.get(5).getName());
    }

    @Test
    public void testGetCollectionNamesforMovies() throws Exception {
        List<ResourceIdentifier> collections = mongoDataBlade.getMetadataProvider().getCollectionNames("movies");
        assertNotNull(collections);
        assertEquals(2, collections.size());
        assertEquals("movies", collections.get(0).getName());
        assertEquals("embedded_movies", collections.get(1).getName());
    }

    @Test
    public void testMinimalAllCollectionsMetadata() throws Exception{
        List<NativeCollectionMetadata> metadataList = mongoDataBlade.getMetadataProvider().getMinimalAllCollectionsMetadata();
        assertNotNull(metadataList);
        assertEquals(6,metadataList.size());
    }

    @Test
    public void testCollectionMetadata() throws  Exception {
        NativeCollectionMetadata nativeCollectionMetadata = mongoDataBlade.getMetadataProvider().getCollectionMetadata(new ResourceIdentifier("comments"));
        assertNotNull(nativeCollectionMetadata);
        Map<String, NativeFieldMetadata> fieldsCollection1 = nativeCollectionMetadata.getFields();
        assertNotNull(fieldsCollection1);
        assertEquals(6, fieldsCollection1.size());

        assertTrue(fieldsCollection1.containsKey("_id"));
        NativeFieldMetadata fieldcol1 = fieldsCollection1.get("_id");
        assertNotNull(fieldcol1);
        assertEquals("_id", fieldcol1.getName());
        assertEquals("string",fieldcol1.getTypeIdentifier().getName());

        assertTrue(fieldsCollection1.containsKey("name"));
        NativeFieldMetadata fieldcol2 = fieldsCollection1.get("name");
        assertNotNull(fieldcol2);
        assertEquals("name", fieldcol2.getName());
        assertEquals("string",fieldcol2.getTypeIdentifier().getName());

        assertTrue(fieldsCollection1.containsKey("email"));
        NativeFieldMetadata fieldcol3 = fieldsCollection1.get("email");
        assertNotNull(fieldcol3);
        assertEquals("email", fieldcol3.getName());
        assertEquals("string",fieldcol3.getTypeIdentifier().getName());

        assertTrue(fieldsCollection1.containsKey("movie_id"));
        NativeFieldMetadata fieldcol4 = fieldsCollection1.get("movie_id");
        assertNotNull(fieldcol4);
        assertEquals("movie_id", fieldcol4.getName());
        assertEquals("string",fieldcol4.getTypeIdentifier().getName());

        assertTrue(fieldsCollection1.containsKey("text"));
        NativeFieldMetadata fieldcol5 = fieldsCollection1.get("text");
        assertNotNull(fieldcol5);
        assertEquals("text", fieldcol5.getName());
        assertEquals("string",fieldcol5.getTypeIdentifier().getName());

        assertTrue(fieldsCollection1.containsKey("date"));
        NativeFieldMetadata fieldcol6 = fieldsCollection1.get("date");
        assertNotNull(fieldcol6);
        assertEquals("date", fieldcol6.getName());
        assertEquals("date",fieldcol6.getTypeIdentifier().getName());


        NativeCollectionMetadata nativeCollectionMetadata1 = mongoDataBlade.getMetadataProvider().getCollectionMetadata(new ResourceIdentifier("embedded_movies"));
        assertNotNull(nativeCollectionMetadata1);
        Map<String, NativeFieldMetadata> fieldsCollection2 = nativeCollectionMetadata1.getFields();
        assertNotNull(fieldsCollection2);
        assertEquals(2, fieldsCollection2.size());
        assertTrue(fieldsCollection2.containsKey("_id"));
        assertTrue(fieldsCollection2.containsKey(MongoMetadataProvider.MONGO_FIELD_NAME));

        NativeCollectionMetadata nativeCollectionMetadata2 = mongoDataBlade.getMetadataProvider().getCollectionMetadata(new ResourceIdentifier("sessions"));
        assertNotNull(nativeCollectionMetadata2);
        Map<String, NativeFieldMetadata> fieldsCollection3 = nativeCollectionMetadata2.getFields();
        assertNotNull(fieldsCollection3);
        assertEquals(2, fieldsCollection3.size());
        assertTrue(fieldsCollection3.containsKey("_id"));
        assertTrue(fieldsCollection3.containsKey(MongoMetadataProvider.MONGO_FIELD_NAME));

        NativeCollectionMetadata nativeCollectionMetadata3 = mongoDataBlade.getMetadataProvider().getCollectionMetadata(new ResourceIdentifier("theaters"));
        assertNotNull(nativeCollectionMetadata3);
        Map<String, NativeFieldMetadata> fieldsCollection4 = nativeCollectionMetadata3.getFields();
        assertNotNull(fieldsCollection4);
        assertEquals(2, fieldsCollection4.size());
        assertTrue(fieldsCollection4.containsKey("_id"));
        assertTrue(fieldsCollection4.containsKey(MongoMetadataProvider.MONGO_FIELD_NAME));

        NativeCollectionMetadata collections = mongoDataBlade.getMetadataProvider().getCollectionMetadata(new ResourceIdentifier("users"));
        assertNotNull(collections);
        Map<String, NativeFieldMetadata> fields = collections.getFields();
        assertEquals(5, fields.size());

        assertTrue(fields.containsKey("_id"));
        NativeFieldMetadata field = fields.get("_id");
        assertNotNull(field);
        assertEquals("_id", field.getName());
        assertEquals("string",field.getTypeIdentifier().getName());

        assertTrue(fields.containsKey("name"));
        NativeFieldMetadata field2 = fields.get("name");
        assertNotNull(field2);
        assertEquals("name", field2.getName());
        assertEquals("string",field2.getTypeIdentifier().getName());

        assertTrue(fields.containsKey("email"));
        NativeFieldMetadata field3 = fields.get("email");
        assertNotNull(field3);
        assertEquals("email", field3.getName());
        assertEquals("string",field3.getTypeIdentifier().getName());

        assertTrue(fields.containsKey("password"));
        NativeFieldMetadata field4 = fields.get("password");
        assertNotNull(field4);
        assertEquals("password", field4.getName());
        assertEquals("string",field4.getTypeIdentifier().getName());

        assertTrue(fields.containsKey(MongoMetadataProvider.MONGO_FIELD_NAME));
        NativeFieldMetadata fieldUsers5 = fields.get(MongoMetadataProvider.MONGO_FIELD_NAME);
        assertNotNull(fieldUsers5);
        assertEquals(MongoMetadataProvider.MONGO_FIELD_NAME, fieldUsers5.getName());
        assertEquals("string",fieldUsers5.getTypeIdentifier().getName());

        NativeCollectionMetadata collections2 = mongoDataBlade.getMetadataProvider().getCollectionMetadata(new ResourceIdentifier("movies"));
        assertNotNull(collections2);
        Map<String, NativeFieldMetadata> fields2 = collections2.getFields();
        assertEquals(11, fields2.size());

        assertTrue(fields2.containsKey("_id"));
        NativeFieldMetadata fieldMoviesId = fields2.get("_id");
        assertNotNull(fieldMoviesId);
        assertEquals("_id", fieldMoviesId.getName());
        assertEquals("string",fieldMoviesId.getTypeIdentifier().getName());

        assertTrue(fields2.containsKey("title"));
        NativeFieldMetadata field5 = fields2.get("title");
        assertNotNull(field5);
        assertEquals("title", field5.getName());
        assertEquals("string",field5.getTypeIdentifier().getName());
        assertFalse(field5.isArray());

        assertTrue(fields2.containsKey("year"));
        NativeFieldMetadata field6 = fields2.get("year");
        assertNotNull(field6);
        assertEquals("year", field6.getName());
        assertEquals("int",field6.getTypeIdentifier().getName());
        assertFalse(field6.isArray());

        assertTrue(fields2.containsKey("plot"));
        NativeFieldMetadata field7 = fields2.get("plot");
        assertNotNull(field7);
        assertEquals("plot", field7.getName());
        assertEquals("string",field7.getTypeIdentifier().getName());
        assertFalse(field7.isArray());

        assertTrue(fields2.containsKey("genres"));
        NativeFieldMetadata field8 = fields2.get("genres");
        assertNotNull(field8);
        assertEquals("genres", field8.getName());
        assertEquals("string",field8.getTypeIdentifier().getName());
        assertTrue(field8.isArray());

        assertTrue(fields2.containsKey("cast"));
        NativeFieldMetadata field9 = fields2.get("cast");
        assertNotNull(field9);
        assertEquals("cast", field9.getName());
        assertEquals("string",field9.getTypeIdentifier().getName());
        assertTrue(field9.isArray());

        assertTrue(fields2.containsKey("directors"));
        NativeFieldMetadata field10 = fields2.get("directors");
        assertNotNull(field10);
        assertEquals("directors", field10.getName());
        assertEquals("string",field10.getTypeIdentifier().getName());
        assertTrue(field10.isArray());

        assertTrue(fields2.containsKey("countries"));
        NativeFieldMetadata field11 = fields2.get("countries");
        assertNotNull(field11);
        assertEquals("countries", field11.getName());
        assertEquals("string",field11.getTypeIdentifier().getName());
        assertTrue(field11.isArray());

        assertTrue(fields2.containsKey("imdb"));
        NativeFieldMetadata field12 = fields2.get("imdb");
        assertNotNull(field12);
        assertEquals("imdb", field12.getName());
        assertEquals("movies/imdb",field12.getTypeIdentifier().getName());

        assertTrue(fields2.containsKey("tomatoes"));
        NativeFieldMetadata field13 = fields2.get("tomatoes");
        assertNotNull(field13);
        assertEquals("tomatoes", field13.getName());
        assertEquals("movies/tomatoes",field13.getTypeIdentifier().getName());

        assertTrue(fields2.containsKey(MongoMetadataProvider.MONGO_FIELD_NAME));
        NativeFieldMetadata field14 = fields2.get(MongoMetadataProvider.MONGO_FIELD_NAME);
        assertNotNull(field14);
        assertEquals(MongoMetadataProvider.MONGO_FIELD_NAME, field14.getName());
        assertEquals("string",field14.getTypeIdentifier().getName());

        NativeTypeMetadata nativeTypeMetadata1 = mongoDataBlade.getMetadataProvider().getTypeMetadata(new ResourceIdentifier("movies/imdb"));
        assertNotNull(nativeTypeMetadata1);
        assertEquals("movies/imdb", nativeTypeMetadata1.getName());
        assertEquals("movies/imdb", nativeTypeMetadata1.getIdentifier().getKey());

        TypeResource typeResource1 = mongoDataBlade.getMetadataProvider().getTypeDetails(new ResourceIdentifier("movies/imdb"));
        assertNotNull(typeResource1);
        assertEquals("jmovies_imdb", typeResource1.getName());
        assertEquals("movies/imdb",typeResource1.getType());
        assertEquals(3,typeResource1.getAttributeDescriptors().size());
        assertEquals("rating",typeResource1.getAttributeDescriptors().get(0).getName());
        assertEquals("double",typeResource1.getAttributeDescriptors().get(0).getNativeTypeIdentifier().getName());
        assertEquals("DOUBLE",typeResource1.getAttributeDescriptors().get(0).getType());
        assertEquals("votes", typeResource1.getAttributeDescriptors().get(1).getName());
        assertEquals("int",typeResource1.getAttributeDescriptors().get(1).getNativeTypeIdentifier().getName());
        assertEquals("INT32",typeResource1.getAttributeDescriptors().get(1).getType());
        assertEquals("id",typeResource1.getAttributeDescriptors().get(2).getName());
        assertEquals("int",typeResource1.getAttributeDescriptors().get(2).getNativeTypeIdentifier().getName());
        assertEquals("INT32",typeResource1.getAttributeDescriptors().get(2).getType());

        NativeTypeMetadata nativeTypeMetadata2 = mongoDataBlade.getMetadataProvider().getTypeMetadata(new ResourceIdentifier("movies/tomatoes"));
        assertNotNull(nativeTypeMetadata2);
        assertEquals("movies/tomatoes", nativeTypeMetadata2.getName());
        assertEquals("movies/tomatoes", nativeTypeMetadata2.getIdentifier().getKey());

        TypeResource typeResource2 = mongoDataBlade.getMetadataProvider().getTypeDetails(new ResourceIdentifier("movies/tomatoes"));
        assertNotNull(typeResource2);
        assertEquals("jmovies_tomatoes", typeResource2.getName());
        assertEquals("movies/tomatoes", typeResource2.getType());
        assertEquals(2,typeResource2.getAttributeDescriptors().size());
        assertEquals("viewer",typeResource2.getAttributeDescriptors().get(0).getName());
        assertEquals("movies/tomatoes/viewer",typeResource2.getAttributeDescriptors().get(0).getNativeTypeIdentifier().getName());
        assertEquals("movies/tomatoes/viewer",typeResource2.getAttributeDescriptors().get(0).getType());
        assertEquals("critic",typeResource2.getAttributeDescriptors().get(1).getName());
        assertEquals("movies/tomatoes/critic",typeResource2.getAttributeDescriptors().get(1).getNativeTypeIdentifier().getName());
        assertEquals("movies/tomatoes/critic",typeResource2.getAttributeDescriptors().get(1).getType());

        NativeTypeMetadata nativeTypeMetadata3 = mongoDataBlade.getMetadataProvider().getTypeMetadata(new ResourceIdentifier("movies/tomatoes/viewer"));
        assertNotNull(nativeTypeMetadata3);
        assertEquals("movies/tomatoes/viewer", nativeTypeMetadata3.getName());
        assertEquals("movies/tomatoes/viewer", nativeTypeMetadata3.getIdentifier().getKey());

        TypeResource typeResource3 = mongoDataBlade.getMetadataProvider().getTypeDetails(new ResourceIdentifier("movies/tomatoes/viewer"));
        assertNotNull(typeResource3);
        assertEquals("jmovies_tomatoes_viewer", typeResource3.getName());
        assertEquals("movies/tomatoes/viewer", typeResource3.getType());
        assertEquals(2,typeResource3.getAttributeDescriptors().size());
        assertEquals("rating",typeResource3.getAttributeDescriptors().get(0).getName());
        assertEquals("double",typeResource3.getAttributeDescriptors().get(0).getNativeTypeIdentifier().getName());
        assertEquals("DOUBLE",typeResource3.getAttributeDescriptors().get(0).getType());
        assertEquals("numReviews", typeResource3.getAttributeDescriptors().get(1).getName());
        assertEquals("int",typeResource3.getAttributeDescriptors().get(1).getNativeTypeIdentifier().getName());
        assertEquals("INT32",typeResource3.getAttributeDescriptors().get(1).getType());

        NativeTypeMetadata nativeTypeMetadata4 = mongoDataBlade.getMetadataProvider().getTypeMetadata(new ResourceIdentifier("movies/tomatoes/critic"));
        assertNotNull(nativeTypeMetadata4);
        assertEquals("movies/tomatoes/critic", nativeTypeMetadata4.getName());
        assertEquals("movies/tomatoes/critic", nativeTypeMetadata4.getIdentifier().getKey());

        TypeResource typeResource4 = mongoDataBlade.getMetadataProvider().getTypeDetails(new ResourceIdentifier("movies/tomatoes/critic"));
        assertNotNull(typeResource4);
        assertEquals("jmovies_tomatoes_critic", typeResource4.getName());
        assertEquals("movies/tomatoes/critic", typeResource4.getType());
        assertEquals(2,typeResource4.getAttributeDescriptors().size());
        assertEquals("rating",typeResource4.getAttributeDescriptors().get(0).getName());
        assertEquals("double",typeResource4.getAttributeDescriptors().get(0).getNativeTypeIdentifier().getName());
        assertEquals("DOUBLE",typeResource4.getAttributeDescriptors().get(0).getType());
        assertEquals("numReviews", typeResource4.getAttributeDescriptors().get(1).getName());
        assertEquals("int",typeResource4.getAttributeDescriptors().get(1).getNativeTypeIdentifier().getName());
        assertEquals("INT32",typeResource4.getAttributeDescriptors().get(1).getType());

        assertNotNull(mongoDataBlade.getClassFactoryForObjectTypes().getTypesCluster().getClassForTypeName("movies/imdb"));
        assertNotNull(mongoDataBlade.getClassFactoryForObjectTypes().getTypesCluster().getClassForTypeName("movies/tomatoes"));
        assertNotNull(mongoDataBlade.getClassFactoryForObjectTypes().getTypesCluster().getClassForTypeName("movies/tomatoes/viewer"));
        assertNotNull(mongoDataBlade.getClassFactoryForObjectTypes().getTypesCluster().getClassForTypeName("movies/tomatoes/critic"));
    }
}
