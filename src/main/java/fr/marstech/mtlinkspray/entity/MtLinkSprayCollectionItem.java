package fr.marstech.mtlinkspray.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "mt-link-spray-collection")
public class MtLinkSprayCollectionItem {

    @Id
    private String id;

    private String description;

    private LocalDateTime creationDate;
}
