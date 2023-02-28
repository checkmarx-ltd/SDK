package com.cx.sdk.oidcLogin.restClient;

import com.cx.sdk.oidcLogin.constants.Consts;
import com.cx.sdk.oidcLogin.exceptions.CxRestClientException;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TokenHTTPEntityBuilder {

    private static final String ERROR_MESSAGE_PREFIX = "Failed to create body entity, due to: ";

    public static StringEntity createGetAccessTokenFromCodeParamsEntity(String code, String serverURL) throws CxRestClientException {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(Consts.GRANT_TYPE_KEY, Consts.AUTHORIZATION_CODE_GRANT_TYPE));
        urlParameters.add(new BasicNameValuePair(Consts.CLIENT_ID_KEY, Consts.CLIENT_VALUE));
        urlParameters.add(new BasicNameValuePair(Consts.SCOPE_KEY, Consts.SCOPE_VALUE2));
        urlParameters.add(new BasicNameValuePair(Consts.REDIRECT_URI_KEY, serverURL.endsWith("/") ? serverURL : (serverURL + "/")));
        urlParameters.add(new BasicNameValuePair(Consts.CODE_KEY, code));

        try {
            return new UrlEncodedFormEntity(urlParameters, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new CxRestClientException(ERROR_MESSAGE_PREFIX + e.getMessage());
        }
    }

    public static StringEntity createGetAccessTokenFromRefreshTokenParamsEntity(String refreshToken) throws CxRestClientException {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(Consts.GRANT_TYPE_KEY, Consts.REFRESH_TOKEN));
        urlParameters.add(new BasicNameValuePair(Consts.CLIENT_ID_KEY, Consts.CLIENT_VALUE));
        urlParameters.add(new BasicNameValuePair(Consts.REFRESH_TOKEN, refreshToken));

        try {
            return new UrlEncodedFormEntity(urlParameters, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new CxRestClientException(ERROR_MESSAGE_PREFIX + e.getMessage());
        }
    }
}