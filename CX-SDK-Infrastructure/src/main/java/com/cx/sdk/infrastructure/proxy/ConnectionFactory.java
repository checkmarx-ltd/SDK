package com.cx.sdk.infrastructure.proxy;

import com.cx.sdk.application.contracts.providers.SDKConfigurationProvider;
import com.cx.sdk.domain.entities.ProxyParams;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.net.ssl.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class ConnectionFactory implements HttpUrlConnectorProvider.ConnectionFactory {
    private final SDKConfigurationProvider sdkConfigurationProvider;
    private SSLContext sslContext;
    private static final Logger logger = LoggerFactory.getLogger(ConnectionFactory.class);

    @Inject
    public ConnectionFactory(SDKConfigurationProvider sdkConfigurationProvider) {
        this.sdkConfigurationProvider = sdkConfigurationProvider;
    }

    private HttpURLConnection getHttpURLConnection(URL url) throws IOException {
        Proxy proxy = null;
        Proxy.Type proxyType = null;
        ProxyParams proxyParams = sdkConfigurationProvider.getProxyParams();
        if (proxyParams != null && proxyParams.getType() != null) {
            if (proxyParams.getType().equalsIgnoreCase("HTTPS")
                    || proxyParams.getType().equalsIgnoreCase("HTTP")) {
                proxyType = Proxy.Type.HTTP;
            } else {
                proxyType = Proxy.Type.valueOf(proxyParams.getType());
            }
            proxy = new Proxy(proxyType, new InetSocketAddress(proxyParams.getServer(), proxyParams.getPort()));
        } else {
            proxy = Proxy.NO_PROXY;
        }
        if (url.getProtocol().equalsIgnoreCase("https")) {
            HttpsURLConnection httpsCon = (HttpsURLConnection) url.openConnection(proxy);
            httpsCon.setHostnameVerifier(getHostnameVerifier());
            httpsCon.setSSLSocketFactory(getSSLSocketFactory());
            return httpsCon;
        }
        HttpURLConnection con = (HttpURLConnection) url.openConnection(proxy);
        return con;
    }

    private SSLSocketFactory getSSLSocketFactory() throws SSLException {
        TrustStrategy acceptingTrustStrategy = new TrustAllStrategy();
        SSLContext sslContext;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new SSLException("Fail to set trust all certificate, 'SSLConnectionSocketFactory'", e);
        }
        return sslContext.getSocketFactory();
    }

    private HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
                return true;
            }
        };
    }


    @Override
    public HttpURLConnection getConnection(URL url) throws IOException {
        return getHttpURLConnection(url);
    }
}