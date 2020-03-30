// Copyright (c) Dolittle. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package io.dolittle.azure.ad.reader.console.model.roledefinition;

import lombok.Data;

@Data
public class RoleDefinition {
    public String id;
    public String type;
    public String name;
    public RoleDefinitionProperty properties;
}
