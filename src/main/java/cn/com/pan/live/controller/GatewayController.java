package cn.com.pan.live.controller;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.alibaba.fastjson.JSONObject;

import reactor.core.publisher.Flux;

@RestController
public class GatewayController extends BaseController {

	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	@Qualifier(value = "bizcodeProperties")
	private Properties bizcodeProperties;

	@RequestMapping(value = "gateway", method = { RequestMethod.POST })
	public Flux<JSONObject> gateway(@RequestBody(required = false) JSONObject requestBody, ServerWebExchange exchange) {
		JSONObject result = new JSONObject();

		System.out.println(applicationContext);

		return Flux.just(result);
	}

}
