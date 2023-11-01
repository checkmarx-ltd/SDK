package com.cx.sdk.oidcLogin.restClient;

import com.cx.sdk.domain.entities.ProxyParams;
import com.cx.sdk.oidcLogin.constants.Consts;
import com.cx.sdk.oidcLogin.dto.AccessTokenDTO;
import com.cx.sdk.oidcLogin.dto.UserInfoDTO;
import com.cx.sdk.oidcLogin.exceptions.CxRestClientException;
import com.cx.sdk.oidcLogin.exceptions.CxRestLoginException;
import com.cx.sdk.oidcLogin.exceptions.CxValidateResponseException;
import com.cx.sdk.oidcLogin.restClient.entities.Permissions;
import com.cx.sdk.oidcLogin.webBrowsing.LoginData;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.cx.sdk.oidcLogin.constants.Consts.*;

public class CxServerImpl implements ICxServer {

    private String serverURL;
    private String tokenEndpointURL;
    private String userInfoURL;
    private final String sessionEndURL;
    private final String logoutURL;
    private final String versionURL;
    private final ProxyParams proxyParams;
    private HttpClient client;
    private List<Header> headers = new ArrayList<>();
    private String tokenEndpoint = Consts.SAST_PREFIX + "/identity/connect/token";
    private final String clientName;
    private String restEndpoint = "/CxRestAPI/";
    private String restUri;
    private String userInfoEndpoint = Consts.USER_INFO_ENDPOINT;
    public static final String GET_VERSION_ERROR = "Get Version API not found, server not found or version is older than 9.0";
    private static final String AUTHENTICATION_FAILED = " User authentication failed";
    private static final String INFO_FAILED = "User info failed";

    private AccessTokenDTO accessTokenDTO;
    private static final String ORIGIN_HEADER = "cxOrigin";
    private static final String ORIGIN_URL_HEADER = "cxOriginUrl";
    private static final String TEAM_PATH = "cxTeamPath";

    public static String getCxOrigin() {
        return cxOrigin;
    }

    public static void setCxOrigin(String cxOrigin) {
        CxServerImpl.cxOrigin = cxOrigin;
    }

    public static String getCxOriginUrl() {
        return cxOriginUrl;
    }

    public static void setCxOriginUrl(String cxOriginUrl) {
        CxServerImpl.cxOriginUrl = cxOriginUrl;
    }

    public static String getCxTeam() {
        return cxTeam;
    }

    public static void setCxTeam(String cxTeam) {
        CxServerImpl.cxTeam = cxTeam;
    }

    private static String cxOrigin = "cxOrigin";
    private static String cxOriginUrl = "cxOriginUrl";
    private static String cxTeam = "cxTeamPath";
    private String rootUri;
    private static final Logger logger = LoggerFactory.getLogger(CxServerImpl.class);


    public CxServerImpl(String serverURL) {
        this.serverURL = serverURL;
        this.tokenEndpointURL = serverURL + tokenEndpoint;
        this.userInfoURL = serverURL + userInfoEndpoint;
        this.sessionEndURL = serverURL + END_SESSION_ENDPOINT;
        this.logoutURL = serverURL + LOGOUT_ENDPOINT;
        this.versionURL = serverURL + VERSION_END_POINT;
        this.clientName = "";
        this.proxyParams = null;
        setClient();
    }

    public CxServerImpl(String serverURL, String clientName, ProxyParams proxyParams) {
        this.serverURL = serverURL;
        this.tokenEndpointURL = serverURL + tokenEndpoint;
        this.userInfoURL = serverURL + userInfoEndpoint;
        this.sessionEndURL = serverURL + END_SESSION_ENDPOINT;
        this.logoutURL = serverURL + LOGOUT_ENDPOINT;
        this.versionURL = serverURL + VERSION_END_POINT;
        this.clientName = clientName;
        this.proxyParams = proxyParams;
        this.restUri = serverURL + restEndpoint;
        setClient();
    }

    public AccessTokenDTO getAccessTokenDTO() {
        return accessTokenDTO;
    }

    public void setAccessTokenDTO(AccessTokenDTO accessTokenDTO) {
        this.accessTokenDTO = accessTokenDTO;
    }

    public String getRootUri() {
        return rootUri;
    }

    public void setRootUri(String rootUri) {
        this.rootUri = rootUri;
    }


    private void setClient() {
        HttpClientBuilder builder = HttpClientBuilder.create().setDefaultHeaders(headers);
        setSSLTls("TLSv1.2");
        logger.debug("Validate that TLSv is 1.2!!!");
        disableCertificateValidation(builder);
        //Add using proxy
        if (!isCustomProxySet(proxyParams))
            builder.useSystemProperties();
        else
            setCustomProxy(builder, proxyParams);
        client = builder.build();
    }

    public String getServerURL() {
        return serverURL;
    }

    public String getCxVersion() throws IOException, CxValidateResponseException {
        return getCxVersion("");
    }

    public String getCxVersion(String clientName) throws CxValidateResponseException, IOException {
        HttpResponse response;
        HttpUriRequest request;
        String version;

        request = RequestBuilder
                .get()
                .setUri(versionURL)
                .setHeader("cxOrigin", clientName)
                .setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString())
                .build();

        logger.debug(" Print Get Version request line\n" + request.getRequestLine());

        response = client.execute(request);
        logger.debug("Print Get version response \n" + response.getStatusLine());
        logger.debug("Print Get Version response \n" + Arrays.toString(response.getAllHeaders()));
        validateResponse(response, 200, GET_VERSION_ERROR);
        version = new BasicResponseHandler().handleResponse(response);


        return version;
    }

    public LoginData login(String code) throws CxRestLoginException, CxValidateResponseException, CxRestClientException {
        HttpUriRequest postRequest;
        HttpResponse loginResponse = null;
        try {
            headers.clear();
            setClient();
            postRequest = RequestBuilder.post()
                    .setUri(tokenEndpointURL)
                    .setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString())
                    .setEntity(TokenHTTPEntityBuilder.createGetAccessTokenFromCodeParamsEntity(code, serverURL))
                    .build();
            logger.debug("Print Request\n" + postRequest.getRequestLine());
            loginResponse = client.execute(postRequest);
            logger.debug("Print response \n" + loginResponse.getStatusLine());
            validateResponse(loginResponse, 200, AUTHENTICATION_FAILED);
            AccessTokenDTO jsonResponse = parseJsonFromResponse(loginResponse, AccessTokenDTO.class);
            Long accessTokenExpirationInMilli = getAccessTokenExpirationInMilli(jsonResponse.getExpiresIn());
            return new LoginData(jsonResponse.getAccessToken(), jsonResponse.getRefreshToken(), accessTokenExpirationInMilli, jsonResponse.getIdToken());
        } catch (IOException e) {
            logger.trace("Failed to login", e);
            throw new CxRestLoginException("Failed to login: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(loginResponse);
        }
    }


    @Override
    public LoginData getAccessTokenFromRefreshToken(String refreshToken) throws CxRestClientException, CxRestLoginException, CxValidateResponseException {
        HttpUriRequest postRequest;
        HttpResponse loginResponse = null;
        try {
            headers.clear();
            setClient();
            postRequest = RequestBuilder.post()
                    .setUri(tokenEndpointURL)
                    .setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString())
                    .setEntity(TokenHTTPEntityBuilder.createGetAccessTokenFromRefreshTokenParamsEntity(refreshToken))
                    .build();
            logger.debug("Print Request\n" + postRequest.getRequestLine());
            loginResponse = client.execute(postRequest);
            logger.debug("Print response \n" + loginResponse.getStatusLine());
            validateResponse(loginResponse, 200, AUTHENTICATION_FAILED);
            AccessTokenDTO jsonResponse = parseJsonFromResponse(loginResponse, AccessTokenDTO.class);
            Long accessTokenExpirationInMilli = getAccessTokenExpirationInMilli(jsonResponse.getExpiresIn());
            return new LoginData(jsonResponse.getAccessToken(), jsonResponse.getRefreshToken(), accessTokenExpirationInMilli, jsonResponse.getIdToken());
        } catch (IOException e) {
            logger.trace("Failed to get new access token from refresh token: ", e);
            throw new CxRestLoginException("Failed to get new access token from refresh token: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(loginResponse);
        }
    }

    @Override
    public Permissions getPermissionsFromUserInfo(String accessToken) throws CxValidateResponseException {
        HttpUriRequest postRequest;
        HttpResponse userInfoResponse = null;
        Permissions permissions = null;
        try {
            HttpClientBuilder builder = HttpClientBuilder.create();

            //Add using proxy
            if (!isCustomProxySet(proxyParams))
                builder.useSystemProperties();
            else
                setCustomProxy(builder, proxyParams);
            setSSLTls("TLSv1.2");
            disableCertificateValidation(builder);
            client = builder.setDefaultHeaders(headers).build();

            postRequest = RequestBuilder.post()
                    .setHeader(Consts.AUTHORIZATION_HEADER, Consts.BEARER + accessToken)
                    .setHeader("Content-Length", "0")
                    .setUri(userInfoURL)
                    .build();
            //Add print request
            logger.debug("Print Request\n" + postRequest.getRequestLine());
            userInfoResponse = client.execute(postRequest);
            logger.debug("Print response \n" + userInfoResponse.getStatusLine());
            validateResponse(userInfoResponse, 200, INFO_FAILED);
            UserInfoDTO jsonResponse = parseJsonFromResponse(userInfoResponse, UserInfoDTO.class);
            permissions = getPermissions(jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            HttpClientUtils.closeQuietly(userInfoResponse);
        }
        return permissions;
    }

    private Permissions getPermissions(UserInfoDTO jsonResponse) {
        ArrayList<String> sastPermissions = jsonResponse.getSastPermissions();
        return new Permissions(sastPermissions.contains(Consts.SAVE_SAST_SCAN), sastPermissions.contains(Consts.MANAGE_RESULTS_COMMENT),
                sastPermissions.contains(Consts.MANAGE_RESULTS_EXPLOITABILITY));
    }

    private Long getAccessTokenExpirationInMilli(long accessTokenExpirationInSec) {
        long currentTime = System.currentTimeMillis();
        long accessTokenExpInMilli = accessTokenExpirationInSec * 1000;
        return currentTime + accessTokenExpInMilli;
    }

    public <T> T patchRequest(String relPath, String contentType, String entity, Class<T> responseType, int expectStatus, String failedMsg) throws IOException {
        HttpPatch patch = new HttpPatch(restUri + relPath);
        StringEntity entity1 = new StringEntity(entity, StandardCharsets.UTF_8);
        return request(patch, contentType, entity1, responseType, expectStatus, failedMsg, false, true);
    }

    public <T> T putRequest(String relPath, String contentType, String entity, Class<T> responseType, int expectStatus, String failedMsg) throws IOException {
        HttpPut put = new HttpPut(restUri + relPath);
        StringEntity entity1 = new StringEntity(entity, StandardCharsets.UTF_8);
        return request(put, contentType, entity1, responseType, expectStatus, failedMsg, false, true);
    }

    private <T> T request(HttpRequestBase httpMethod, String contentType, HttpEntity entity, Class<T> responseType, int expectStatus, String failedMsg, boolean isCollection, boolean retry) throws IOException {
        if (contentType != null) {
            httpMethod.addHeader("Content-type", contentType);
        }
        if (entity != null && httpMethod instanceof HttpEntityEnclosingRequestBase) { //Entity for Post methods
            ((HttpEntityEnclosingRequestBase) httpMethod).setEntity(entity);
        }
        HttpResponse response = null;
        int statusCode = 0;

        try {
            httpMethod.addHeader(ORIGIN_HEADER, getCxOrigin());
            httpMethod.addHeader(ORIGIN_URL_HEADER, getCxOriginUrl());
            httpMethod.addHeader(TEAM_PATH, getCxTeam());
            if (accessTokenDTO.getAccessToken() != null) {
                httpMethod.addHeader(HttpHeaders.AUTHORIZATION, accessTokenDTO.getTokenType() + " " + accessTokenDTO.getAccessToken());
            }

            response = client.execute(httpMethod);
            statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == HttpStatus.SC_UNAUTHORIZED) { // Token has probably expired
                throw new CxRestClientException("Failed to Authenticate Token");
            }

            validateResponse(response, expectStatus, "Failed to " + failedMsg);

            //extract response as object and return the link
            return convertToObject(response, responseType);
        } catch (UnknownHostException e) {
            logger.error(e.getMessage());
            try {
                throw new CxRestClientException("Connection to checkMarx server failed " + e.getMessage());
            } catch (CxRestClientException ex) {
                throw new RuntimeException(ex);
            }

        } catch (CxRestClientException e) {
            throw new RuntimeException(e);
        } catch (CxValidateResponseException e) {
            throw new RuntimeException(e);
        } finally {
            httpMethod.releaseConnection();
            HttpClientUtils.closeQuietly(response);
        }
    }

    private static void validateResponse(HttpResponse response, int status, String message) throws CxValidateResponseException {
        try {
            if (response.getStatusLine().getStatusCode() != status) {
                String responseBody = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
                responseBody = responseBody.replace("{", "").replace("}", "").replace(System.getProperty("line.separator"), " ").replace("  ", "");
                if (responseBody.contains("<!DOCTYPE html>")) {
                    throw new CxValidateResponseException(message + ": " + "status code: 500. Error message: Internal Server Error");
                } else if (responseBody.contains("\"error\":\"invalid_grant\"")) {
                    logger.error("[CHECKMARX] - Fail to validate response, response: " + responseBody);
                    throw new CxValidateResponseException(message);
                } else {
                    throw new CxValidateResponseException(message + ": " + "status code: " + response.getStatusLine() + ". Error message:" + responseBody);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new CxValidateResponseException("Error parse REST response body: " + e.getMessage());
        }
    }

    private static <ResponseObj> ResponseObj parseJsonFromResponse(HttpResponse response, Class<ResponseObj> dtoClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(createStringFromResponse(response).toString(), dtoClass);
    }

    private static StringBuilder createStringFromResponse(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return result;
    }


    private boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    private boolean isCustomProxySet(ProxyParams proxyConfig) {
        return proxyConfig != null &&
                proxyConfig.getServer() != null && !proxyConfig.getServer().isEmpty() &&
                proxyConfig.getPort() != 0;
    }

    private void setCustomProxy(HttpClientBuilder cb, ProxyParams proxyConfig) {
        String scheme = proxyConfig.getType();
        HttpHost proxy = new HttpHost(proxyConfig.getServer(), proxyConfig.getPort(), scheme);
        if (!isEmpty(proxyConfig.getUsername()) &&
                !isEmpty(proxyConfig.getPassword())) {
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(proxyConfig.getUsername(), proxyConfig.getPassword());
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(proxy), credentials);
            cb.setDefaultCredentialsProvider(credsProvider);
        }

        logger.info("Setting proxy for Checkmarx http client");
        cb.setProxy(proxy);
        cb.setRoutePlanner(new DefaultProxyRoutePlanner(proxy));
        cb.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
    }

    private static <T> T convertToObject(HttpResponse response, Class<T> responseType) throws CxRestClientException {
        ObjectMapper mapper = getObjectMapper();
        try {
            if (responseType != null && responseType.isInstance(response)) {
                return (T) response;
            }


            // If the caller is asking for the whole response, return the response (instead of just its entity),
            // no matter if the entity is empty.
            if (responseType != null && responseType.isAssignableFrom(response.getClass())) {
                return (T) response;
            }
            if (response.getEntity() == null) {
                return null;
            }
            String json = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
            if (responseType.equals(String.class)) {
                return (T) json;
            }
            return mapper.readValue(json, responseType);

        } catch (IOException e) {
            throw new CxRestClientException("Failed to parse json response: " + e.getMessage());
        }
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper result = new ObjectMapper();

        // Prevent UnrecognizedPropertyException if additional fields are added to API responses.
        result.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return result;
    }

    private HttpClientBuilder disableCertificateValidation(HttpClientBuilder builder) {
        try {
            SSLContext disabledSSLContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
            builder.setSslcontext(disabledSSLContext);
            builder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
            //Add using proxy
            if (!isCustomProxySet(proxyParams))
                builder.useSystemProperties();
            else
                setCustomProxy(builder, proxyParams);
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            logger.warn("Failed to disable certificate verification: " + e.getMessage());
        }

        return builder;
    }

    private void setSSLTls(String protocol) {
        try {
            final SSLContext sslContext = SSLContext.getInstance(protocol);
            sslContext.init(null, null, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            logger.warn("Failed to set SSL TLS : " + e.getMessage());
        }
    }
}