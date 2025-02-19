package com.cx.sdk.application.services;

public interface ShortDescriptionService {

	String fetchShortDescription(String accessToken, long scanId, long pathId  );

}
