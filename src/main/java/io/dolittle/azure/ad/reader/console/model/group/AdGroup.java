// Copyright (c) Dolittle. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package io.dolittle.azure.ad.reader.console.model.group;

import io.dolittle.azure.ad.reader.console.model.BaseData;
import io.dolittle.azure.ad.reader.console.model.roleassignment.RoleAssignment;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper=true)
public class AdGroup extends BaseData {

    private List<User> users = new ArrayList<>();
    private List<RoleAssignment> roleAssignments = new ArrayList<>();
}
