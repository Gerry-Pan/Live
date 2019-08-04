package cn.com.pan.live.repository.mini;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import cn.com.pan.live.entity.mini.MiniUser;

public interface MiniUserRepository extends ReactiveMongoRepository<MiniUser, String> {

}
