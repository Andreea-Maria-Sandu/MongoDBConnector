package com.alloy.dts.blade.mongo;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MongoBsonTypeResolver {
    private static final List<String> PRIORITY = List.of(
            "object",
            "array",
            "string",
            "decimal128",
            "double",
            "long",
            "int",
            "boolean",
            "date",
            "objectId",
            "null"
    );
    public static String resolveBsonType(Document fieldOptions) {
        Object bsonType = fieldOptions.get("bsonType", Object.class);

        if (bsonType == null) {
            return "unknown";
        }

        if (bsonType instanceof String type) {
            if(type.equals("objectId")){
                type = "string";
            }
            return type;
        }

        if (bsonType instanceof List<?> types) {
            return resolveFromList(types);
        }

        return "unknown";
    }
    private static String resolveFromList(List<?> types) {
        Set<String> extractedTypes = new HashSet<>();

        for (Object type : types) {
            if (type instanceof String s) {
                extractedTypes.add(s);
            }
        }

        if (extractedTypes.isEmpty()) {
            return "unknown";
        }

        for (String candidate : PRIORITY) {
            if (extractedTypes.contains(candidate)) {
                return candidate;
            }
        }

        return extractedTypes.iterator().next();
    }
}
