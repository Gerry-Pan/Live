package cn.com.pan.live.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.alibaba.fastjson.JSONObject;

import reactor.core.publisher.Mono;

@RestController
public class ManageController extends BaseController {

	@RequestMapping(path = "index")
	public Mono<JSONObject> index(ServerWebExchange exchange) {
		JSONObject result = new JSONObject();

		return exchange.getSession().map(session -> {
			String openid = session.getAttribute("currentOpenid");

			result.put("code", 1);
			result.put("message", "index ok");
			result.put("openid", openid);

			return result;
		});
	}
	
}
