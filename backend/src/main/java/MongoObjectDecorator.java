package com.alloy.dts.blade.mongo;
import java.util.ArrayList;
import java.util.List;

public class MongoObjectDecorator extends MongoTypeDecorator{
   private final List<MongoAttributeMetadata> attributes = new ArrayList<>();

   public  MongoObjectDecorator(MongoTypeMetadata owner)
   {
      super(owner);
   }

   protected  void addAttribute(MongoAttributeMetadata attributeMetadata)
   {
       attributes.add(attributeMetadata);
       attributeMetadata.setOwner(this);
   }
   public List<MongoAttributeMetadata> getAttributes(){
       return this.attributes;
   }
}
