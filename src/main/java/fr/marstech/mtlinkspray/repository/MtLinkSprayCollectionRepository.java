package fr.marstech.mtlinkspray.repository;

import fr.marstech.mtlinkspray.entity.MtLinkSprayCollection;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MtLinkSprayCollectionRepository extends MongoRepository<MtLinkSprayCollection, String> {

    Optional<MtLinkSprayCollection> findById(String id);
}
