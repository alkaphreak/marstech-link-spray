package fr.marstech.mtlinkspray.repository;

import fr.marstech.mtlinkspray.entity.LinkItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface LinkItemRepository extends MongoRepository<LinkItem, String> {

    @Query(value = "{}", fields = "{ '_id' : 1 }")
    Set<String> findAllIds();
}
