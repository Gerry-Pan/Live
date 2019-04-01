package cn.com.pan.live.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EntityScan(basePackages = { "cn.com.pan.live.entity" })
@EnableReactiveMongoRepositories(basePackages = { "cn.com.pan.live.repository" })
public class MongoConfiguration {

}