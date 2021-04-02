package com.cx.sdk.infrastructure;

import com.cx.sdk.application.contracts.exceptions.NotAuthorizedException;
import com.cx.sdk.application.contracts.providers.SDKConfigurationProvider;
import com.cx.sdk.domain.enums.LoginType;
import com.cx.sdk.domain.exceptions.SdkException;
import com.cx.sdk.infrastructure.authentication.kerberos.WindowsAuthenticator;
import com.cx.sdk.infrastructure.proxy.ConnectionFactory;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ehuds on 2/28/2017.
 */
public class CxRestClient {

    private static final Logger logger = LoggerFactory.getLogger(CxRestClient.class);

    private final SDKConfigurationProvider sdkConfigurationProvider;
    private final RestResourcesURIBuilder restResourcesURIBuilder = new RestResourcesURIBuilder();
    private ConnectionFactory connectionFactory = null;
    private final Client client;
    private static final String AUTH_TYPE_NEGOTIATE = "Negotiate";
    private URL url = null;

    public CxRestClient(SDKConfigurationProvider sdkConfigurationProvider) {
        this.sdkConfigurationProvider = sdkConfigurationProvider;
        ClientConfig clientConfig = new ClientConfig(getConnectionProvider());
        clientConfig.property(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true);
        client = ClientBuilder.newClient(clientConfig);
    }


    private HttpUrlConnectorProvider getConnectionProvider() {
        setUrlByLoginType();
        HttpUrlConnectorProvider httpUrlConnectorProvider = new HttpUrlConnectorProvider();
        httpUrlConnectorProvider.connectionFactory(new ConnectionFactory(sdkConfigurationProvider));

        return httpUrlConnectorProvider;
    }

    private void setUrlByLoginType() {
        LoginType loginType = sdkConfigurationProvider.getLoginType();
        if (loginType.equals(LoginType.SSO)) {
            url = restResourcesURIBuilder.buildSsoLoginURL(sdkConfigurationProvider.getCxServerUrl());
        } else if (loginType.equals(LoginType.CREDENTIALS)) {
            url = restResourcesURIBuilder.buildLoginURL(sdkConfigurationProvider.getCxServerUrl());
        }
    }

    public Map<String, String> ssoLogin() throws Exception {
        logger.info("Requesting SSO Login to: " + url);
        Response response = baseRequest(url).post(Entity.text(" "));

        validateResponse(response);

        return extractCxCookies(response);
    }

    public Map<String, String> login(String userName, String password) throws Exception {

        String request = "{\"userName\":\"%s\", \"password\":\"%s\"}";

        userName = validateUserName(userName);
        logger.info("Request URL: " + url);
        logger.info("Request payload: " + String.format(request, userName, "*********"));

        Response response = baseRequest(url)
                .accept("application/json")
                .post(Entity.entity(String.format(request, userName, password), MediaType.APPLICATION_JSON));

        logger.info("Login response status: " + response.getStatus());
        logger.info("Login response message: " + response.readEntity(String.class));
        validateResponse(response);

        return extractCxCookies(response);
    }

    private String validateUserName(String username) {
        if (username.contains("/") || username.contains("\\")) {
            return username.replaceAll("[/\\\\\\\\]", "\\\\\\\\");
        }
        return username;
    }

    private Invocation.Builder baseRequest(URL resourceUrl) {
        WebTarget target = client.target(resourceUrl.toString());

        Invocation.Builder requestBuilder = target.request().header("CxOrigin", sdkConfigurationProvider.getCxOriginName());

        if (sdkConfigurationProvider.useKerberosAuthentication()) {
            requestBuilder.header("Authorization", AUTH_TYPE_NEGOTIATE + " " + WindowsAuthenticator.getKrbToken(resourceUrl.getAuthority()));
        }

        return requestBuilder;
    }

    private void validateResponse(Response response) throws Exception {
        if (response.getStatus() == 401) {
            throw new NotAuthorizedException();
        } else if (response.getStatus() >= 400) {
            throw new SdkException("Failed : HTTP error code : " + response.getStatus());
        }
    }

    private Map<String, String> extractCxCookies(Response response) {
        HashMap<String, String> coockies = new HashMap<>();
        for (NewCookie cookie : response.getCookies().values()) {
            if ("cxCookie".equals(cookie.getName()) || "CXCSRFToken".equals(cookie.getName()))
                coockies.put(cookie.getName(), cookie.getValue());
        }

        return coockies;
    }
}
