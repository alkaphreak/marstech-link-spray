package fr.marstech.mtlinkspray.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DashboardCategory extends DashboardItem {

    private List<DashboardLink> links = List.of();
}
