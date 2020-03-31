// Copyright (c) Dolittle. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package io.dolittle.azure.ad.reader.console.client;

import io.dolittle.azure.ad.reader.console.model.roleassignment.RoleAssignmentList;
import io.dolittle.azure.ad.reader.console.model.roledefinition.RoleDefinition;
import io.dolittle.azure.ad.reader.console.service.AuthenticationService;
import io.dolittle.azure.ad.reader.console.util.RESTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Component
@Slf4j
public class AzureMgtClient {

    private static final String BASE_URL = "https://management.azure.com";
    private final String SUB_ID;
    private final AuthenticationService authenticationService;
    //private String path_RoleAssignments = "/subscriptions/%s/providers/Microsoft.Authorization/roleAssignments?api-version=2015-07-01&$filter=assignedTo('%s')";
    private static final String path_RoleAssignments = "/subscriptions/%s/providers/Microsoft.Authorization/roleAssignments?api-version=2015-07-01";
    private static final String path_RoleDefinitionById = "%s?api-version=2015-07-01";
    private HashMap<String, RoleDefinition> roleDefinitionCache = new HashMap<>();

    @Autowired
    public AzureMgtClient(@Value("${azure.app-subscription-id}") String subscriptionId, AuthenticationService authenticationService) {
        SUB_ID = subscriptionId;
        this.authenticationService = authenticationService;
    }

    public RoleAssignmentList getAllRoleAssignments() {

        String url = getURL(SUB_ID, path_RoleAssignments);

        log.info("URL: {}", url);

        RestTemplate restTemplate = RESTUtil.getRestTemplate();
        HttpHeaders headerWithBearerToken = RESTUtil.getHeaderWithBearerToken(getToken());
        HttpEntity<String> requestEntity = new HttpEntity<>("", headerWithBearerToken);

        ResponseEntity<RoleAssignmentList> responseEntity;
        responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, RoleAssignmentList.class);

        return responseEntity.getBody();

    }

    private String getToken() {
        return authenticationService.authenticate(AuthenticationService.TokenSource.MGT);
    }

    public RoleDefinition getRoleDefinition(String roleDefinitionId) {
        if (isInRoleDefinitionCache(roleDefinitionId)) {
            return roleDefinitionCache.get(roleDefinitionId);
        }

        String url = getURL(roleDefinitionId, path_RoleDefinitionById);

        log.info("URL: {}", url);

        RestTemplate restTemplate = RESTUtil.getRestTemplate();
        HttpHeaders headerWithBearerToken = RESTUtil.getHeaderWithBearerToken(getToken());
        HttpEntity<String> requestEntity = new HttpEntity<>("", headerWithBearerToken);

        ResponseEntity<RoleDefinition> responseEntity;
        responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, RoleDefinition.class);
        RoleDefinition roleDefinition = responseEntity.getBody();

        assert roleDefinition != null;
        roleDefinitionCache.put(roleDefinition.getId(), roleDefinition);

        return roleDefinition;
    }

    private Boolean isInRoleDefinitionCache(String roleDefinitionId) {
        RoleDefinition roleDefinition = roleDefinitionCache.get(roleDefinitionId);
        return roleDefinition != null;
    }

    private String getURL(String value, String template) {
        return BASE_URL + String.format(template, value);
    }

}
