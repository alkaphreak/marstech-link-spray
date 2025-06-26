package fr.marstech.mtlinkspray.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class DashboardItem {

    private String id;
    private String name;
    private String description;
}
