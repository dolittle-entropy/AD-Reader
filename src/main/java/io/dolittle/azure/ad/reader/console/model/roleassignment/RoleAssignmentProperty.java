// Copyright (c) Dolittle. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package io.dolittle.azure.ad.reader.console.model.roleassignment;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoleAssignmentProperty {
    private String roleDefinitionId;
    private String principalId;
    private String scope;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
    private String createdBy;
    private String updatedBy;
}
