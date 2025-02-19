package com.cx.sdk.application.contracts.providers;

public interface ShortDescriptionProvider {

	String fetchShortDescription(String accessToken, long scanId, long pathId  );

}
