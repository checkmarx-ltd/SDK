package com.cx.sdk.oidcLogin.webBrowsing;

import com.cx.sdk.oidcLogin.constants.Consts;
import com.cx.sdk.oidcLogin.exceptions.CxRestLoginException;
import com.google.common.base.Splitter;
import com.teamdev.jxbrowser.chromium.*;
import com.teamdev.jxbrowser.chromium.dom.DOMDocument;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;
import com.teamdev.jxbrowser.chromium.events.LoadListener;
import com.teamdev.jxbrowser.chromium.internal.Environment;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import com.teamdev.jxbrowser.chromium.swing.DefaultNetworkDelegate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class OIDCWebBrowser extends JFrame implements IOIDCWebBrowser {

    public static final String END_SESSION_FORMAT = "?id_token_hint=%s&post_logout_redirect_uri=%s";
    private String clientName;
    private JPanel contentPane;
    private String error;
    private Browser browser;
    private AuthenticationData response;
    private final Object lock = new Object();
    private Map<String, String> urlParamsMap;
    private String serverUrl;
    private String endSessionEndPoint;

    @Override
    public AuthenticationData browseAuthenticationData(String serverUrl, String clientName) throws Exception {
        this.clientName = clientName;
        this.serverUrl = serverUrl;
        String authorizationEndpointUrl = serverUrl + Consts.AUTHORIZATION_ENDPOINT;
        endSessionEndPoint = serverUrl + Consts.END_SESSION_ENDPOINT;
        initBrowser(authorizationEndpointUrl);
        waitForAuthentication();
        if (hasErrors()) {
            throw new CxRestLoginException(error);
        }

        return response;
    }

    private void initBrowser(String restUrl) {
        if (Environment.isMac()) {
            System.setProperty("java.ipc.external", "true");
            System.setProperty("jxbrowser.ipc.external", "true");

            if (!BrowserCore.isInitialized()) {
                BrowserCore.initialize();
            }
        }

        BrowserPreferences.setChromiumSwitches("--disable-google-traffic");
        contentPane = new JPanel(new GridLayout(1, 1));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        BrowserContext browserContext = BrowserContext.defaultContext();
        browserContext.getNetworkService().setNetworkDelegate(new DefaultNetworkDelegate() {
            @Override
            public void onBeforeSendHeaders(BeforeSendHeadersParams params) {
                params.getHeadersEx().setHeader("cxOrigin", clientName);
            }
        });
        browser = new Browser(browserContext);
        String postData = getPostData();
        LoadURLParams urlParams = new LoadURLParams(restUrl, postData);
        String pathToImage = "/checkmarxIcon.jpg";
        setIconImage(new ImageIcon(getClass().getResource(pathToImage), "checkmarx icon").getImage());
        browser.loadURL(urlParams);
        contentPane.add(new BrowserView(browser));
        browser.addLoadListener(AddResponsesHandler());
        setSize(700, 650);
        setLocationRelativeTo(null);
        getContentPane().add(contentPane, BorderLayout.CENTER);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                browser.dispose();
                if (response == null) {
                    response = new AuthenticationData(true);
                }
                notifyAuthenticationFinish();
            }
        });
        setVisible(true);
    }

    @Override
    public void logout(String idToken) {
        BrowserContext browserContext = BrowserContext.defaultContext();
        browser = new Browser(browserContext);
        browser.loadURL(endSessionEndPoint + String.format(END_SESSION_FORMAT, idToken, serverUrl + "/cxwebclient/"));
        browser.addLoadListener(disposeOnLoadDone());
    }

    private LoadListener disposeOnLoadDone() {
        return new LoadAdapter() {
            @Override
            public void onFinishLoadingFrame(FinishLoadingEvent event) {
                browser.dispose();
            }
        };
    }

    private void waitForAuthentication() {
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String getPostData() {
        StringBuilder sb = new StringBuilder();
        sb.append(Consts.CLIENT_ID_KEY);
        sb.append("=");
        sb.append(Consts.CLIENT_VALUE);
        sb.append("&");
        sb.append(Consts.SCOPE_KEY);
        sb.append("=");
        sb.append(Consts.SCOPE_VALUE);
        sb.append("&");
        sb.append(Consts.RESPONSE_TYPE_KEY);
        sb.append("=");
        sb.append(Consts.RESPONSE_TYPE_VALUE);
        sb.append("&");
        sb.append(serverUrl.endsWith("/") ? Consts.REDIRECT_URI_KEY + "=" + serverUrl : Consts.REDIRECT_URI_KEY + "=" + serverUrl + "/");
        return sb.toString();
    }

    private void notifyAuthenticationFinish() {
        synchronized (lock) {
            lock.notify();
        }
    }

    private LoadAdapter AddResponsesHandler() {
        return new LoadAdapter() {
            @Override
            public void onFinishLoadingFrame(FinishLoadingEvent event) {
                handleErrorResponse(event);
                handleResponse(event);
                if (response.code != null || hasErrors())
                    closePopup();
            }

        };
    }

    private void handleErrorResponse(FinishLoadingEvent event) {
        if (event.isMainFrame()) {

            checkForUrlQueryErrors(event);
            if (!hasErrors())
                checkForBodyErrors(event);
        }
    }

    private void checkForUrlQueryErrors(FinishLoadingEvent event) {
        if (!isUrlErrorResponse(event)) return;

        try {
            String queryStringParams = new URL(event.getValidatedURL()).getQuery();
            String[] params = queryStringParams.split("&");
            for (Integer i = 0; i < params.length; i++) {
                if (params[i].startsWith("Error")) {
                    error = java.net.URLDecoder.decode(params[i].substring(6), "UTF-8");
                    break;
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private boolean isUrlErrorResponse(FinishLoadingEvent event) {
        return event.getValidatedURL().contains("Error=");
    }

    private void checkForBodyErrors(FinishLoadingEvent event) {
        Browser browser = event.getBrowser();
        DOMDocument document = browser.getDocument();
        String content = document.getDocumentElement().getInnerHTML();

        if (!isBodyErrorResponse(content)) return;
        handleInternalServerError(content);

        if (hasErrors() || !content.contains("messageDetails")) return;
        extractMessageErrorFromBody(content);
    }

    private void handleInternalServerError(String content) {
        if (content.contains("HTTP 500")) {
            error = "Internal server error";
        }
    }

    private void extractMessageErrorFromBody(String content) {
        String[] contentComponents = content.split("\\r?\\n");
        for (String component : contentComponents) {
            if (component.contains("messageDetails")) {
                error = component.split(":")[1].trim();
                TrimError();
                break;
            }
        }
    }

    private void TrimError() {
        if (error.startsWith("\""))
            error = error.substring(1);
        if (error.endsWith("\""))
            error = error.substring(0, error.length() - 1);
    }

    private boolean isBodyErrorResponse(String content) {
        return content.toLowerCase().contains("messagecode");
    }

    private boolean validateUrlResponse(FinishLoadingEvent event) {
        return event.getValidatedURL().toLowerCase().contains(Consts.CODE_KEY);
    }

    private boolean hasErrors() {
        return error != null && !error.isEmpty();
    }

    private void handleResponse(FinishLoadingEvent event) {
        if (event.isMainFrame() && (validateUrlResponse(event)) && !hasErrors()) {
            String validatedURL = event.getValidatedURL();
            extractReturnedUrlParams(validatedURL);
            response = new AuthenticationData(urlParamsMap.get(Consts.CODE_KEY));
        }
    }

    private Map<String, String> extractReturnedUrlParams(String validatedURL) {
        String query = validatedURL.split("\\?")[1];
        urlParamsMap = Splitter.on('&').trimResults().withKeyValueSeparator("=").split(query);
        return urlParamsMap;
    }

    private void closePopup() {
        dispatchEvent(new WindowEvent(OIDCWebBrowser.this, WindowEvent.WINDOW_CLOSING));
    }

    @Override
    public void disposeBrowser() {
        browser.dispose();
    }

}