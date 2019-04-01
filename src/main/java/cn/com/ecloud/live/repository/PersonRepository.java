package cn.com.ecloud.live.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import cn.com.ecloud.live.entity.Person;

public interface PersonRepository extends ReactiveMongoRepository<Person, String> {

}
