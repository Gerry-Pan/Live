package cn.com.ecloud.live.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import com.alibaba.fastjson.JSONObject;

import cn.com.ecloud.live.entity.Car;
import cn.com.ecloud.live.entity.Person;
import reactor.core.publisher.Flux;

@Service
public class LiveService {

	protected final Logger log = LogManager.getLogger(getClass());

	@Autowired
	protected ReactiveMongoTemplate reactiveMongoTemplate;

	public Flux<JSONObject> save(ServerWebExchange exchange) {
		JSONObject result = new JSONObject();
		System.out.println(reactiveMongoTemplate);

		return reactiveMongoTemplate.inTransaction()
				.execute(action -> action.save(new Car().setName("aadddddddda")).flatMap(car -> {
					System.out.println("ddsdfsdf");
					System.out.println(action);
					return action.save(new Person().setName("000gggggggg0"));
				}).map(p -> {
					System.out.println("afffffffff");
					System.out.println(1 / 0);
					result.put("code", 1);
					result.put("message", "ok");
					return result;
				}));
	}

}
