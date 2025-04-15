package fr.marstech.mtlinkspray.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DashboardLink extends DashboardItem {

    private String url;
}
