package com.cx.sdk.oidcLogin;


import com.cx.sdk.oidcLogin.exceptions.CxRestLoginException;
import com.cx.sdk.oidcLogin.exceptions.CxValidateResponseException;
import com.cx.sdk.oidcLogin.restClient.ICxServer;
import com.cx.sdk.oidcLogin.webBrowsing.AuthenticationData;
import com.cx.sdk.oidcLogin.webBrowsing.IOIDCWebBrowser;
import com.cx.sdk.oidcLogin.webBrowsing.LoginData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CxOIDCConnector {
    private ICxServer cxServer;
    private String clientName;
    private IOIDCWebBrowser webBrowser;
    private final Logger logger = LoggerFactory.getLogger(CxOIDCConnector.class);

    public CxOIDCConnector(ICxServer cxServer, IOIDCWebBrowser webBrowser, String clientName) {
        this.cxServer = cxServer;
        this.webBrowser = webBrowser;
        this.clientName = clientName;
    }

    public LoginData connect() throws Exception {
        String version;
        try {
            version= (String) cxServer.getCxVersion(clientName);
            logger.debug("Print CxVersion: \n" + version);
        } catch (IOException|CxValidateResponseException e) {
            throw new CxRestLoginException(e.getMessage());
        }

        if ("Pre 9.0".equals(version) ) {
            throw new CxRestLoginException("sast version is older than 9.x");
        }
        logger.debug("Start initialize authenticationData ");
        AuthenticationData authenticationData = webBrowser.browseAuthenticationData(cxServer.getServerURL(), clientName);
        logger.debug("Finish initialize authenticationData ");
        if (authenticationData.wasCanceled) {
            logger.debug("Login was canceled");
            return new LoginData(true);
        }
        LoginData loginData = cxServer.login(authenticationData.code);
        //fetch SAST version from response json of get version api and set in loginData
        JsonNode node = new ObjectMapper().readTree(version);
		String cxVersion = node.path("version").asText();
		String[] sastVersionSplit = cxVersion.split("\\.");
        loginData.setCxVersion(sastVersionSplit[0]+"."+sastVersionSplit[1]);
        return loginData;
    }
}