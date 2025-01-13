package fr.marstech.mtlinkspray.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.util.Pair;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "mt-link-spray-items")
public class LinkItem {

    @Id
    private String id;

    @NonNull
    private LocalDateTime creationDate = LocalDateTime.now();

    @NonNull
    private LinkItemTarget target;

    @NonNull
    private Boolean isEnabled = true;

    private String name;
    private String description;
    private Map<String, String> metadata;
    private Set<String> urlsToReplaceByJs;
    private Set<String> stringsToReplaceByJs;
    private Pair<LocalDateTime, String> expirationDateAndDestination;
}
