package fr.marstech.mtlinkspray.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "mt-link-spray-abuse-reports")
public class AbuseReport {

    @Id
    private String id = UUID.randomUUID().toString();

    @NonNull
    private LocalDateTime creationDate = LocalDateTime.now();

    @NonNull
    private String abuseDescription;
}
