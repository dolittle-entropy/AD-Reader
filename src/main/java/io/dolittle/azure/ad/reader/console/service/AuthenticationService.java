// Copyright (c) Dolittle. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package io.dolittle.azure.ad.reader.console.service;

import com.microsoft.aad.msal4j.DeviceCode;
import com.microsoft.aad.msal4j.DeviceCodeFlowParameters;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.PublicClientApplication;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CharSequenceReader;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

@Service
@Slf4j
public class AuthenticationService {
    private final String appId;
    private final String authority;
    private final Environment env;
    private final String TOKEN_PATH;
    private HashMap<TokenSource, String> tokenCache = new HashMap<>();
    private final Boolean STORE_TOKEN;

    public AuthenticationService(Environment env) {
        appId = env.getProperty("azure.app.client-id");
        authority = env.getProperty("azure.oauth.authorization-uri");
        TOKEN_PATH = env.getProperty("file.output.path");
        STORE_TOKEN = env.getProperty("debug.store.token", Boolean.class);
        this.env = env;
    }

    public String authenticate(TokenSource source) {
        String token = check4Token(source);
        if (token.isEmpty()) {
            token = doLogin(source);
        }
        return token;
    }

    private String doLogin(TokenSource tokenSource) {
        log.info("Authenticating!!");
        PublicClientApplication app;
        try {
            // Build the MSAL application object with
            // app ID and authority
            app = PublicClientApplication.builder(appId)
                    .authority(authority)
                    .build();
        } catch (MalformedURLException e) {
            log.error(e.getMessage());
            return null;
        }

        Consumer<DeviceCode> deviceCodeConsumer = (DeviceCode deviceCode) -> {
            // Print the login information to the console
            System.out.println(deviceCode.message());
        };

        Set<String> scopes = getScope(tokenSource);

        // Request a token, passing the requested permission scopes
        IAuthenticationResult result = app.acquireToken(
                DeviceCodeFlowParameters
                        .builder(scopes, deviceCodeConsumer)
                        .build()
        ).exceptionally(ex -> {
            System.out.println("Unable to authenticate - " + ex.getMessage());
            return null;
        }).join();

        if (result != null) {
            log.info("Got a token:");
            try {
                storeToken(result.accessToken(), tokenSource);
            } catch (IOException e) {
                log.error("Unable to store token", e);
            }
            return result.accessToken();
        }
        return null;
    }

    private Set<String> getScope(TokenSource tokenSource) {
        Set<String> scopes;
        switch (tokenSource) {
            case MGT:
                scopes = Set.of(Objects.requireNonNull(env.getProperty("azure.app.mgt.scopes")).split(","));
                break;
            case GRAPH:
                scopes = Set.of(Objects.requireNonNull(env.getProperty("azure.app.graph.scopes")).split(","));
                break;
            default:
                throw new RuntimeException("Unknown TokenSource. Unable to set Scope");
        }
        return scopes;
    }

    private void storeToken(String token, TokenSource tokenSource) throws IOException {
        if (!STORE_TOKEN) {
            return;
        }

        Reader reader = new CharSequenceReader(token);
        File targetFile = new File(TOKEN_PATH + tokenSource.str());
        FileUtils.touch(targetFile);
        byte[] buffer = IOUtils.toByteArray(reader, StandardCharsets.UTF_8);
        FileUtils.writeByteArrayToFile(targetFile, buffer);

        reader.close();
        tokenCache.put(tokenSource, token);
    }

    private String check4Token(TokenSource tokenSource) {
        String cachedToken = tokenCache.get(tokenSource);
        if (cachedToken != null && !cachedToken.isEmpty()) {
            log.debug("Using token from cache.");
            return cachedToken;
        }

        File file;
        try {
            file = FileUtils.getFile(getClass().getClassLoader()
                    .getResource(tokenSource.str())
                    .getPath());
        } catch (NullPointerException e) {
            log.info("No previous token file");
            return "";
        }

        try {
            String token = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            tokenCache.put(tokenSource, token);
            return token;
        } catch (IOException e) {
            log.error("Unable to read token", e);
        }
        return "";
    }

    public enum TokenSource {
        GRAPH("graphToken.txt"),
        MGT("mgtToken.txt");
        private String source;

        TokenSource(String source) {
            this.source = source;
        }
        public String str() {
            return this.source;
        }
    }

}
