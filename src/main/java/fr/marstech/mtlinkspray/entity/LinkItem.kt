package fr.marstech.mtlinkspray.entity;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.util.Pair;

import java.time.LocalDateTime;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Document(collection = "mt-link-spray-items")
public class LinkItem extends StandardEntity {

    @Id
    private String id;

    @NonNull
    private LinkItemTarget target;

    private String name;
    private Set<String> urlsToReplaceByJs;
    private Set<String> stringsToReplaceByJs;
    private Pair<LocalDateTime, String> expirationDateAndDestination;
}
