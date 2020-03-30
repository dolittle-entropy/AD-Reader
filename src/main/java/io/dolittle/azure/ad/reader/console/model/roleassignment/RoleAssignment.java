// Copyright (c) Dolittle. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package io.dolittle.azure.ad.reader.console.model.roleassignment;

import io.dolittle.azure.ad.reader.console.model.roledefinition.RoleDefinition;
import lombok.Data;

@Data
public class RoleAssignment {
    private String id;
    private String type;
    private String name;
    private RoleAssignmentProperty properties;
    private RoleDefinition roleDefinition;
    private String principalType;


}
