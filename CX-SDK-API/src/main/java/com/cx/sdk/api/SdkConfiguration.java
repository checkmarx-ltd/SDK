package com.cx.sdk.api;

import com.cx.sdk.api.dtos.LoginTypeDTO;
import com.cx.sdk.domain.entities.ProxyParams;

import java.net.URL;

/**
 * Created by victork on 28/02/2017.
 */
public class SdkConfiguration {
    private final URL cxServerUrl;
    private final String cxOrigin;
    private final Boolean useKerberosAuthentication;
    private final ProxyParams proxyParams;


    public SdkConfiguration(URL cxServerUrl, String cxOrigin, ProxyParams proxyParams)
    {
        this(cxServerUrl, cxOrigin, false, proxyParams);
    }

    public SdkConfiguration(URL cxServerUrl, String cxOrigin, Boolean useKerberosAuthentication, ProxyParams proxyParams)
    {
        this.cxServerUrl = cxServerUrl;
        this.cxOrigin = cxOrigin;
        this.useKerberosAuthentication = useKerberosAuthentication;
        this.proxyParams = proxyParams;
    }

    public String getOriginName() {
        return cxOrigin;
    }

    public URL getCxServerUrl() {
        return cxServerUrl;
    }

    public Boolean useKerberosAuthentication() { return useKerberosAuthentication; }

    public ProxyParams getProxyParams() {
        return proxyParams;
    }
}
