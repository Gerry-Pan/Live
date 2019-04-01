package cn.com.pan.live.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import com.alibaba.fastjson.JSONObject;

import reactor.core.publisher.Flux;

@Service
public class LiveService {

	protected final Logger log = LogManager.getLogger(getClass());

	@Autowired
	protected ReactiveMongoTemplate reactiveMongoTemplate;

	public Flux<JSONObject> save(ServerWebExchange exchange) {
		JSONObject result = new JSONObject();
		System.out.println(reactiveMongoTemplate);

		return Flux.just(result);
	}

}
