package cn.com.pan.live.service.mini;

import java.time.Duration;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import com.alibaba.fastjson.JSONObject;

import cn.com.pan.live.entity.mini.Mini;
import cn.com.pan.live.entity.mini.MiniUserSessionKey;
import cn.com.pan.live.service.LiveService;
import reactor.core.publisher.Mono;

@Service
public class MiniUserService extends LiveService {

	public Mono<JSONObject> signTicket(JSONObject requestBody, ServerWebExchange exchange) {
		JSONObject result = new JSONObject();

		String ticket = requestBody.getString("ticket");

		if (!StringUtils.hasText(ticket)) {
			result.put("code", 0);
			result.put("message", "ticket must not be null or blank.");
			return Mono.just(result);
		}

		return exchange.getSession().map(session -> (String) session.getAttribute("currentOpenid")).flatMap(openid -> {
			return reactiveRedisTemplate.opsForValue().set(ticket, openid, Duration.ofSeconds(600));
		}).map(r -> {
			if (r) {
				result.put("code", 1);
				result.put("message", "ok");
			} else {
				result.put("code", 0);
				result.put("message", "系统繁忙，请稍候重试！");
			}
			return result;
		});
	}

	public Mono<JSONObject> getPhoneNumber(JSONObject requestBody, ServerWebExchange exchange) {
		JSONObject result = new JSONObject();

		String iv = requestBody.getString("iv");
		String encryptedData = requestBody.getString("encryptedData");

		return exchange.getSession().map(session -> (String) session.getAttribute("currentOpenid"))
				.flatMap(openid -> reactiveMongoTemplate.findById(openid, MiniUserSessionKey.class))
				.map(MiniUserSessionKey::getSessionKey).map(sessionkey -> decrypt(encryptedData, iv, sessionkey))
				.map(r -> JSONObject.parseObject(r)).map(responseBody -> responseBody.getString("purePhoneNumber"))
				.map(purePhoneNumber -> {
					result.put("code", 1);
					result.put("message", "ok");
					result.put("purePhoneNumber", purePhoneNumber);
					return result;
				});

	}

	public Mono<JSONObject> getwxacodeunlimit(JSONObject requestBody, ServerWebExchange exchange) {
		JSONObject result = new JSONObject();

		String appid = requestBody.getString("appid");
		String ticket = UUID.randomUUID().toString().replaceAll("-", "");

		requestBody.put("scene", ticket);
		requestBody.put("page", "pages/qrcode/login");

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);

		return reactiveMongoTemplate.findById(appid, Mini.class).map(mini -> getMiniInvokeHandler(mini))
				.flatMap(miniInvokeHandler -> miniInvokeHandler.httpHandler("wxa_getwxacodeunlimit", "miniprogram",
						new JSONObject(), requestHeaders, requestBody, JSONObject.class, byte[].class))
				.map(responseEntity -> {
					HttpHeaders responseHeaders = responseEntity.getHeaders();
					byte[] responseBody = responseEntity.getBody();

					if (responseHeaders.getContentType().equals(MediaType.APPLICATION_JSON_UTF8)
							|| responseHeaders.getContentType().equals(MediaType.APPLICATION_JSON)) {
						log.info(JSONObject.parse(new String(responseBody)));

						result.put("code", 0);
						result.put("message", "系统繁忙");
					} else {
						String img = Base64.encodeBase64String(responseBody);

						result.put("code", 1);
						result.put("message", "ok");
						result.put("ticket", ticket);
						result.put("img", img);
					}

					return result;
				}).switchIfEmpty(Mono.defer(() -> {
					result.put("code", 0);
					result.put("message", "invalid requset.");
					return Mono.just(result);
				})).onErrorResume(e -> {
					log.error(e.getMessage(), e);
					e.printStackTrace();
					result.put("code", -1);
					result.put("message", "Internal Server error");
					return null;
				});
	}

}
