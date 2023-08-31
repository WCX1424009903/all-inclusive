package org.example.dao;

import org.example.domain.MongoTest;
import org.example.mongodb.service.MongoDaoSupport;
import org.springframework.stereotype.Repository;

@Repository
public class MongoTestRepository extends MongoDaoSupport<MongoTest> {

}
