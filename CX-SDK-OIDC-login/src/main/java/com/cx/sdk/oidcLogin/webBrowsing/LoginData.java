package com.cx.sdk.oidcLogin.webBrowsing;

public class LoginData {

    private boolean wasCanceled;
    private String accessToken;
    private String refreshToken;
    private String idToken;
    private String cxVersion;

    private Long accessTokenExpirationInMillis;

    public LoginData(String accessToken, String refreshToken, Long accessTokenExpirationInMillis, String idToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpirationInMillis = accessTokenExpirationInMillis;
        this.idToken = idToken;
    }

    public LoginData(boolean wasCanceled) {
        this.wasCanceled = wasCanceled;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public boolean wasCanceled() {
        return wasCanceled;
    }

    public Long getAccessTokenExpirationInMillis() {
        return accessTokenExpirationInMillis;
    }
    
    public String getCxVersion() {
    	return cxVersion;
    }
    
    public void setCxVersion(String cxVersion) {
    	this.cxVersion = cxVersion;
    }

}