package fr.marstech.mtlinkspray.entity;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "mt-link-spray-abuse-reports")
public class AbuseReport extends StandardEntity {

    @Id
    private String id = UUID.randomUUID().toString();

    @NonNull
    private String abuseDescription;
}
