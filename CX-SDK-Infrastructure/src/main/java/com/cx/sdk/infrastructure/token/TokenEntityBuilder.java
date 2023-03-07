package com.cx.sdk.infrastructure.token;

import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import jakarta.ws.rs.core.MultivaluedMap;

public class TokenEntityBuilder {

    private static final String USERNAME_KEY = "username";
    private static final String PASS_KEY = "password";
    private static final String GRANT_TYPE_KEY = "grant_type";
    public static final String SCOPE_KEY = "scope";
    private static final String CLIENT_ID_KEY = "client_id";
    private static final String CLIENT_ID_VALUE = "ide_client";
    private static final String CLIENT_SECRET_KEY = "client_secret";
    private static final String CLIENT_SECRET_VALUE = "014DF517-39D1-4453-B7B3-9930C563627C";
    public static final String SAST_REST_API_OFFLINE_ACCESS_CXARM_API = "sast_rest_api soap_api";

    public static MultivaluedMap createGetAccessTokenFromCredentialsParamsEntity(String userName, String password) {
        MultivaluedMap formData = new MultivaluedStringMap();
        formData.add(USERNAME_KEY, userName);
        formData.add(PASS_KEY, password);
        formData.add(GRANT_TYPE_KEY, PASS_KEY);
        formData.add(SCOPE_KEY, SAST_REST_API_OFFLINE_ACCESS_CXARM_API);
        formData.add(CLIENT_ID_KEY, CLIENT_ID_VALUE);
        formData.add(CLIENT_SECRET_KEY, CLIENT_SECRET_VALUE);
        return formData;
    }
}