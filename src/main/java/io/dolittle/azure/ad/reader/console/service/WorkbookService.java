// Copyright (c) Dolittle. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package io.dolittle.azure.ad.reader.console.service;

import io.dolittle.azure.ad.reader.console.model.group.AdGroup;
import io.dolittle.azure.ad.reader.console.model.group.User;
import io.dolittle.azure.ad.reader.console.model.roleassignment.RoleAssignment;
import io.dolittle.azure.ad.reader.console.model.user.Group;
import io.dolittle.azure.ad.reader.console.model.user.Roles;
import io.dolittle.azure.ad.reader.console.model.user.AdUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class WorkbookService {

    private final String FILE_PATH;
    private static final String FILE_EXT= ".xlsx";

    @Autowired
    public WorkbookService(@Value("${file.output.path}") String filePath) {
        FILE_PATH = filePath;
    }

    public XSSFWorkbook createWorkbook() {
        XSSFWorkbook wb = new XSSFWorkbook();
        log.info("Workbook created");
        return wb;
    }

    public void populateAdGroupSheet(List<AdGroup> groupList, XSSFWorkbook wb) {
        XSSFSheet sheet = wb.createSheet("Groups");
        int rowCounter = 0;
        int columnCounter = -1;
        XSSFRow row = sheet.createRow(rowCounter);
        //Header
        row.createCell(++columnCounter).setCellValue("ID");
        row.createCell(++columnCounter).setCellValue("DisplayName");
        row.createCell(++columnCounter).setCellValue("Description");

        for (AdGroup adGroup : groupList) {
            //Group data
            rowCounter = populateAdGroupData(adGroup, sheet, rowCounter);

            //Member data
            rowCounter = populateAdGroupMemberData(adGroup.getUsers(), sheet, rowCounter);

            //Azure Roless data
            rowCounter = populateAZ_RolesData(adGroup.getRoleAssignments(), sheet, rowCounter);

            rowCounter++; //Additional row as space
        }

    }

    private int populateAdGroupData(AdGroup adGroup, XSSFSheet sheet, int rowCounter) {
        XSSFRow row;
        rowCounter++;
        int columnCounter = -1;
        row = sheet.createRow((rowCounter));
        row.createCell(++columnCounter).setCellValue(adGroup.getId());
        row.createCell(++columnCounter).setCellValue(adGroup.getDisplayName());
        row.createCell(++columnCounter).setCellValue(adGroup.getDescription());

        return rowCounter;
    }

    private int populateAdGroupMemberData(List<User> userList, XSSFSheet sheet, int rowCounter) {
        XSSFRow row;
        if (userList.size() == 0) {
            return rowCounter;
        }
        rowCounter++;
        int columnCounter = 1;
        row = sheet.createRow((rowCounter));
        row.createCell(columnCounter).setCellValue("MEMBERS:");

        for (User user : userList) {
            rowCounter++;
            columnCounter = 1;
            row = sheet.createRow((rowCounter));
            row.createCell(++columnCounter).setCellValue(user.getDisplayName());
        }

        return rowCounter;
    }

    public void populateAzureRolesSheet(List<AdUser> userList, XSSFWorkbook wb) {
        XSSFSheet sheet = wb.createSheet("Roles Assignment");

        int rowCounter = 0;
        int columnCounter = -1;
        XSSFRow row = sheet.createRow(rowCounter);
        //Header
        row.createCell(++columnCounter).setCellValue("ID");
        row.createCell(++columnCounter).setCellValue("DisplayName");
        row.createCell(++columnCounter).setCellValue("RoleName");
        row.createCell(++columnCounter).setCellValue("RoleType");
        row.createCell(++columnCounter).setCellValue("RoleDescription");
        row.createCell(++columnCounter).setCellValue("RoleScope");

        for (AdUser adUser : userList) {
            rowCounter = populateAzureRolesData(adUser, sheet, rowCounter);
        }

    }

    private int populateAzureRolesData(AdUser user, XSSFSheet sheet, int rowCounter) {
        XSSFRow row;
        rowCounter++;
        int columnCounter = -1;
        row = sheet.createRow((rowCounter));
        row.createCell(++columnCounter).setCellValue(user.getId());
        row.createCell(++columnCounter).setCellValue(user.getDisplayName());
        List<RoleAssignment> roleAssignments = user.getRoleAssignments();
        rowCounter = populateAzureRoleDefinition(roleAssignments, sheet, rowCounter);
        return rowCounter;
    }

    public void populateUsersSheet(List<AdUser> userList, XSSFWorkbook wb) {
        XSSFSheet sheet = wb.createSheet("Users");
        int rowCounter = 0;
        int columnCounter = -1;
        XSSFRow row = sheet.createRow(rowCounter);

        //Header
        row.createCell(++columnCounter).setCellValue("userID");
        row.createCell(++columnCounter).setCellValue("DisplayName");
        row.createCell(++columnCounter).setCellValue("userType");
        row.createCell(++columnCounter).setCellValue("Email");
        for (AdUser adUser : userList) {
            //Userdata
            rowCounter = populateUserData(adUser, sheet, rowCounter);

            //Group data
            rowCounter = populateGroupData(adUser.getGroup(), sheet, rowCounter);

            //AD Roles data
            rowCounter = populateADRolesData(adUser.getRoles(), sheet, rowCounter);

            //Azure Roless data
            rowCounter = populateAZ_RolesData(adUser.getRoleAssignments(), sheet, rowCounter);

            rowCounter++; //Additional space after an user
        }
    }

    private int populateUserData(AdUser user, XSSFSheet sheet, int rowCounter) {
        XSSFRow row;
        rowCounter++;
        int columnCounter = -1;
        row = sheet.createRow((rowCounter));
        row.createCell(++columnCounter).setCellValue(user.getId());
        row.createCell(++columnCounter).setCellValue(user.getDisplayName());
        row.createCell(++columnCounter).setCellValue(user.getUserType());
        row.createCell(++columnCounter).setCellValue(user.getMail());
        return rowCounter;
    }

    private int populateGroupData(List<Group> groupList, XSSFSheet sheet, int rowCounter) {
        XSSFRow row;
        if (groupList.size() == 0) {
            return rowCounter;
        }
        rowCounter++;
        int columnCounter = 1;
        row = sheet.createRow((rowCounter));
        row.createCell(columnCounter).setCellValue("GROUPS:");

        for (Group group : groupList) {
            rowCounter++;
            columnCounter = 1;
            row = sheet.createRow((rowCounter));
            row.createCell(++columnCounter).setCellValue(group.getDisplayName());
            row.createCell(++columnCounter).setCellValue(group.getDescription());
        }
        return rowCounter;
    }

    private int populateADRolesData(List<Roles> rolesList, XSSFSheet sheet, int rowCounter) {
        XSSFRow row;
        if (rolesList.size() == 0) {
            return rowCounter;
        }
        rowCounter++;
        int columnCounter = 1;
        row = sheet.createRow((rowCounter));
        row.createCell(columnCounter).setCellValue("AD ROLES:");
        for (Roles roles : rolesList) {
            rowCounter++;
            columnCounter = 1;
            row = sheet.createRow((rowCounter));
            row.createCell(++columnCounter).setCellValue(roles.getDisplayName());
        }
        return rowCounter;
    }

    private int populateAZ_RolesData(List<RoleAssignment> raList, XSSFSheet sheet, int rowCounter) {
        XSSFRow row;
        if (raList.size() == 0) {
            return rowCounter;
        }
        rowCounter++;
        int columnCounter = 1;
        row = sheet.createRow((rowCounter));
        row.createCell(columnCounter).setCellValue("AZURE ROLES:");
        rowCounter = populateAzureRoleDefinition(raList, sheet, rowCounter);
        return rowCounter;
    }

    private int populateAzureRoleDefinition(List<RoleAssignment> raList, XSSFSheet sheet, int rowCounter) {
        int columnCounter;
        XSSFRow row;
        for (RoleAssignment ra : raList) {
            rowCounter++;
            columnCounter = 1;
            row = sheet.createRow((rowCounter));
            row.createCell(++columnCounter).setCellValue(ra.getRoleDefinition().getProperties().getRoleName());
            row.createCell(++columnCounter).setCellValue(ra.getRoleDefinition().getProperties().getType());
            row.createCell(++columnCounter).setCellValue(ra.getPrincipalType());
            row.createCell(++columnCounter).setCellValue(ra.getProperties().getScope());
            row.createCell(++columnCounter).setCellValue(ra.getRoleDefinition().getProperties().getDescription());
        }
        return rowCounter;
    }

    public void saveWorkbook(XSSFWorkbook workbook, String fileName){
        FileOutputStream xlsxFile;
        try {
            xlsxFile = new FileOutputStream(FILE_PATH + fileName + FILE_EXT);
            workbook.write(xlsxFile);
            xlsxFile.flush();
            xlsxFile.close();
        } catch (IOException e) {
            log.error("Unable to save Workbook", e);
        }
    }
}
