package com.cx.sdk.oidcLogin.api;

import com.cx.sdk.oidcLogin.CxOIDCConnector;
import com.cx.sdk.oidcLogin.exceptions.CxRestClientException;
import com.cx.sdk.oidcLogin.exceptions.CxRestLoginException;
import com.cx.sdk.oidcLogin.exceptions.CxValidateResponseException;
import com.cx.sdk.oidcLogin.restClient.CxServerImpl;
import com.cx.sdk.oidcLogin.restClient.ICxServer;
import com.cx.sdk.oidcLogin.restClient.entities.Permissions;
import com.cx.sdk.oidcLogin.webBrowsing.IOIDCWebBrowser;
import com.cx.sdk.oidcLogin.webBrowsing.LoginData;
import com.cx.sdk.oidcLogin.webBrowsing.OIDCWebBrowser;

import java.net.URL;

public class CxOIDCLoginClientImpl implements CxOIDCLoginClient {

    private String clientName;
    private ICxServer server;
    private IOIDCWebBrowser webBrowser;
    private LoginData loginData;

    public CxOIDCLoginClientImpl(URL serverUrl, String clientName) {
        this.clientName = clientName;
        this.server = new CxServerImpl(serverUrl.toString());
    }


    public LoginData login() throws Exception {
        webBrowser = new OIDCWebBrowser();
        CxOIDCConnector connector = new CxOIDCConnector(server, webBrowser, clientName);
        loginData = connector.connect();

        return loginData;
    }

    public void logout() {
        webBrowser.logout(loginData.getIdToken());
    }

    @Override
    public boolean isTokenExpired(Long expirationTime) {
        boolean isTokenExpired;
        if (expirationTime == null) {
            isTokenExpired = true;
        } else {
            isTokenExpired = expirationTime.compareTo(System.currentTimeMillis()) < 0;
        }
        return isTokenExpired;
    }

    public LoginData getAccessTokenFromRefreshToken(String refreshToken) throws CxRestClientException, CxRestLoginException, CxValidateResponseException {
        return server.getAccessTokenFromRefreshToken(refreshToken);
    }

    public Permissions getPermissions(String accessToken) throws CxValidateResponseException {
        return server.getPermissionsFromUserInfo(accessToken);
    }

    @Override
    public void dispose() {
        webBrowser.disposeBrowser();
    }
}