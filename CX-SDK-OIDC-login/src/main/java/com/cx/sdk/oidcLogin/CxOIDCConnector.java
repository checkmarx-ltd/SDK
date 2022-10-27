package com.cx.sdk.oidcLogin;


import com.cx.sdk.oidcLogin.exceptions.CxRestLoginException;
import com.cx.sdk.oidcLogin.restClient.CxServerImpl;
import com.cx.sdk.oidcLogin.restClient.ICxServer;
import com.cx.sdk.oidcLogin.webBrowsing.AuthenticationData;
import com.cx.sdk.oidcLogin.webBrowsing.IOIDCWebBrowser;
import com.cx.sdk.oidcLogin.webBrowsing.LoginData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CxOIDCConnector {

    private static final Logger log = LoggerFactory.getLogger(CxOIDCConnector.class);

    private ICxServer cxServer;
    private String clientName;
    private IOIDCWebBrowser webBrowser;

    public CxOIDCConnector(ICxServer cxServer, IOIDCWebBrowser webBrowser, String clientName) {
        this.cxServer = cxServer;
        this.webBrowser = webBrowser;
        this.clientName = clientName;
    }

    public LoginData connect() throws Exception {
        if (cxServer.getCxVersion().equals("Pre 9.0")) {
            log.error("SAST server version is either older than 9.0 or the server can't be reached");
        }

        AuthenticationData authenticationData = webBrowser.browseAuthenticationData(cxServer.getServerURL(), clientName);

        if (authenticationData.wasCanceled) {
            return new LoginData(true);
        }

        LoginData loginData = cxServer.login(authenticationData.code);
        return loginData;
    }
}