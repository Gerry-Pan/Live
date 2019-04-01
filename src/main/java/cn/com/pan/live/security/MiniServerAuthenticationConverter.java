package cn.com.pan.live.security;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;

import com.alibaba.fastjson.JSONObject;

import reactor.core.publisher.Mono;

@Component
public class MiniServerAuthenticationConverter implements ServerAuthenticationConverter {

	protected final Logger logger = LogManager.getLogger(getClass());

	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private ReactiveRedisTemplate<Object, Object> reactiveRedisTemplate;

	@Override
	public Mono<Authentication> convert(ServerWebExchange exchange) {
		ServerHttpRequest request = exchange.getRequest();

		String referer = request.getHeaders().getFirst("referer");
		System.out.println(referer);

		if (StringUtils.hasText(referer) && referer.startsWith("https://servicewechat.com/")) {
			MultiValueMap<String, String> params = request.getQueryParams();

			String reverse = new StringBuilder(referer).reverse().toString();

			int i = reverse.indexOf("/");
			int j = reverse.indexOf("/", i + 1);
			int k = reverse.indexOf("/", j + 1);
			String s = reverse.substring(j + 1, k);

			String appid = new StringBuilder(s).reverse().toString();

			logger.info(appid);

			return exchange.getSession().flatMap(session -> invokeHandler(appid, session, params))
					.flatMap(openid -> Mono.just(new UsernamePasswordAuthenticationToken(openid, openid)));

		}

		return Mono.empty();
	}

	public Mono<String> invokeHandler(String appid, WebSession session, MultiValueMap<String, String> params) {

		String sessionkey = "";
		String openid = (String) session.getAttribute("currentOpenid");

		if (!StringUtils.hasText(openid)) {
			String jscode = params.getFirst("code");
			JSONObject responseBody = jscode2session("", "", jscode, JSONObject.class);// 待修改---------------------------

			if (responseBody.containsKey("openid")) {
				openid = responseBody.getString("openid");
				sessionkey = responseBody.getString("session_key");

				session.getAttributes().put("loginType", 1);
				session.getAttributes().put("currentAppid", appid);
				session.getAttributes().put("currentOpenid", openid);
				session.getAttributes().put("sessionkey", sessionkey);

				return reactiveRedisTemplate.opsForValue().set("sessionkey of " + openid, sessionkey).map(r -> {
					return responseBody.getString("openid");
				});
			} else {
				String errmsg = responseBody.getString("errmsg");
				return Mono.error(new AuthenticationServiceException(errmsg));
			}
		}

		return Mono.just(openid);
	}

	public <F> F jscode2session(String appid, String secret, String jscode, Class<F> responseBodyClass) {

		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appid + "&secret=" + secret + "&js_code="
				+ jscode + "&grant_type=authorization_code";

		logger.info("url---" + url);

		HttpEntity<byte[]> requestEntity = new HttpEntity<byte[]>(new byte[0]);

		ResponseEntity<F> responseEntity = exchange(url, HttpMethod.GET, requestEntity, responseBodyClass);
		F responseBody = responseEntity.getBody();
		logger.info("responseBody-1--" + responseBody);

		return responseBody;
	}

	protected <T, E> ResponseEntity<E> exchange(String url, HttpMethod httpMethod, HttpEntity<T> requestEntity,
			Class<E> responseType) {
		ResponseEntity<E> responseEntity = restTemplate.exchange(URI.create(url), httpMethod, requestEntity,
				responseType);
		return responseEntity;
	}

}
