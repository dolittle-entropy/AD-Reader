// Copyright (c) Dolittle. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package io.dolittle.azure.ad.reader.console.auth;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.http.IHttpRequest;

public class SimpleAuthProvider implements IAuthenticationProvider {

    private final String accessToken;

    public SimpleAuthProvider(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public void authenticateRequest(IHttpRequest iHttpRequest) {
        iHttpRequest.addHeader("Authorization", "Bearer " + accessToken);
    }
}
