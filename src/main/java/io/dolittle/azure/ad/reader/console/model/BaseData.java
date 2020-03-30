// Copyright (c) Dolittle. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package io.dolittle.azure.ad.reader.console.model;

import lombok.Data;

@Data
public class BaseData {
    private String id;
    private String displayName;
    private String description;
    private String odata_type;

}
