package com.alloy.dts.blade.mongo;

import com.alloy.dts.DTSException;
import com.alloy.dts.log.DTSLogger;
import com.alloy.dts.model.ResourceIdentifier;
import com.alloy.dts.model.dd.*;
import com.alloy.dts.project.AttributeDescriptor;
import com.alloy.dts.project.ConnectorProjectEntry;
import com.alloy.dts.project.TypeResource;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import java.util.*;

public class MongoMetadataProvider implements INativeMetadataProvider {
    private final MongoDataBlade owner;
    public final Map<String, NativeCollectionMetadata> collectionsMetadata = new HashMap<>();
    public final Map<String, MongoTypeMetadata> mongoTypesMetadata = new HashMap<>();
    public final Map<String, TypeResource> typeResources = new HashMap<>();
    public static final String MONGO_FIELD_NAME = "_content";

    public MongoDataBlade getOwner() {
        return owner;
    }

    public MongoMetadataProvider(MongoDataBlade owner) {
        this.owner = owner;
    }

    @Override
    public List<ResourceIdentifier> getCollectionNames(String filter) {
        List<ResourceIdentifier> resourceIdentifiers = new ArrayList<>();

        for (String name : owner.getMongoDatabase().listCollectionNames()) {
            if (filter == null || name.contains(filter)) {
                ResourceIdentifier resourceIdentifier = new ResourceIdentifier(name);
                resourceIdentifiers.add(resourceIdentifier);
            }
        }

        return resourceIdentifiers;
    }

    @Override
    public List<NativeCollectionMetadata> getMinimalAllCollectionsMetadata() {
        List<NativeCollectionMetadata> metadataList = new ArrayList<>();
        for (ResourceIdentifier collectionId : getCollectionNames(null)) {
            metadataList.add(getCollectionMetadata(collectionId)); //
        }
        return metadataList;
    }

    private MongoTypeMetadata resolveObjectType(String pathToObject, Document fieldDoc) throws DTSException {
        ResourceIdentifier resourceIdentifier = new ResourceIdentifier(pathToObject);
        MongoTypeMetadata mongoTypeMetadata = new MongoTypeMetadata(resourceIdentifier, getOwner());

        if (mongoTypesMetadata.containsKey(pathToObject)) {
            return mongoTypesMetadata.get(pathToObject);
        }
        TypeResource typeResource = new TypeResource();

        typeResource.setNativeIdentifier(mongoTypeMetadata.getTypeIdentifier());
        typeResource.setTypeKeyName(resourceIdentifier.getName());
        typeResource.setRecordClassName(resourceIdentifier.getRawName());

        Document properties = fieldDoc.get("properties", Document.class);
        if (properties == null) {
            throw new DTSException("Object at path '" + pathToObject + "' has no 'properties' defined.");
        }

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            AttributeDescriptor attributeDescriptor = new AttributeDescriptor();
            attributeDescriptor.setName(entry.getKey());

            String fieldName = entry.getKey();
            Document fieldValue = (Document) entry.getValue();

            String currentPath;
            if (pathToObject == null || pathToObject.trim().isEmpty()) {
                currentPath = fieldName;
            } else {
                currentPath = pathToObject + "/" + fieldName;
            }

            String subDocumentBsonType = MongoBsonTypeResolver.resolveBsonType(fieldValue);

            if (currentPath.contains("tomatoes")) {
                System.out.println(currentPath);
            }

            if ("object".equals(subDocumentBsonType)) {
                MongoTypeMetadata typeMetadata = resolveObjectType(currentPath, fieldValue);
                attributeDescriptor.setType(typeMetadata.getDtsTypeIdentifier().getKey());
                attributeDescriptor.setNativeTypeIdentifier(typeMetadata.getIdentifier());
            } else if (subDocumentBsonType.equals("array")) {
                attributeDescriptor.makeArray(true);
                attributeDescriptor.setNativeTypeIdentifier(resolveArrayType(currentPath, fieldValue));
                attributeDescriptor.setType(MongoTypeMetadata.getDTSTypeNameFor(attributeDescriptor.getNativeTypeIdentifier().getRawName()));
            } else {
                attributeDescriptor.setType(MongoTypeMetadata.getDTSTypeNameFor(subDocumentBsonType));
                attributeDescriptor.setNativeTypeIdentifier(new ResourceIdentifier(subDocumentBsonType));
            }
            typeResource.addAttributeDescriptor(attributeDescriptor);
        }

        if (!typeResources.containsKey(pathToObject)) {
            getOwner().getClassFactoryForObjectTypes().createClassFor(typeResource);

            typeResources.put(pathToObject, typeResource);
        }
        mongoTypeMetadata.setDtsTypeIdentifier(mongoTypeMetadata.getIdentifier().getRawName());
        mongoTypesMetadata.put(pathToObject, mongoTypeMetadata);

        return mongoTypeMetadata;
    }

    private ResourceIdentifier resolveArrayType(String pathToObject, Document fieldDoc) throws DTSException {
        pathToObject = pathToObject + "[]";
        Document items = fieldDoc.get("items", Document.class);
        if (items == null) {
            throw new DTSException("Unsupported array field with no items definition: " + pathToObject);
        }
        String elementBsonType = items.get("bsonType", String.class);
        if (elementBsonType == null) {
            throw new DTSException("Unsupported array field with no items bsonType definition: " + fieldDoc);
        }
        if ("object".equals(elementBsonType)) {
            NativeTypeMetadata objectTypeMetadata = resolveObjectType(pathToObject, items);
            return objectTypeMetadata.getIdentifier();
        } else {
            return new ResourceIdentifier(elementBsonType);
        }
    }

    @Override
    public NativeCollectionMetadata getCollectionMetadata(ResourceIdentifier resourceIdentifier) {
        //create MongoTypeMetadata
        //handle additionalProperties
        ConnectorProjectEntry connectorProjectEntry = new ConnectorProjectEntry();
        connectorProjectEntry.setType("MONGO");

        NativeCollectionMetadata nativeCollectionMetadata = new NativeCollectionMetadata(resourceIdentifier, getOwner());

        String collectionName = nativeCollectionMetadata.getName();

        Document collectionInfo = getOwner().getMongoDatabase().listCollections()
                .filter(Filters.eq("name", collectionName)).first();

        try {
            if (collectionInfo == null) {
                throw new DTSException("Collection not found.");
            }
        } catch (Exception e) {
            DTSLogger.error("Collection not found, because: ", e);
        }

        Document options = collectionInfo.get("options", Document.class);

        Document validator = null;

        if (options != null) {
            validator = options.get("validator", Document.class);
        }

        Map<String, Object> details = new HashMap<>();

        if (validator != null) {
            try {
                Document jsonSchema = validator.get("$jsonSchema", Document.class);
                String jsonSchemaString = jsonSchema.toJson();
                details.put("validationSchema", jsonSchemaString);
                details.put("valid", true);
                List<String> required = jsonSchema.getList("required", String.class);
                Document properties = jsonSchema.get("properties", Document.class);

                if (!properties.keySet().contains("_id")) {
                    MongoFieldMetadata mongoFieldMetadata = new MongoFieldMetadata(new ResourceIdentifier("_id"), resourceIdentifier, getOwner());
                    mongoFieldMetadata.setTypeIdentifier(new ResourceIdentifier("string"));
                    MongoTypeMetadata mongoTypeMetadata = new MongoTypeMetadata(new ResourceIdentifier("string"), getOwner());
                    mongoFieldMetadata.setTypeMetadata(mongoTypeMetadata);
                    mongoTypesMetadata.put(resourceIdentifier.getRawName(), mongoTypeMetadata);
                    nativeCollectionMetadata.addFieldMetadata(mongoFieldMetadata);
                }

                for (String fieldName : properties.keySet()) {
                    MongoFieldMetadata mongoFieldMetadata = new MongoFieldMetadata(new ResourceIdentifier(fieldName), resourceIdentifier, getOwner());
                    Document fieldOptions = properties.get(fieldName, Document.class);
                    String bsonType = MongoBsonTypeResolver.resolveBsonType(fieldOptions);

                    if (fieldName.equals("_id")) {
                        bsonType = "string";
                    }

                    if (required.contains(fieldName)) {
                        mongoFieldMetadata.setRequired(true);
                    } else {
                        mongoFieldMetadata.setRequired(false);
                    }

                    if ("array".equals(bsonType)) {
                        mongoFieldMetadata.setArray(true);
                        mongoFieldMetadata.setTypeIdentifier(resolveArrayType(fieldName, fieldOptions));
                    } else if ("object".equals(bsonType)) {
                        NativeTypeMetadata objectTypeMetadata = resolveObjectType(resourceIdentifier.getRawName() + "/" + fieldName, fieldOptions);
                        mongoFieldMetadata.setTypeIdentifier(objectTypeMetadata.getIdentifier());
                        mongoFieldMetadata.setTypeMetadata(objectTypeMetadata);
                    } else {
                        mongoFieldMetadata.setTypeIdentifier(new ResourceIdentifier(bsonType));
                    }
                    nativeCollectionMetadata.addFieldMetadata(mongoFieldMetadata);
                }
                // daca are addtitional propertiez , creez un field "_content" si adaugam in el tot documentul
                Object additionalProperties = jsonSchema.get("additionalProperties");

                if (!jsonSchema.containsKey("additionalProperties")) {
                    MongoFieldMetadata mongoFieldMetadata = new MongoFieldMetadata(new ResourceIdentifier(MONGO_FIELD_NAME), resourceIdentifier, getOwner());
                    mongoFieldMetadata.setTypeIdentifier(new ResourceIdentifier("string"));
                    MongoTypeMetadata mongoTypeMetadata = new MongoTypeMetadata(new ResourceIdentifier("string"), getOwner());
                    mongoFieldMetadata.setTypeMetadata(mongoTypeMetadata);
                    mongoTypesMetadata.put(resourceIdentifier.getRawName(), mongoTypeMetadata);
                    nativeCollectionMetadata.addFieldMetadata(mongoFieldMetadata);
                    details.put("additionalProperties", "Not in schema");
                } else if (jsonSchema.containsKey("additionalProperties") && additionalProperties instanceof Boolean) {
                    if (additionalProperties.equals(true)) {
                        MongoFieldMetadata mongoFieldMetadata = new MongoFieldMetadata(new ResourceIdentifier(MONGO_FIELD_NAME), resourceIdentifier, getOwner());
                        mongoFieldMetadata.setTypeIdentifier(new ResourceIdentifier("string"));
                        MongoTypeMetadata mongoTypeMetadata = new MongoTypeMetadata(new ResourceIdentifier("string"), getOwner());
                        mongoFieldMetadata.setTypeMetadata(mongoTypeMetadata);
                        mongoTypesMetadata.put(resourceIdentifier.getRawName(), mongoTypeMetadata);
                        nativeCollectionMetadata.addFieldMetadata(mongoFieldMetadata);
                        details.put("additionalProperties", "In schema and true");
                    }else{
                        details.put("additionalProperties", "In schema and false");
                    }
                } else if (jsonSchema.containsKey("additionalProperties") && additionalProperties instanceof Document) {
                    MongoFieldMetadata mongoFieldMetadata = new MongoFieldMetadata(new ResourceIdentifier(MONGO_FIELD_NAME), resourceIdentifier, getOwner());
                    mongoFieldMetadata.setTypeIdentifier(new ResourceIdentifier("string"));
                    MongoTypeMetadata mongoTypeMetadata = new MongoTypeMetadata(new ResourceIdentifier("string"), getOwner());
                    mongoFieldMetadata.setTypeMetadata(mongoTypeMetadata);
                    mongoTypesMetadata.put(resourceIdentifier.getRawName(), mongoTypeMetadata);
                    nativeCollectionMetadata.addFieldMetadata(mongoFieldMetadata);
                    details.put("additionalProperties", "In schema and document");
                }


            } catch (Exception e) {
                DTSLogger.error("Unsupported validator schema for collection '" + resourceIdentifier.getName() + "', because: ", e);
                details.put("valid", false);
            }
        } else {

            MongoFieldMetadata mongoFieldMetadata = new MongoFieldMetadata(new ResourceIdentifier("_id"), resourceIdentifier, getOwner());
            mongoFieldMetadata.setTypeIdentifier(new ResourceIdentifier("string"));
            MongoTypeMetadata mongoTypeMetadata = new MongoTypeMetadata(new ResourceIdentifier("string"), getOwner());
            mongoFieldMetadata.setTypeMetadata(mongoTypeMetadata);
            mongoTypesMetadata.put(resourceIdentifier.getRawName(), mongoTypeMetadata);
            nativeCollectionMetadata.addFieldMetadata(mongoFieldMetadata);

            MongoFieldMetadata mongoFieldMetadata2 = new MongoFieldMetadata(new ResourceIdentifier(MONGO_FIELD_NAME), resourceIdentifier, getOwner());
            mongoFieldMetadata2.setTypeIdentifier(new ResourceIdentifier("string"));
            mongoFieldMetadata2.setTypeMetadata(mongoTypeMetadata);
            nativeCollectionMetadata.addFieldMetadata(mongoFieldMetadata2);
        }
        nativeCollectionMetadata.setDetails(details);
        return nativeCollectionMetadata;
    }

    @Override
    public List<ResourceIdentifier> getFunctionNames(String s) {
        return List.of();
    }

    @Override
    public NativeRemoteMetadata getRemoteMetadata(ResourceIdentifier resourceIdentifier) {
        return null;
    }

    @Override
    public NativeTypeMetadata getTypeMetadata(ResourceIdentifier resourceIdentifier) {
        System.out.println(resourceIdentifier.getRawName());
        return mongoTypesMetadata.get(resourceIdentifier.getRawName());
    }

    @Override
    public TypeResource getTypeDetails(ResourceIdentifier resourceIdentifier) {
        return typeResources.get(resourceIdentifier.getRawName());
    }

    @Override
    public void forgetCollectionMetadata(ResourceIdentifier resourceIdentifier) {

    }

    @Override
    public void forgetRemoteMetadata(ResourceIdentifier resourceIdentifier) {

    }

    @Override
    public List<ResourceIdentifier> getTopicNames(String filter) {
        return INativeMetadataProvider.super.getTopicNames(filter);
    }

    @Override
    public NativeTopicMetadata getTopicMetadata(ResourceIdentifier topicIdentifier) {
        return INativeMetadataProvider.super.getTopicMetadata(topicIdentifier);
    }

    @Override
    public void forgetTopicMetadata(ResourceIdentifier topicIdentifier) {
        INativeMetadataProvider.super.forgetTopicMetadata(topicIdentifier);
    }
}
