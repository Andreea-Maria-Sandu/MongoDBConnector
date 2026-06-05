package com.alloy.dts.blade.mongo;

import com.alloy.dts.DTSException;
import com.alloy.dts.model.DTSTypeIdentifier;
import com.alloy.dts.model.ResourceIdentifier;
import com.alloy.dts.model.dd.DTSDetailMapping;
import com.alloy.dts.record.DTSTypeBuilder;

public class MongoDTSTypeBuilder extends DTSTypeBuilder {
    @Override
    public void ensureDTSTypeFor(DTSDetailMapping detailMapping) throws DTSException {
        ResourceIdentifier typeIdentifier = ((MongoFieldMetadata)detailMapping.getNativeResource()).getTypeIdentifier();
        if(MongoTypeMetadata.isNativeType(typeIdentifier.getRawName())) {
            detailMapping.setDtsTypeIdentifier(new DTSTypeIdentifier(MongoTypeMetadata.getDTSTypeNameFor(typeIdentifier.getRawName()),typeIdentifier));
        }else{
            detailMapping.setDtsTypeIdentifier(getMetadataProvider().getTypeMetadata(typeIdentifier).getDtsTypeIdentifier());
            if (getDataBlade().getTypesCluster().getClassForTypeName(detailMapping.getDtsTypeIdentifier().getKey()) == null) {
                throw new DTSException("Could not find type " + detailMapping.getDtsTypeIdentifier().getKey() + ". Mongo client probably did not initialize correctly.");
            }
        }
    }
}
