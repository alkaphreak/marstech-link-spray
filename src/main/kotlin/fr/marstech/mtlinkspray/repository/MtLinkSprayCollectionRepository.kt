package fr.marstech.mtlinkspray.repository;

import fr.marstech.mtlinkspray.entity.MtLinkSprayCollectionItem;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MtLinkSprayCollectionRepository extends MongoRepository<MtLinkSprayCollectionItem, String> {

    @NotNull Optional<MtLinkSprayCollectionItem> findById(@NotNull String id);
}
