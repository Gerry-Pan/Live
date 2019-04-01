package cn.com.pan.live.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.alibaba.fastjson.JSONObject;

import reactor.core.publisher.Flux;

@RestController
public class ManageController extends BaseController {

	@RequestMapping(path = "index")
	public Flux<JSONObject> index(ServerWebExchange exchange) {

		JSONObject result = new JSONObject();

		result.put("code", 1);
		result.put("message", "index ok");

		System.out.println("index");
		System.out.println("index-----" + Thread.currentThread().getName());

		return exchange.getPrincipal().map((p) -> {
			System.out.println(p.getName());
			System.out.println("index---22222-----" + Thread.currentThread().getName());
			return result;
		}).flux();
	}
}
