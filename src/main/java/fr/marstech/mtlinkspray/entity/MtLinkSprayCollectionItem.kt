package fr.marstech.mtlinkspray.entity;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@SuperBuilder
@Document(collection = "mt-link-spray-collection")
public class MtLinkSprayCollectionItem extends StandardEntity {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();
}
