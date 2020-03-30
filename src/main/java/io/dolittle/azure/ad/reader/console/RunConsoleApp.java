// Copyright (c) Dolittle. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package io.dolittle.azure.ad.reader.console;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.graph.models.extensions.DirectoryObject;
import com.microsoft.graph.models.extensions.User;
import com.microsoft.graph.requests.extensions.IDirectoryObjectCollectionWithReferencesPage;
import com.microsoft.graph.requests.extensions.IGroupCollectionPage;
import com.microsoft.graph.requests.extensions.IUserCollectionPage;
import io.dolittle.azure.ad.reader.console.client.AzureMgtClient;
import io.dolittle.azure.ad.reader.console.client.GraphClient;
import io.dolittle.azure.ad.reader.console.model.group.AdGroup;
import io.dolittle.azure.ad.reader.console.model.roleassignment.RoleAssignment;
import io.dolittle.azure.ad.reader.console.model.roleassignment.RoleAssignmentList;
import io.dolittle.azure.ad.reader.console.model.roledefinition.RoleDefinition;
import io.dolittle.azure.ad.reader.console.model.user.AdUser;
import io.dolittle.azure.ad.reader.console.model.user.Group;
import io.dolittle.azure.ad.reader.console.model.user.Roles;
import io.dolittle.azure.ad.reader.console.service.WorkbookService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RunConsoleApp implements ApplicationRunner {
    private final WorkbookService workbookService;
    private final AzureMgtClient azureMgtClient;
    private final GraphClient graphClient;
    private final Gson gson = new Gson();

    @Autowired
    public RunConsoleApp(WorkbookService workbookService, AzureMgtClient azureMgtClient, GraphClient graphClient) {
        this.workbookService = workbookService;
        this.azureMgtClient = azureMgtClient;
        this.graphClient = graphClient;
    }

    @Override
    public void run(ApplicationArguments args) {

        //For Testing
//        AdUser currentAdUser = getCurrentAdUser();
//        RoleAssignmentList allRoleAssignments = getAllRoleAssignments();
//
//        List<RoleAssignment> rolesAssignmentsByPrincipalId = allRoleAssignments.getRolesAssignmentsByPrincipalId(currentAdUser.getId());
//        populateRoleDefinitions(rolesAssignmentsByPrincipalId);
//        currentAdUser.setRoleAssignments(rolesAssignmentsByPrincipalId);

        //Prod
        List<AdUser> adUsers = getAdUsers();
        List<AdGroup> adGroups = getAdGroups();

        RoleAssignmentList allRoleAssignments = getAllRoleAssignments();

        populateAdUserRoleAssignments(adUsers, allRoleAssignments);
        populateAdGroupRoleAssignments(adGroups, allRoleAssignments);

        XSSFWorkbook workbook = workbookService.createWorkbook();
        workbookService.populateUsersSheet(adUsers, workbook);
        workbookService.populateAzureRolesSheet(adUsers, workbook);
        workbookService.populateAdGroupSheet(adGroups, workbook);
        workbookService.saveWorkbook(workbook, "Azure-AD-Users");

        log.info("done");
    }

    private RoleAssignmentList getAllRoleAssignments() {
        RoleAssignmentList allRoleAssignments = azureMgtClient.getAllRoleAssignments();

        log.info("Got {} roleAssignments: ", allRoleAssignments.getValue().size());
        return allRoleAssignments;
    }

    private void populateAdGroupRoleAssignments(List<AdGroup> adGroups, RoleAssignmentList roleAssignmentList) {
        adGroups.forEach(adGroup -> {
            List<RoleAssignment> raByPrincipalIdList = roleAssignmentList.getRolesAssignmentsByPrincipalId(adGroup.getId());
            raByPrincipalIdList.forEach(roleAssignment -> roleAssignment.setPrincipalType(adGroup.getDisplayName()));
            populateRoleDefinitions(raByPrincipalIdList);
            adGroup.setRoleAssignments(raByPrincipalIdList);
        });
    }

    private void populateAdUserRoleAssignments(List<AdUser> adUsers, RoleAssignmentList roleAssignmentList) {
        for (AdUser adUser : adUsers) {
            List<RoleAssignment> raByPrincipalIdList = roleAssignmentList.getRolesAssignmentsByPrincipalId(adUser.getId());
            raByPrincipalIdList.forEach(roleAssignment -> roleAssignment.setPrincipalType("U"));

            List<Group> groupList = adUser.getGroup();
            for (Group group : groupList) {
                List<RoleAssignment> raByGroupIdList = roleAssignmentList.getRolesAssignmentsByPrincipalId(group.getId());
                raByGroupIdList.forEach(roleAssignment -> roleAssignment.setPrincipalType(group.getDisplayName()));
                raByPrincipalIdList.addAll(raByGroupIdList);
            }
            populateRoleDefinitions(raByPrincipalIdList);
            adUser.setRoleAssignments(raByPrincipalIdList);
        }
    }

    private void populateRoleDefinitions(List<RoleAssignment> roleAssignmentList) {
        for (RoleAssignment ra : roleAssignmentList) {
            RoleDefinition roleDefinition = azureMgtClient.getRoleDefinition(ra.getProperties().getRoleDefinitionId());
            ra.setRoleDefinition(roleDefinition);
        }
    }

    //For testing
    private AdUser getCurrentAdUser() {
        User user = graphClient.getCurrentUser();
        AdUser adUser = gson.fromJson(user.getRawObject(), AdUser.class);
        populateDirectoryObjects(adUser);
        log.info("Group Count: {}", adUser.getGroup().size());
        log.info("Role Count: {}", adUser.getRoles().size());
        return adUser;
    }

    private List<AdUser> getAdUsers() {
        List<AdUser> adUsers = listUsers();
        log.info("Total users: {}", adUsers.size());
        return adUsers;
    }

    private List<AdUser> listUsers() {
        log.info("Listing all users:");
        IUserCollectionPage pagedUsers = graphClient.getAllUsers();
        List<User> userList = pagedUsers.getCurrentPage();
        List<AdUser> adUserList = new ArrayList<>();
        while (userList != null) {
            for (User user : userList) {
                //log.info("User: {}", user.displayName);
                AdUser adUser = gson.fromJson(user.getRawObject(), AdUser.class);
                populateDirectoryObjects(adUser);
                adUserList.add(adUser);
            }

            if (pagedUsers.getNextPage() == null) {
                break;
            }

            log.info("nextpage: {}", pagedUsers.getNextPage().getRequestUrl());
            pagedUsers = pagedUsers.getNextPage().buildRequest().get();
            userList = pagedUsers.getCurrentPage();
        }
        return adUserList;
    }

    private void populateDirectoryObjects(AdUser adUser) {
        IDirectoryObjectCollectionWithReferencesPage pagedGroups = graphClient.getUserMemberOf(adUser.getId());
        List<DirectoryObject> ugList = pagedGroups.getCurrentPage();

        while (ugList != null) {

            List<JsonObject> filteredGroups = ugList.stream().filter(directoryObject -> directoryObject.oDataType.equals(GraphClient.oDataType.GROUP.str())).map(DirectoryObject::getRawObject).collect(Collectors.toList());
            filteredGroups.forEach(jsonObject -> {
                Group group = gson.fromJson(jsonObject, Group.class);
                adUser.getGroup().add(group);
            });

            filteredGroups = ugList.stream().filter(directoryObject -> directoryObject.oDataType.equals(GraphClient.oDataType.ROLES.str())).map(DirectoryObject::getRawObject).collect(Collectors.toList());
            filteredGroups.forEach(jsonObject -> {
                Roles roles = gson.fromJson(jsonObject, Roles.class);
                adUser.getRoles().add(roles);
            });

            if (pagedGroups.getNextPage() == null) {
                break;
            }
            log.info("nextpage: {}", pagedGroups.getNextPage().getRequestUrl());
            pagedGroups = pagedGroups.getNextPage().buildRequest().get();
            ugList = pagedGroups.getCurrentPage();
        }

    }

    private List<AdGroup> getAdGroups() {
        List<AdGroup> adGroups = listGroups();
        log.info("Total groups: {}", adGroups.size());
        return adGroups;
    }

    private List<AdGroup> listGroups() {
        log.info("Listing groups:");
        IGroupCollectionPage pagedGroups = graphClient.getAllGroups();
        List<com.microsoft.graph.models.extensions.Group> groupList = pagedGroups.getCurrentPage();
        List<AdGroup> adGroupList = new ArrayList<>();

        while (groupList != null) {

            groupList.forEach(group -> {
                AdGroup adGroup = gson.fromJson(group.getRawObject(), AdGroup.class);
                populateAdGroupMembers(adGroup);
                adGroupList.add(adGroup);
            });

            if (pagedGroups.getNextPage() == null) {
                break;
            }

            log.info("nextpage: {}", pagedGroups.getNextPage().getRequestUrl());
            pagedGroups = pagedGroups.getNextPage().buildRequest().get();
            groupList = pagedGroups.getCurrentPage();
        }
        return adGroupList;
    }

    private void populateAdGroupMembers(AdGroup adGroup) {
        IDirectoryObjectCollectionWithReferencesPage pagedMembers = graphClient.getGroupMembers(adGroup.getId());
        List<DirectoryObject> membersList = pagedMembers.getCurrentPage();
        List<io.dolittle.azure.ad.reader.console.model.group.User> userList = new ArrayList<>();
        while (membersList != null) {
            //log.info("parsing members for groups");
            membersList.forEach(dirObj -> {
                io.dolittle.azure.ad.reader.console.model.group.User user = gson.fromJson(dirObj.getRawObject(), io.dolittle.azure.ad.reader.console.model.group.User.class);
                userList.add(user);
            });


            if (pagedMembers.getNextPage() == null) {
                break;
            }
            log.info("nextpage: {}", pagedMembers.getNextPage().getRequestUrl());
            pagedMembers = pagedMembers.getNextPage().buildRequest().get();
            membersList = pagedMembers.getCurrentPage();
        }
        adGroup.setUsers(userList);
    }
}
