package com.cx.sdk.oidcLogin.api;

import com.cx.sdk.oidcLogin.exceptions.CxRestClientException;
import com.cx.sdk.oidcLogin.exceptions.CxRestLoginException;
import com.cx.sdk.oidcLogin.exceptions.CxValidateResponseException;
import com.cx.sdk.oidcLogin.restClient.entities.Configurations;
import com.cx.sdk.oidcLogin.restClient.entities.Permissions;
import com.cx.sdk.oidcLogin.webBrowsing.LoginData;
import com.cx.sdk.oidcLogin.webBrowsing.OIDCWebBrowser;

public interface CxOIDCLoginClient {

    LoginData login() throws Exception;

    void logout() throws CxRestClientException;

    boolean isTokenExpired(Long expirationTime);

    LoginData getAccessTokenFromRefreshToken(String accessToken) throws CxRestClientException, CxRestLoginException, CxValidateResponseException;

    Permissions getPermissions(String accessToken) throws CxValidateResponseException;
    
    Configurations getExtendedConfigurations(String accessToken, String portalOrNone) throws CxValidateResponseException;

    void dispose();
}
