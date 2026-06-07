package com.alloy.dts.blade.mongo;

import com.alloy.dts.DTSException;
import com.alloy.dts.blade.DataBlade;
import com.alloy.dts.model.ResourceIdentifier;
import com.alloy.dts.model.dd.NativeTypeMetadata;
import com.alloy.dts.type.DTSTypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MongoTypeMetadata extends NativeTypeMetadata {
    public MongoTypeMetadata(ResourceIdentifier identifier, DataBlade dataSource) {
        super(identifier, dataSource);
    }

    //
    public static final List<String> NATIVE_TYPES = List.of(
            //
            // numeric int32=int int64=long
            "int", "long", "double", "decimal128",
            //
            //string
            "string", "symbol",
            //
            //binary
            "binData",
            //
            //boolean
            "boolean",
            //
            //document types
            "object", "array",
            //
            //special, deprecated
            "undefined", "dbPointer",
            //
            //identifiers
            "objectId",
            //
            //date & time
            "date", "timestamp",
            //
            //null
            "null",
            //
            //regex
            "regex",
            //
            //javascript
            "javascript", "javascriptWithScope",
            //
            //special bson limits
            "minKey", "maxKey"
    );

    private static final Map<String, String> NATIVE_TYPE_SYNONYMS = new HashMap<>();

    static {
        NATIVE_TYPE_SYNONYMS.put("long", "int64");
        NATIVE_TYPE_SYNONYMS.put("decimal128", "decimal");
        NATIVE_TYPE_SYNONYMS.put("symbol", "string");
        NATIVE_TYPE_SYNONYMS.put("binData", "binary");
        NATIVE_TYPE_SYNONYMS.put("undefined", "string");
        NATIVE_TYPE_SYNONYMS.put("dbPointer", "string");
        NATIVE_TYPE_SYNONYMS.put("objectId", "string");
        NATIVE_TYPE_SYNONYMS.put("timestamp", "date_time");
        NATIVE_TYPE_SYNONYMS.put("null", "string");
        NATIVE_TYPE_SYNONYMS.put("regex", "string");
        NATIVE_TYPE_SYNONYMS.put("javascript", "string");
        NATIVE_TYPE_SYNONYMS.put("javascriptWithScope", "string");
        NATIVE_TYPE_SYNONYMS.put("minKey", "string");
        NATIVE_TYPE_SYNONYMS.put("maxKey", "string");
    }

    private static final Map<String, String> NATIVE_TYPES_MAP = new HashMap<>();

    static {
        NATIVE_TYPES_MAP.put("int", DTSTypes.INT32_TYPE);
        NATIVE_TYPES_MAP.put("int64", DTSTypes.INT64_TYPE);
        NATIVE_TYPES_MAP.put("double", DTSTypes.DOUBLE_TYPE);
        NATIVE_TYPES_MAP.put("decimal", DTSTypes.DECIMAL_TYPE);
        NATIVE_TYPES_MAP.put("string", DTSTypes.STRING_TYPE);
        NATIVE_TYPES_MAP.put("boolean", DTSTypes.BOOLEAN_TYPE);
        NATIVE_TYPES_MAP.put("date", DTSTypes.DATE_TYPE);
        NATIVE_TYPES_MAP.put("date_time", DTSTypes.DATE_TIME_TYPE);
    }

    private ResourceIdentifier typeIdentifier;
    private MongoTypeMetadata parentType;
    private MongoTypeDecorator decorator;

    public ResourceIdentifier getTypeIdentifier() {
        return typeIdentifier;
    }

    public void setTypeIdentifier(ResourceIdentifier typeIdentifier) {
        this.typeIdentifier = typeIdentifier;
    }

    public MongoTypeMetadata getParentType() {
        return parentType;
    }

    public void setParentType(MongoTypeMetadata parentType) {
        this.parentType = parentType;
        setTypeIdentifier(parentType.getTypeIdentifier());
    }

    public MongoTypeDecorator getDecorator() {
        return decorator;
    }

    public void setDecorator(MongoTypeDecorator decorator) {
        this.decorator = decorator;
    }

    public static boolean isNativeType(String typeName) {
        return NATIVE_TYPES.contains(resolveTypeName(typeName));
    }

    public static String correctTypeName(String typeName) {
        String correctedDataType = typeName.toLowerCase().trim();
        return correctedDataType;
    }

    public static String resolveTypeName(String typeName) {
        String typeKey = correctTypeName(typeName);
        if (NATIVE_TYPE_SYNONYMS.containsKey(typeKey)) {
            typeKey = NATIVE_TYPE_SYNONYMS.get(typeKey);
        }
        return typeKey;
    }

    public static String getDTSTypeNameFor(String nativeName) throws DTSException {
        String typeKey = resolveTypeName(nativeName);
        String dtsTypeName = NATIVE_TYPES_MAP.get(typeKey);
        if (dtsTypeName == null) {
            throw new DTSException("Unsupported native type '" + nativeName + "' in DTS.");
        }
        return dtsTypeName;
    }
}