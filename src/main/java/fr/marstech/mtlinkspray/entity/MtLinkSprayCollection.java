package fr.marstech.mtlinkspray.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "mt-link-spray-collection")
public class MtLinkSprayCollection {

    @Id
    private String id;

    private String description;


}
