package fr.marstech.mtlinkspray.entity;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Abstract class representing a standard entity with common properties.
 * This class is used as a base class for other entities in the application.
 */

@EqualsAndHashCode()
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@SuperBuilder
public abstract class StandardEntity {

    @NonNull
    @Builder.Default
    private LocalDateTime creationDate = LocalDateTime.now();

    @NonNull
    @Builder.Default
    private Boolean isEnabled = true;

    private String description;
    private Map<String, String> metadata;
}
