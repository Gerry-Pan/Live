package cn.com.pan.live.controller;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;

import cn.com.pan.live.enumerate.ErrorCode;
import cn.com.pan.live.util.exception.RestRuntimeException;
import reactor.core.publisher.Mono;

public abstract class BaseController {

	protected final Logger log = LogManager.getLogger(getClass());

	@Autowired
	private Map<Integer, String> errorMap;

	protected final Mono<JSONObject> onErrorResume(Throwable e) {
		JSONObject result = new JSONObject();
		if (e instanceof RestRuntimeException) {
			RestRuntimeException r = (RestRuntimeException) e;
			result = withoutMono(r.getCode(), r.getArgs());
		} else {
			log.error(e.getMessage(), e);

			result.put("code", ErrorCode.CODE1);
			result.put("message", errorMap.get(ErrorCode.CODE1));
		}

		return Mono.just(result);
	}

	protected Mono<JSONObject> withMono(Integer code, Object... args) {
		String message = null;
		String s = errorMap.get(code);

		if (args == null || args.length == 0) {
			message = s;
		} else {
			message = String.format(s, args);
		}

		JSONObject result = new JSONObject();
		result.put("code", code);
		result.put("message", message);
		return Mono.just(result);
	}

	protected JSONObject withoutMono(Integer code, Object... args) {
		String message = null;
		String s = errorMap.get(code);

		if (args == null || args.length == 0) {
			message = s;
		} else {
			message = String.format(s, args);
		}

		JSONObject result = new JSONObject();
		result.put("code", code);
		result.put("message", message);
		return result;
	}

}
