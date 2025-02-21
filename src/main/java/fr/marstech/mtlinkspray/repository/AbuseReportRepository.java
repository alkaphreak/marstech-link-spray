package fr.marstech.mtlinkspray.repository;

import fr.marstech.mtlinkspray.entity.AbuseReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AbuseReportRepository extends MongoRepository<AbuseReport, String> {

}
