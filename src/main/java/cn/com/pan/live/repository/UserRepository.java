package cn.com.pan.live.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import cn.com.pan.live.entity.User;

public interface UserRepository extends ReactiveMongoRepository<User, String> {

}
