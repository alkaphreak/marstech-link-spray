package fr.marstech.mtlinkspray.dto;

import fr.marstech.mtlinkspray.entity.DashboardItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
public class DashboardDto {

    private String id;
    private String name;
    private String description;
    private List<DashboardItem> items;
}
