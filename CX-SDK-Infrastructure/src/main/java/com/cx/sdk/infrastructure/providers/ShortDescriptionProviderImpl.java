package com.cx.sdk.infrastructure.providers;

import javax.inject.Inject;

import com.cx.sdk.application.contracts.providers.SDKConfigurationProvider;
import com.cx.sdk.application.contracts.providers.ShortDescriptionProvider;
import com.cx.sdk.domain.exceptions.SdkException;
import com.cx.sdk.oidcLogin.CxOIDCConnector;
import com.cx.sdk.oidcLogin.api.CxOIDCLoginClientImpl;
import com.cx.sdk.oidcLogin.restClient.CxServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShortDescriptionProviderImpl implements ShortDescriptionProvider {
	
	public static final String FAILED_TO_FETCH_SHORT_DESC = "Failed to Fetch Short Description";
    private final SDKConfigurationProvider sdkConfigurationProvider;
    private final Logger logger = LoggerFactory.getLogger(ShortDescriptionProviderImpl.class);
    private String shortDescription = "Click on the vulnerable file to view.";

    @Inject
    public ShortDescriptionProviderImpl(SDKConfigurationProvider sdkConfigurationProvider) {
        this.sdkConfigurationProvider = sdkConfigurationProvider;
    }

	@Override
	public String fetchShortDescription(String accessToken, long scanId, long pathId) {
		 try {
	            logger.info("cxOIDCLoginClient start fetchShortDescription");
	            String serverURL = sdkConfigurationProvider.getCxServerUrl().toString();
	            CxOIDCConnector connector = new CxOIDCConnector(new CxServerImpl(serverURL), null, null);
	            shortDescription = connector.fetchShortDescription(accessToken, scanId, pathId);
	        } catch (Exception e) {
	            String errorMessage = String.format("Failed to fetch short description login to server: [%s]\nError: %s",
	                    sdkConfigurationProvider.getCxServerUrl().toString(), e.getMessage());
	            logger.error(errorMessage, e);
	            throw new SdkException(errorMessage, e);
	        }
		return shortDescription;
	}   
}
