package com.cx.sdk.infrastructure.providers;

import com.cx.sdk.application.contracts.providers.LoginProvider;
import com.cx.sdk.application.contracts.providers.SDKConfigurationProvider;
import com.cx.sdk.domain.Session;
import com.cx.sdk.domain.exceptions.SdkException;
import com.cx.sdk.infrastructure.proxy.ConnectionFactory;
import com.cx.sdk.oidcLogin.api.CxOIDCLoginClient;
import com.cx.sdk.oidcLogin.api.CxOIDCLoginClientImpl;
import com.cx.sdk.oidcLogin.exceptions.CxRestClientException;
import com.cx.sdk.oidcLogin.exceptions.CxRestLoginException;
import com.cx.sdk.oidcLogin.exceptions.CxValidateResponseException;
import com.cx.sdk.oidcLogin.restClient.entities.Configurations;
import com.cx.sdk.oidcLogin.restClient.entities.Permissions;
import com.cx.sdk.oidcLogin.webBrowsing.LoginData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by ehuds on 2/25/2017.
 */
public class LoginProviderImpl implements LoginProvider {

    public static final String FAILED_TO_GET_NEW_ACCESS_TOKEN_FROM_REFRESH_TOKEN = "Failed to get new access token from refresh token.";
    public static final String INVALID_OR_EXPIRED_SESSION_PLEASE_LOGIN = "Invalid or expired session. Please login.";
    private final SDKConfigurationProvider sdkConfigurationProvider;
    private final Logger logger = LoggerFactory.getLogger(LoginProviderImpl.class);
    private final ConnectionFactory connectionFactory;
    private final CxOIDCLoginClient cxOIDCLoginClient;
    private LoginData loginData = null;


    public static final String SERVER_CONNECTIVITY_FAILURE = "Failed to validate server connectivity for server: ";
        public static final String CX_SDK_WEB_SERVICE_URL = "/cxwebinterface/sdk/cxsdkwebservice.asmx";

    @Inject
    public LoginProviderImpl(SDKConfigurationProvider sdkConfigurationProvider) {
        this.sdkConfigurationProvider = sdkConfigurationProvider;
        connectionFactory = new ConnectionFactory(sdkConfigurationProvider);
        cxOIDCLoginClient = new CxOIDCLoginClientImpl(sdkConfigurationProvider.getCxServerUrl(),
                sdkConfigurationProvider.getCxOriginName(),sdkConfigurationProvider.getProxyParams());
    }


    @Override
    public Session login() throws SdkException {
        try {
            logger.info("cxOIDCLoginClient start login");
            loginData = cxOIDCLoginClient.login();
        } catch (Exception e) {
            String errorMessage = String.format("Failed to perform login to server: [%s]\nError: %s",
                    sdkConfigurationProvider.getCxServerUrl().toString(), e.getMessage());
            logger.error(errorMessage, e);
            throw new SdkException(errorMessage, e);
        }

        if (loginData.wasCanceled())
            return null;

        Permissions permissions = getPermissions(loginData.getAccessToken());
        Configurations configurations = new Configurations();
        try {
        configurations = getExtendedConfigurations(loginData.getAccessToken(),"portal");
        if(!configurations.isMandatoryCommentOnChangeResultState()) {
        	configurations = getExtendedConfigurations(loginData.getAccessToken(),"None");
        }
        }catch (Exception e) {
            System.err.println("Error fetching configurations: " + e.getMessage());
            //making comments mandatory in case of api failure
            configurations.setMandatoryCommentOnChangeResultState(true);
            configurations.setMandatoryCommentOnChangeResultStateToNE(true);
            configurations.setMandatoryCommentOnChangeResultStateToPNE(true);
        }
        Session session = new Session("",
                loginData.getAccessToken(),
                loginData.getRefreshToken(),
                loginData.getAccessTokenExpirationInMillis(),
                permissions.isSaveSastScan(),
                permissions.isManageResultsExploitability(),
                permissions.isManageResultsComment());
        session.setCxVersion(loginData.getCxVersion());
        session.setMandatoryCommentOnChangeResultState(configurations.isMandatoryCommentOnChangeResultState());
        session.setMandatoryCommentOnChangeResultStateToNE(configurations.isMandatoryCommentOnChangeResultStateToNE());
        session.setMandatoryCommentOnChangeResultStateToPNE(configurations.isMandatoryCommentOnChangeResultStateToPNE());
        return session;
    }

    @Override
    public void logout() {
        try {
            cxOIDCLoginClient.logout();
        } catch (CxRestClientException e) {
            e.printStackTrace();
            String errorMessage = String.format("Failed to logging out of server: %s",
                    sdkConfigurationProvider.getCxServerUrl().toString());
            logger.error(errorMessage, e);
            throw new SdkException(errorMessage, e);
        }
    }

    private Permissions getPermissions(String accessToken) {
        try {
            return cxOIDCLoginClient.getPermissions(accessToken);
        } catch (CxValidateResponseException e) {
            String errorMessage = String.format("Failed to perform login to server: %s",
                    sdkConfigurationProvider.getCxServerUrl().toString() + ". Failed to get permissions.");
            logger.error(errorMessage, e);
            throw new SdkException(errorMessage, e);
        }

    }
    
    private Configurations getExtendedConfigurations(String accessToken,String portalOrNone) {
        try {
            return cxOIDCLoginClient.getExtendedConfigurations(accessToken,portalOrNone);
        } catch (CxValidateResponseException e) {
            String errorMessage = String.format("Failed to perform login to server: %s",
                    sdkConfigurationProvider.getCxServerUrl().toString() + ". Failed to get extended configurations for "+portalOrNone+". "+e.getMessage());
            logger.error(errorMessage, e);
            throw new SdkException("You do not have the necessary permissions to perform this action.", e);
        }

    }

    @Override
    public boolean isTokenExpired(Long expirationTime) {
        return cxOIDCLoginClient.isTokenExpired(expirationTime);
    }

    public Session getAccessTokenFromRefreshToken(String refreshToken) throws SdkException {
        LoginData loginData;
        try {
            loginData = cxOIDCLoginClient.getAccessTokenFromRefreshToken(refreshToken);
        } catch (CxRestClientException e) {
            e.printStackTrace();
            String errorMessage = String.format(FAILED_TO_GET_NEW_ACCESS_TOKEN_FROM_REFRESH_TOKEN);
            logger.error(errorMessage, e);
            throw new SdkException(errorMessage, e);
        } catch (CxRestLoginException e) {
            e.printStackTrace();
            String errorMessage = String.format(FAILED_TO_GET_NEW_ACCESS_TOKEN_FROM_REFRESH_TOKEN);
            logger.error(errorMessage, e);
            throw new SdkException(errorMessage, e);
        } catch (CxValidateResponseException e) {
            String errorMessage = String.format(INVALID_OR_EXPIRED_SESSION_PLEASE_LOGIN);
            logger.error(errorMessage, e);
            throw new SdkException(errorMessage, e);
        }
        Permissions permissions = getPermissions(loginData.getAccessToken());
        return new Session("",
                loginData.getAccessToken(),
                loginData.getRefreshToken(),
                loginData.getAccessTokenExpirationInMillis(),
                permissions.isSaveSastScan(),
                permissions.isManageResultsExploitability(),
                permissions.isManageResultsComment());
    }

    @Override
    public boolean isCxWebServiceAvailable() {
        int responseCode;
        try {
            URL urlAddress = new URL(sdkConfigurationProvider.getCxServerUrl(), CX_SDK_WEB_SERVICE_URL);
            logger.info("CxService available URL: " + urlAddress.toString());
            URLConnection conn = connectionFactory.getConnection(urlAddress) ;
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setReadTimeout(60000);
            if (urlAddress.getProtocol().equalsIgnoreCase("https")) {
                logger.info("CxService available using proxy: " + ((HttpsURLConnection) conn).usingProxy());
                responseCode = ((HttpsURLConnection) conn).getResponseCode();
            } else {
                logger.info("CxService available using proxy: " + ((HttpURLConnection) conn).usingProxy());
                responseCode = ((HttpURLConnection) conn).getResponseCode();
            }
            logger.info("CxService available response code: " + responseCode);
        } catch (Exception e) {
            logger.error("Cx server interface is not available", e);
            return false;
        }

        return (responseCode != 404);
    }
}
