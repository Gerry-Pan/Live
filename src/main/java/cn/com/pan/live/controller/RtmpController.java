package cn.com.pan.live.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

@RestController
@RequestMapping(value = "#{globalProperties['rtmpPath']}")
public class RtmpController extends BaseController {

	@GetMapping(value = "on_connect")
	public String onConnect(ServerWebExchange exchange) {
		System.out.println("on_connect");
		return "on_connect";
	}

	@GetMapping(value = "on_play")
	public String onPlay(ServerWebExchange exchange) {
		System.out.println("on_play");
		return "on_play";
	}

	@GetMapping(value = "on_publish")
	public String onPublish(ServerWebExchange exchange) {
		System.out.println(exchange.getRequest().getHeaders());
		System.out.println(exchange.getRequest().getQueryParams());

		System.out.println("on_publish");
		return "on_publish";
	}

	@GetMapping(value = "on_done")
	public String onDone(ServerWebExchange exchange) {
		System.out.println("on_done");
		return "on_done";
	}

	@GetMapping(value = "on_play_done")
	public String onPlayDone(ServerWebExchange exchange) {
		System.out.println("on_play_done");
		return "on_play_done";
	}

	@GetMapping(value = "on_publish_done")
	public String onPublishDone(ServerWebExchange exchange) {
		System.out.println("on_publish_done");
		return "on_publish_done";
	}

	@GetMapping(value = "on_record_done")
	public String onRecordDone(ServerWebExchange exchange) {
		System.out.println("on_record_done");
		return "on_record_done";
	}

	@GetMapping(value = "on_update")
	public String onUpdate(ServerWebExchange exchange) {
		System.out.println("on_update");
		return "on_update";
	}

}
