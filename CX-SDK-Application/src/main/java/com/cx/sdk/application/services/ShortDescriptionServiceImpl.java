package com.cx.sdk.application.services;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cx.sdk.application.contracts.providers.ShortDescriptionProvider;
import com.cx.sdk.application.contracts.providers.SDKConfigurationProvider;

public class ShortDescriptionServiceImpl implements ShortDescriptionService{

    private static final Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);

    public static final String FAILED_TO_FETCH = "Failed to Fetch Short Description";

    private final ShortDescriptionProvider shortDescriptionProvider;
    private final SDKConfigurationProvider sdkConfigurationProvider;
    
    @Inject
    public ShortDescriptionServiceImpl(ShortDescriptionProvider shortDescriptionProvider, SDKConfigurationProvider sdkConfigurationProvider) {
		this.shortDescriptionProvider = shortDescriptionProvider;
		this.sdkConfigurationProvider = sdkConfigurationProvider; 
	}

	@Override
    public String fetchShortDescription(String accessToken, long scanId, long pathId  ) {
    	String shortDescription = "Click on the vulnerable file to view.";
    	try {
    		shortDescription  = shortDescriptionProvider.fetchShortDescription(accessToken, scanId, pathId);
    	}catch (Exception ex){
    		 logger.error(FAILED_TO_FETCH, ex);
    	}
    	return shortDescription;
    }

}
