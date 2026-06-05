package com.alloy.dts.blade.mongo;
import com.alloy.dts.model.DTSCollection;
import com.alloy.dts.model.DTSPredicate;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

public class MongoQuery {
    private final DTSCollection collection;
    private final DTSPredicate predicate;

    public MongoQuery(DTSCollection collection, DTSPredicate predicate) {
        this.collection = collection;
        this.predicate = predicate;
    }

    public Bson buildFilter(DTSPredicate predicate2) {
        if (predicate2.getOperatorName().equals("eq") && predicate2.getNegated().equals(false)) {
            if(predicate2.getAttributeValue().matches("\\d+")){
                Double value = Double.parseDouble(predicate2.getAttributeValue());
                return Filters.eq(predicate2.getAttributeName(), value);
            }else if(predicate2.getAttributeName().equals("_id")){
                return Filters.eq(predicate2.getAttributeName(), new org.bson.types.ObjectId(predicate2.getAttributeValue()));
            }
            return Filters.eq(predicate2.getAttributeName(), predicate2.getAttributeValue());
        } else if (predicate2.getOperatorName().equals("gt") && predicate2.getNegated().equals(false)) {
            if(predicate2.getAttributeValue().matches("\\d+")){
                Double value = Double.parseDouble(predicate2.getAttributeValue());
                return Filters.gt(predicate2.getAttributeName(), value);
            }
            return Filters.gt(predicate2.getAttributeName(), predicate2.getAttributeValue());
        } else if (predicate2.getOperatorName().equals("lt") && predicate2.getNegated().equals(false)) {
            if(predicate2.getAttributeValue().matches("\\d+")){
                Double value = Double.parseDouble(predicate2.getAttributeValue());
                return Filters.lt(predicate2.getAttributeName(), value);
            }
            return Filters.lt(predicate2.getAttributeName(), predicate2.getAttributeValue());
        } else if (predicate2.getOperatorName().equals("gteq") && predicate2.getNegated().equals(false)) {
            if(predicate2.getAttributeValue().matches("\\d+")){
                Double value = Double.parseDouble(predicate2.getAttributeValue());
                return Filters.gte(predicate2.getAttributeName(), value);
            }
            return Filters.gte(predicate2.getAttributeName(), predicate2.getAttributeValue());
        } else if (predicate2.getOperatorName().equals("lteq") && predicate2.getNegated().equals(false)) {
            if(predicate2.getAttributeValue().matches("\\d+")){
                Double value = Double.parseDouble(predicate2.getAttributeValue());
                return Filters.lte(predicate2.getAttributeName(), value);
            }
            return Filters.lte(predicate2.getAttributeName(), predicate2.getAttributeValue());
        } else if (predicate2.getOperatorName().equals("like") && predicate2.getNegated().equals(false)) {
            return Filters.regex(predicate2.getAttributeName(), predicate2.getAttributeValue());
        } else if (predicate2.getOperatorName().equals("and") && predicate2.getNegated().equals(false)) {
            return Filters.and(buildFilter(predicate2.getLeftPredicate()), buildFilter(predicate2.getRightPredicate()));
        } else if (predicate2.getOperatorName().equals("or") && predicate2.getNegated().equals(false)) {
            return Filters.or(buildFilter(predicate2.getLeftPredicate()), buildFilter(predicate2.getRightPredicate()));
        }else if(predicate2.getOperatorName().equals("and") && predicate2.getNegated().equals(true)){
            //Mongo does not support negation of AND => NOT(AND(A,B)) => NOT(A) OR NOT(B)
            return Filters.or(Filters.not(buildFilter(predicate2.getLeftPredicate())), Filters.not(buildFilter(predicate2.getRightPredicate())));
        }else if(predicate2.getOperatorName().equals("or") && predicate2.getNegated().equals(true)){
            return Filters.nor(buildFilter(predicate2.getLeftPredicate()), buildFilter(predicate2.getRightPredicate()));
        }else if(predicate2.getOperatorName().equals("eq") && predicate2.getNegated().equals(true)){
            if(predicate2.getAttributeValue().matches("\\d+")){
                Double value = Double.parseDouble(predicate2.getAttributeValue());
                return Filters.ne(predicate2.getAttributeName(), value);
            }
            return Filters.ne(predicate2.getAttributeName(), predicate2.getAttributeValue());
        }else if(predicate2.getOperatorName().equals("like") && predicate2.getNegated().equals(true)){
            return Filters.not(Filters.regex(predicate2.getAttributeName(), predicate2.getAttributeValue()));
        }
        return null;
    }

    public String getQueryString() {
        return buildFilter(predicate).toBsonDocument(Document.class,
                MongoClientSettings.getDefaultCodecRegistry()).toJson();
    }
}
