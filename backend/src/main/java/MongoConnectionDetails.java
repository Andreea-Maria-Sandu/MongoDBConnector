package com.alloy.dts.blade.mongo;

import com.alloy.dts.blade.IConnectionDetails;

import java.util.Map;

public class MongoConnectionDetails implements IConnectionDetails {

    private String host;
    private String port;
    private String username;
    private String password;
    private String dbName;
    private String connectionString;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }


    @Override
    public void loadFrom(Map<String, String> parameters) {
        setUsername(parameters.get("username"));
        setPassword(parameters.get("password"));
        setHost(parameters.get("host"));
        setPort(parameters.get("port"));
        setDbName(parameters.get("dbName"));
        setConnectionString(parameters.get("connectionString"));
    }
}
