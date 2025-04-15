package fr.marstech.mtlinkspray.repository;

import fr.marstech.mtlinkspray.entity.DashboardEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository extends MongoRepository<DashboardEntity, String> {

}
