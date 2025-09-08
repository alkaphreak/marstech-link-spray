package fr.marstech.mtlinkspray.entity;

import lombok.*;
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

    @Builder.Default
    private List<DashboardLink> links = List.of();
}
