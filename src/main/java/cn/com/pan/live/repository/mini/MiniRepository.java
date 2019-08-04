package cn.com.pan.live.repository.mini;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import cn.com.pan.live.entity.mini.Mini;

public interface MiniRepository extends ReactiveMongoRepository<Mini, String> {

}
