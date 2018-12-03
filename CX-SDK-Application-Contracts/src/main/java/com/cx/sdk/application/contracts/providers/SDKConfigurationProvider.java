package com.cx.sdk.application.contracts.providers;

import com.cx.sdk.domain.entities.ProxyParams;

import java.net.URL;

/**
 * Created by ehuds on 2/25/2017.
 */
public interface SDKConfigurationProvider {
    String getCxOriginName();
    URL getCxServerUrl();
    ProxyParams getProxyParams();
}
