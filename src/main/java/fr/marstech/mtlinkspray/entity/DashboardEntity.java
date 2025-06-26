package fr.marstech.mtlinkspray.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Document(collection = "mt-link-spray-dashboard-entity")
public class DashboardEntity extends StandardEntity {

    @Id
    private String id;

    @NonNull
    private String name;

    @Builder.Default
    private List<DashboardItem> items = List.of();
}
