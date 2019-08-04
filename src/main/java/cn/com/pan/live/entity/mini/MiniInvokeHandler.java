package cn.com.pan.live.entity.mini;

import java.util.Date;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import com.alibaba.fastjson.JSONObject;

import lombok.Setter;
import reactor.core.publisher.Mono;

public class MiniInvokeHandler {

	protected final Logger logger = LogManager.getLogger(getClass());

	@Setter
	protected Mini mini;

	protected final Long timeout;

	protected final WebClient webClient;

	protected final Properties miniProperties;

	protected final ReactiveMongoTemplate reactiveMongoTemplate;

	protected final ReactiveRedisTemplate<Object, Object> reactiveRedisTemplate;

	public MiniInvokeHandler(Mini mini, Properties miniProperties, WebClient webClient,
			ReactiveRedisTemplate<Object, Object> reactiveRedisTemplate, ReactiveMongoTemplate reactiveMongoTemplate) {
		this.mini = mini;
		this.timeout = 7100L;
		this.webClient = webClient;
		this.miniProperties = miniProperties;
		this.reactiveRedisTemplate = reactiveRedisTemplate;
		this.reactiveMongoTemplate = reactiveMongoTemplate;
	}

	public Mono<String> accessToken() {
		String appid = mini.getId();

		return reactiveMongoTemplate.findById(appid, MiniAccessToken.class).map(MiniAccessToken::getAccessToken)
				.switchIfEmpty(accessTokenOnLock());
	}

	public <F> Mono<F> jscode2session(String jscode, Class<F> responseBodyClass) {
		String appid = mini.getId();
		String secret = mini.getSecret();

		String url = "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={jscode}&grant_type=authorization_code";

		return webClient.get().uri(url, appid, secret, jscode).retrieve().bodyToMono(responseBodyClass);
	}

	protected Mono<String> accessTokenOnLock() {
		String appid = mini.getId();
		String secret = mini.getSecret();
		String accessTokenLockKey = "lock_" + "miniprogram_" + appid + "_access_token";
		String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={appid}&secret={secret}";

		return reactiveRedisTemplate.opsForValue().setIfAbsent(accessTokenLockKey, "locked").flatMap(r -> {
			if (r) {
				return reactiveMongoTemplate.findById(appid, MiniAccessToken.class)
						.switchIfEmpty(webClient.get().uri(url, appid, secret).retrieve().bodyToMono(JSONObject.class)
								.map(responseBody -> responseBody.getString("access_token"))
								.flatMap(accessToken -> reactiveMongoTemplate.save(new MiniAccessToken().setId(appid)
										.setAccessToken(accessToken).setCreatedAt(new Date()))))
						.map(MiniAccessToken::getAccessToken).onErrorResume(e -> {
							logger.error(e.getMessage(), e);
							return Mono.error(e);
						}).doFinally(st -> {
							reactiveRedisTemplate.opsForValue().delete(accessTokenLockKey).subscribe(d -> {
								logger.info("Delete " + accessTokenLockKey + "---" + d);
							});
						});
			} else {
				return reactiveMongoTemplate.findById(appid, MiniAccessToken.class)
						.map(MiniAccessToken::getAccessToken);
			}
		});
	}

	public <Q, F> Mono<ResponseEntity<F>> httpHandler(String invoke, String tokenType, JSONObject urlParams,
			HttpHeaders requestHeaders, Q requestBody, Class<Q> requestBodyClass, Class<F> responseBodyClass) {

		String s = miniProperties.getProperty(invoke);
		MiniInvoke miniInvoke = JSONObject.parseObject(s, MiniInvoke.class);

		assert miniInvoke != null : ("miniInvoke '" + invoke + "' not exist");

		String url = miniInvoke.getUrl();
		HttpMethod httpMethod = miniInvoke.getMethod();

		return accessToken().flatMap(accessToken -> {
			urlParams.put("access_token", accessToken);
			return webClient.method(httpMethod).uri(url, urlParams).headers(headers -> headers = requestHeaders)
					.body(Mono.just(requestBody), requestBodyClass).exchange()
					.flatMap(clientResponse -> clientResponse.toEntity(responseBodyClass));
		});
	}

}
