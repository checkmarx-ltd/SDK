package com.cx.sdk.domain.entities;

public class ProxyParams {

    public String server;
    public int port;
    public String username;
    public String password;
    public String type;

    public ProxyParams() {
    }

    public ProxyParams(String server, int port, String username, String password, String type) {
        this.server = server;
        this.port = port;
        this.username = username;
        this.password = password;
        this.type = type;
    }

    public boolean isHostPortExist() {
        return isNotEmpty(server) && port != 0;
    }

    public boolean isBasicAuth() {
        return isNotEmpty(username) && isNotEmpty(password);
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    private boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

}