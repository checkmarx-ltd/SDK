package com.cx.sdk.oidcLogin.webBrowsing;

import com.cx.sdk.domain.entities.ProxyParams;
import com.cx.sdk.oidcLogin.constants.Consts;
import com.cx.sdk.oidcLogin.exceptions.CxRestLoginException;
import com.google.common.base.Splitter;
import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.browser.event.ConsoleMessageReceived;
import com.teamdev.jxbrowser.cookie.Cookie;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.engine.EngineOptions;
import com.teamdev.jxbrowser.engine.RenderingMode;
import com.teamdev.jxbrowser.frame.Frame;
import com.teamdev.jxbrowser.js.ConsoleMessage;
import com.teamdev.jxbrowser.navigation.LoadUrlParams;
import com.teamdev.jxbrowser.navigation.Navigation;
import com.teamdev.jxbrowser.navigation.NavigationException;
import com.teamdev.jxbrowser.navigation.callback.StartNavigationCallback;
import com.teamdev.jxbrowser.navigation.event.FrameLoadFinished;
import com.teamdev.jxbrowser.net.HttpHeader;
import com.teamdev.jxbrowser.net.callback.AuthenticateCallback;
import com.teamdev.jxbrowser.net.callback.BeforeStartTransactionCallback;
import com.teamdev.jxbrowser.net.callback.BeforeUrlRequestCallback;
import com.teamdev.jxbrowser.net.callback.VerifyCertificateCallback;
import com.teamdev.jxbrowser.os.Environment;
import com.teamdev.jxbrowser.time.Timestamp;
import com.teamdev.jxbrowser.view.swing.BrowserView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
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
    private ProxyParams proxyParams;
    private final Logger logger = LoggerFactory.getLogger(OIDCWebBrowser.class);

    public OIDCWebBrowser(ProxyParams proxyParams) {
        this.proxyParams = proxyParams;
    }

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
          logger.info("Run On MAC");
          System.setProperty("java.ipc.external", "true");
          System.setProperty("jxbrowser.ipc.external", "true");
        }

        Engine engine = Engine.newInstance(
            EngineOptions.newBuilder(RenderingMode.HARDWARE_ACCELERATED)
            	.licenseKey("1BNDHFSC1G0Q2KGCY5QPJXJLZP3ENA0PVFNNF0E9KY6CLEMYB695HED4HO6XKJOR825V3L")
                .addSwitch("--disable-google-traffic").build());

        contentPane = new JPanel(new GridLayout(1, 1));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        engine.network().set(BeforeStartTransactionCallback.class,
            params -> BeforeStartTransactionCallback.Response
                .override(Arrays.asList(HttpHeader.of("cxOrigin", clientName))));

        engine.network().set(AuthenticateCallback.class, (params, tell) -> {

          if (params.isProxy() && proxyParams != null) {
            logger.info("Login with Proxy");
            tell.authenticate(proxyParams.getUsername(), proxyParams.getPassword());
            logger.info("Proxy username: " + proxyParams.getUsername());

          } else {
            // *******************************************************************
            // super.onAuthRequired(params);
          }


        });

        Browser browser = engine.newBrowser();
        String postData = getPostData();
        logger.info("Print PostData: " + postData);
        LoadUrlParams urlParams =
            LoadUrlParams.newBuilder(restUrl).postData(postData).build();// new LoadURLParams(restUrl, postData);
        String pathToImage = "/checkmarxIcon.jpg";
        setIconImage(
            new ImageIcon(getClass().getResource(pathToImage), "checkmarx icon")
                .getImage());
        browser.navigation().loadUrl(urlParams);
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            logger.info("Open browser ");
            contentPane.add(BrowserView.newInstance(browser));
            logger.info("Open popup");
            browser.navigation().on(FrameLoadFinished.class,
                event -> AddResponsesHandler(event));

            setSize(700, 650);
            setLocationRelativeTo(null);
            getContentPane().add(contentPane, BorderLayout.CENTER);
            addWindowListener(new WindowAdapter() {
              @Override
              public void windowClosing(WindowEvent e) {
                logger.info("Run browser.dispose()");
                browser.close();
                if (response == null) {
                  response = new AuthenticationData(true);
                }
                notifyAuthenticationFinish();
              }
            });
            setVisible(true);
          }
        });
      }


    @Override
    public void logout(String idToken) {
        
        Engine engine = Engine.newInstance(
            EngineOptions.newBuilder(RenderingMode.HARDWARE_ACCELERATED)
            .licenseKey("1BNDHFSC1G0Q2KGCY5QPJXJLZP3ENA0PVFNNF0E9KY6CLEMYB695HED4HO6XKJOR825V3L")
            .build());
        Browser browser = engine.newBrowser();
        browser.navigation().loadUrl(endSessionEndPoint + String
            .format(END_SESSION_FORMAT, idToken, serverUrl + "/cxwebclient/"));

        browser.navigation().on(FrameLoadFinished.class, event -> {
          browser.close();
        });
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

    private void AddResponsesHandler(FrameLoadFinished event) {
        logger.info("Run AddResponsesHandler");
        handleErrorResponse(event);
        handleResponse(event);
        logger.info("Print response.code: " + response.code);
        if (response.code != null || hasErrors())
          closePopup();
    }


    private void handleErrorResponse(FrameLoadFinished event) {
        if (event.frame().isMain()) {

          checkForUrlQueryErrors(event);
          if (!hasErrors())
            checkForBodyErrors(event);
        }
    }

    private void checkForUrlQueryErrors(FrameLoadFinished event) {
        if (!isUrlErrorResponse(event))
          return;

        try {
          String queryStringParams = new URL(event.url()).getQuery();
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

    private boolean isUrlErrorResponse(FrameLoadFinished event) {
        return event.url().contains("Error=");
    }

    private void checkForBodyErrors(FrameLoadFinished event) {
        Browser browser = event.frame().browser();
        Frame frame = event.frame();
        frame.document().ifPresent(document -> {
          document.documentElement().ifPresent(element -> {
            String content = element.innerHtml();
            if (!isBodyErrorResponse(content))
              return;
            handleInternalServerError(content);

            if (hasErrors() || !content.contains("messageDetails"))
              return;
            extractMessageErrorFromBody(content);
          });
        });
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

    private boolean validateUrlResponse(FrameLoadFinished event) {
        return event.url().toLowerCase().contains(Consts.CODE_KEY);
    }

    private boolean hasErrors() {
        logger.info("Has Error? ");
        logger.info("Print error: " + error);
        return error != null && !error.isEmpty();
    }

    private void handleResponse(FrameLoadFinished event) {
        if (event.frame().isMain() && (validateUrlResponse(event))
            && !hasErrors()) {
          String validatedURL = event.url();
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
        logger.info("ClosePopup");
        dispatchEvent(new WindowEvent(OIDCWebBrowser.this, WindowEvent.WINDOW_CLOSING));
    }

    @Override
    public void disposeBrowser() {
    	browser.close();
    }

}