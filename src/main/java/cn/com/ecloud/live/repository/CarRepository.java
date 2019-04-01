package cn.com.ecloud.live.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import cn.com.ecloud.live.entity.Car;

public interface CarRepository extends ReactiveMongoRepository<Car, String> {

}
