// Copyright (c) Dolittle. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package io.dolittle.azure.ad.reader.console.model.roleassignment;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class RoleAssignmentList {
    private List<RoleAssignment> value = new ArrayList<>();

    public List<RoleAssignment> getAllRoleAssignments() {
        return this.value;
    }

    public List<RoleAssignment> getRolesAssignmentsByPrincipalId(String principalId) {
        return value.stream().filter(roleAssignment -> roleAssignment.getProperties().getPrincipalId().equals(principalId)).collect(Collectors.toList());
    }
}
