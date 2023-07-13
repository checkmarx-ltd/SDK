package com.cx.sdk.oidcLogin.restClient;


import com.cx.sdk.oidcLogin.exceptions.CxRestClientException;
import com.cx.sdk.oidcLogin.exceptions.CxRestLoginException;
import com.cx.sdk.oidcLogin.exceptions.CxValidateResponseException;
import com.cx.sdk.oidcLogin.restClient.entities.Permissions;
import com.cx.sdk.oidcLogin.webBrowsing.LoginData;

import java.io.IOException;

public interface ICxServer {

    String getServerURL();

    LoginData login(String code) throws CxRestLoginException, CxValidateResponseException, CxRestClientException;

    LoginData getAccessTokenFromRefreshToken(String refreshToken) throws CxRestLoginException, CxValidateResponseException, CxRestClientException;

    Permissions getPermissionsFromUserInfo(String accessToken) throws CxValidateResponseException;

    String getCxVersion() throws IOException, CxValidateResponseException;

    Object getCxVersion(String clientName) throws CxValidateResponseException, IOException;
}
