package com.alloy.dts.blade.mongo;

import com.alloy.dts.model.DTSCollection;
import com.alloy.dts.model.DTSPredicate;
import com.alloy.dts.record.ADTSRecordValuesProvider;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;

public class MongoRecordValuesProvider extends ADTSRecordValuesProvider {
    @Override
    public boolean hasMoreData() {
        return false;
    }

    @Override
    public String getString(String fieldName) throws Exception {
        return "";
    }

    @Override
    public String[] getStringVector(String fieldName) throws Exception {
        return new String[0];
    }

    @Override
    public Byte getByte(String fieldName) throws Exception {
        return 0;
    }

    @Override
    public Short getShort(String fieldName) throws Exception {
        return 0;
    }

    @Override
    public Integer getInteger(String fieldName) throws Exception {
        return 0;
    }

    @Override
    public Long getLong(String fieldName) throws Exception {
        return 0L;
    }

    @Override
    public Float getFloat(String fieldName) throws Exception {
        return 0f;
    }

    @Override
    public Double getDouble(String fieldName) throws Exception {
        return 0.0;
    }

    @Override
    public Boolean getBoolean(String fieldName) throws Exception {
        return null;
    }

    @Override
    public BigInteger getBigInteger(String fieldName) throws Exception {
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(String fieldName) throws Exception {
        return null;
    }

    @Override
    public LocalDate getDate(String fieldName) throws Exception {
        return null;
    }

    @Override
    public Instant getDateTime(String fieldName) throws Exception {
        return null;
    }

    @Override
    public Instant getTime(String fieldName) throws Exception {
        return null;
    }

    @Override
    public Object getObject(String fieldName) throws Exception {
        return null;
    }

    @Override
    public Object[] getObjectVector(String fieldName) throws Exception {
        return new Object[0];
    }

    @Override
    public byte[] getByteVector(String fieldName) throws Exception {
        return new byte[0];
    }

    @Override
    public short[] getShortVector(String fieldName) throws Exception {
        return new short[0];
    }

    @Override
    public int[] getIntVector(String fieldName) throws Exception {
        return new int[0];
    }

    @Override
    public long[] getLongVector(String fieldName) throws Exception {
        return new long[0];
    }

    @Override
    public float[] getFloatVector(String fieldName) throws Exception {
        return new float[0];
    }

    @Override
    public double[] getDoubleVector(String fieldName) throws Exception {
        return new double[0];
    }

    @Override
    public BigInteger[] getBigIntegerVector(String fieldName) throws Exception {
        return new BigInteger[0];
    }

    @Override
    public BigDecimal[] getBigDecimalVector(String fieldName) throws Exception {
        return new BigDecimal[0];
    }

    @Override
    public Boolean[] geBooleanVector(String fieldName) throws Exception {
        return new Boolean[0];
    }

    @Override
    public LocalDate[] getDateVector(String fieldName) throws Exception {
        return new LocalDate[0];
    }

    @Override
    public Instant[] getDateTimeVector(String fieldName) throws Exception {
        return new Instant[0];
    }

    @Override
    public Instant[] getTimeVector(String fieldName) throws Exception {
        return new Instant[0];
    }

    @Override
    public void close() {

    }

    @Override
    public void open(DTSCollection collection, DTSPredicate filter) throws Exception {

    }

    @Override
    public void prepareToReadNextRecord() throws Exception {

    }
}
