package com.cx.sdk.infrastructure;

import com.checkmarx.v7.*;
import com.cx.sdk.application.contracts.exceptions.NotAuthorizedException;
import com.cx.sdk.application.contracts.providers.SDKConfigurationProvider;
import com.cx.sdk.domain.Session;
import com.cx.sdk.domain.entities.ProxyParams;
import com.cx.sdk.domain.exceptions.SdkException;
import com.cx.sdk.infrastructure.authentication.kerberos.DynamicAuthSupplier;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.transports.http.configuration.ProxyServerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.ws.BindingProvider;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;


/**
 * Created by ehuds on 2/28/2017.
 */
public class CxSoapClient {

    private final SDKConfigurationProvider sdkConfigurationProvider;
    private static final Logger logger = LoggerFactory.getLogger(CxSoapClient.class);

    public CxSoapClient(SDKConfigurationProvider sdkConfigurationProvider) {
        this.sdkConfigurationProvider = sdkConfigurationProvider;
        DynamicAuthSupplier.setKerberosActive(sdkConfigurationProvider.useKerberosAuthentication());
    }

    public CxWSResponseLoginData login(String userName, String password) throws Exception {
        logger.debug("Performing credentials login, SOAP client..");
        URL wsdlUrl = getWsdlUrl(sdkConfigurationProvider.getCxServerUrl());
        logger.info("Creating SDK web service on: " + wsdlUrl);
        CxSDKWebService cxSDKWebService = new CxSDKWebService(wsdlUrl);
        CxSDKWebServiceSoap cxSDKWebServiceSoap = cxSDKWebService.getCxSDKWebServiceSoap();

        String serviceEndpointUrl = (String) ((BindingProvider) cxSDKWebServiceSoap).getRequestContext().get(Message.ENDPOINT_ADDRESS);
        logger.info("CXF endpoint before: " + serviceEndpointUrl);
        serviceEndpointUrl = replaceHostName(serviceEndpointUrl).toString();
        logger.info("CXF endpoint After: " + serviceEndpointUrl);
        ((BindingProvider) cxSDKWebServiceSoap).getRequestContext().put(Message.ENDPOINT_ADDRESS, serviceEndpointUrl);

        Credentials credentials = new Credentials();
        credentials.setUser(userName);
        credentials.setPass(password);
        setProxySettingsForSoap(cxSDKWebServiceSoap);
        CxWSResponseLoginData responseLoginData = cxSDKWebServiceSoap.login(credentials, 1033);
        validateLoginResponse(responseLoginData);
        return responseLoginData;
    }

    private void setProxySettingsForSoap(CxSDKWebServiceSoap cxSDKWebServiceSoap) {
        ProxyParams proxyParams = sdkConfigurationProvider.getProxyParams();
        if (proxyParams.getType() != null) {
            logger.debug("setting proxy for soap client");
            Client client = ClientProxy.getClient(cxSDKWebServiceSoap);
            HTTPConduit conduit = (HTTPConduit) client.getConduit();
            String proxyServer = proxyParams.getServer();
            int proxyServerPort = proxyParams.getPort();
            if (proxyParams.getType().equals("HTTPS")) {
                proxyParams.setType(Proxy.Type.HTTP.name());
            }
            ProxyServerType proxyServerType = ProxyServerType.valueOf(proxyParams.getType());
            HTTPClientPolicy clientPolicy = new HTTPClientPolicy();
            clientPolicy.setProxyServerType(proxyServerType);
            clientPolicy.setProxyServer(proxyServer);
            clientPolicy.setProxyServerPort(proxyServerPort);
            if (proxyParams.getUsername() != null) {
                conduit.getProxyAuthorization().setUserName(proxyParams.getUsername());
                conduit.getProxyAuthorization().setPassword(proxyParams.getPassword());
            }
            conduit.setClient(clientPolicy);
        }
    }

    public CxWSResponseLoginData ssoLogin() throws Exception {
        CxSDKWebServiceSoap cxSDKWebServiceSoap = createProxy();

        CxWSResponseLoginData responseLoginData = cxSDKWebServiceSoap.ssoLogin(new Credentials(), 1033);
        validateLoginResponse(responseLoginData);
        return responseLoginData;
    }

    public CxWSResponsePresetList getPresets(Session session) throws Exception {
        CxSDKWebServiceSoap cxSDKWebServiceSoap = createProxy();
        CxWSResponsePresetList response = cxSDKWebServiceSoap.getPresetList(session.getSessionId());
        validateResponse(response);
        return response;
    }

    public CxWSResponseGroupList getTeams(Session session) throws Exception {
        CxSDKWebServiceSoap cxSDKWebServiceSoap = createProxy();
        CxWSResponseGroupList response = cxSDKWebServiceSoap.getAssociatedGroupsList(session.getSessionId());
        validateResponse(response);
        return response;
    }

    public CxWSResponseConfigSetList getConfigurations(Session session) throws Exception {
        CxSDKWebServiceSoap cxSDKWebServiceSoap = createProxy();
        CxWSResponseConfigSetList response = cxSDKWebServiceSoap.getConfigurationSetList(session.getSessionId());
        validateResponse(response);
        return response;
    }

    public Boolean isProjectNameValid(Session session, String projectName, String groupId) throws Exception {
        CxSDKWebServiceSoap cxSDKWebServiceSoap = createProxy();
        CxWSBasicRepsonse response = cxSDKWebServiceSoap.isValidProjectName(session.getSessionId(), projectName, groupId);
        validateResponse(response);
        Boolean isValid = response.isIsSuccesfull();
        return isValid;
    }

    private URL getWsdlUrl(URL cxServerUrl) {
        if (cxServerUrl.toString().endsWith("wsdl")) {
            return cxServerUrl;
        }

        try {
            return new URL(cxServerUrl, "/cxwebinterface/sdk/cxsdkwebservice.asmx?wsdl");
        } catch (MalformedURLException e) {
            return cxServerUrl;
        }
    }

    private void validateResponse(CxWSBasicRepsonse response) throws Exception {
        if (response.isIsSuccesfull())
            return;

        if ("ReConnect".equals(response.getErrorMessage()))
            throw new NotAuthorizedException();

        throw new Exception(response.getErrorMessage());
    }

    private void validateLoginResponse(CxWSBasicRepsonse response) throws SdkException {
        if (response == null || !response.isIsSuccesfull())
            throw new SdkException("Login failed");
    }

    private CxSDKWebServiceSoap createProxy() {
        URL wsdlUrl = getWsdlUrl(sdkConfigurationProvider.getCxServerUrl());
        CxSDKWebService cxSDKWebService = new CxSDKWebService(wsdlUrl);
        CxSDKWebServiceSoap cxSDKWebServiceSoap = cxSDKWebService.getCxSDKWebServiceSoap();

        String serviceEndpointUrl = (String) ((BindingProvider) cxSDKWebServiceSoap).getRequestContext().get(Message.ENDPOINT_ADDRESS);
        logger.info("CXF endpoint before: " + serviceEndpointUrl);
        serviceEndpointUrl = replaceHostName(serviceEndpointUrl).toString();
        logger.info("CXF endpoint After: " + serviceEndpointUrl);
        ((BindingProvider) cxSDKWebServiceSoap).getRequestContext().put(Message.ENDPOINT_ADDRESS, serviceEndpointUrl);

        setProxySettingsForSoap(cxSDKWebServiceSoap);
        return cxSDKWebServiceSoap;
    }

    private URL replaceHostName(String url) {
        String host = System.getProperty("cx.host");
        URL origUrl = null;
        try {
            origUrl = new URL(url);
            if (isEmpty(host) || origUrl.getHost().equalsIgnoreCase(host.trim())) {
                return origUrl;
            }

            return new URL(origUrl.getProtocol(), host.trim(), origUrl.getPort(), origUrl.getPath());
        } catch (Exception ex) {
            logger.error("Replacing hostname failed: " + ex.getMessage());
            return origUrl;
        }
    }

    private static boolean isEmpty(Object str) {
        return str == null || "".equals(str);
    }

}
