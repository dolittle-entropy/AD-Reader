// Copyright (c) Dolittle. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package io.dolittle.azure.ad.reader.console.client;

import com.google.gson.JsonObject;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.RoleAssignment;
import com.microsoft.graph.models.extensions.User;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.extensions.*;
import io.dolittle.azure.ad.reader.console.auth.SimpleAuthProvider;
import io.dolittle.azure.ad.reader.console.service.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Slf4j
public class GraphClient {
    private static IGraphServiceClient graphClient = null;
    private final AuthenticationService authenticationService;

    private void initGraphClient() {
        if (graphClient == null) {
            String accessToken = authenticationService.authenticate(AuthenticationService.TokenSource.GRAPH);
            // Create the auth provider
            SimpleAuthProvider authProvider = new SimpleAuthProvider(accessToken);

            // Build a Graph client
            graphClient = GraphServiceClient.builder()
                    .authenticationProvider(authProvider)
                    .buildClient();
        }
    }

    @Autowired
    public GraphClient(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public User getCurrentUser() {
        initGraphClient();
        // GET /me to get authenticated user
        return graphClient
                .me()
                .buildRequest()
                .get();
    }

    public IDirectoryObjectCollectionWithReferencesPage getUserMemberOf(String userId) {
        initGraphClient();
        return graphClient.users(userId).transitiveMemberOf().buildRequest().get();
    }

    public IGroupCollectionPage getAllGroups() {
        initGraphClient();
        return graphClient.groups().buildRequest().get();
    }

    public IDirectoryObjectCollectionWithReferencesPage getGroupMembers(String groupId) {
        initGraphClient();
        return graphClient.groups(groupId).transitiveMembers().buildRequest().get();
    }

    public IUserCollectionPage getAllUsers() {
        initGraphClient();
        //https://docs.microsoft.com/en-us/graph/api/resources/user?view=graph-rest-1.0
        Option select = new QueryOption("$select", "id, displayName, mail, userType");
        Option orderBy = new QueryOption("$orderby", "displayName");
        //Option expand = new QueryOption("$expand", "memberOf");
        return graphClient.users().buildRequest(Arrays.asList(select, orderBy)).get();
    }

    public enum oDataType {
        GROUP("#microsoft.graph.group"),
        ROLES("#microsoft.graph.directoryRole");
        private String type;

        oDataType(String type) {
            this.type = type;
        }

        public String str() {
            return this.type;
        }
    }

}
