package cn.com.pan.live.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;

import com.alibaba.fastjson.JSONObject;

import cn.com.pan.live.entity.mini.Mini;
import cn.com.pan.live.entity.mini.MiniInvokeHandler;
import cn.com.pan.live.entity.mini.MiniUser;
import cn.com.pan.live.entity.mini.MiniUserSessionKey;
import cn.com.pan.live.service.mini.MiniUserService;
import cn.com.pan.live.util.algorithm.Md5Encoder;
import reactor.core.publisher.Mono;

@Component
public class MiniServerAuthenticationConverter implements ServerAuthenticationConverter {

	protected final Logger logger = LogManager.getLogger(getClass());

	@Autowired
	private MiniUserService miniUserService;
	@Autowired
	protected ReactiveMongoTemplate reactiveMongoTemplate;
	@Autowired
	protected ReactiveRedisTemplate<Object, Object> reactiveRedisTemplate;

	@Override
	public Mono<Authentication> convert(ServerWebExchange exchange) {
		ServerHttpRequest request = exchange.getRequest();

		String referer = request.getHeaders().getFirst("referer");
		System.out.println(referer);

		if (StringUtils.hasText(referer) && referer.startsWith("https://servicewechat.com/")) {
			String reverse = new StringBuilder(referer).reverse().toString();

			int i = reverse.indexOf("/");
			int j = reverse.indexOf("/", i + 1);
			int k = reverse.indexOf("/", j + 1);
			String s = reverse.substring(j + 1, k);

			String appid = new StringBuilder(s).reverse().toString();

			logger.info(appid);

			return exchange.getSession().flatMap(session -> invokeHandler(appid, session, exchange))
					.onErrorResume(e -> {
						logger.error(e.getMessage(), e);
						return Mono.error(new AuthenticationServiceException("Internal Server error"));
					}).switchIfEmpty(Mono.error(new AuthenticationServiceException("unknow invalid")))
					.map(openid -> new UsernamePasswordAuthenticationToken(openid, Md5Encoder.encode(openid)));
		} else {
			MultiValueMap<String, String> params = request.getQueryParams();
			String ticket = params.getFirst("ticket");
			String appid = params.getFirst("appid");

			if (!StringUtils.hasText(ticket)) {
				return Mono.error(new AuthenticationServiceException("unknow invalid"));
			}

			if (!StringUtils.hasText(appid)) {
				return Mono.error(new AuthenticationServiceException("unknow invalid"));
			}

			return reactiveRedisTemplate.opsForValue().get(ticket).cast(String.class).flatMap(openid -> {
				return exchange.getSession().map(session -> {
					session.getAttributes().put("loginType", 1);
					session.getAttributes().put("currentAppid", appid);
					session.getAttributes().put("currentOpenid", openid);
					return openid;
				});
			}).onErrorResume(e -> {
				logger.error(e.getMessage(), e);
				return Mono.error(new AuthenticationServiceException("Internal Server error"));
			}).switchIfEmpty(Mono.error(new AuthenticationServiceException("unknow invalid")))
					.map(openid -> new UsernamePasswordAuthenticationToken(openid, Md5Encoder.encode(openid)));
		}
	}

	public Mono<String> invokeHandler(String appid, WebSession session, ServerWebExchange exchange) {
		String openid = (String) session.getAttribute("currentOpenid");

		if (!StringUtils.hasText(openid)) {
			return reactiveMongoTemplate.findById(appid, Mini.class).flatMap(mini -> {
				MiniInvokeHandler miniInvokeHandler = miniUserService.getMiniInvokeHandler(mini);

				return exchange.getFormData().map(m -> m.getFirst("code"))
						.flatMap(jscode -> miniInvokeHandler.jscode2session(jscode, JSONObject.class));
			}).flatMap(responseBody -> {
				logger.info("responseBody---" + responseBody);

				if (responseBody.containsKey("openid")) {
					String _openid = responseBody.getString("openid");
					String sessionkey = responseBody.getString("session_key");
					session.getAttributes().put("loginType", 1);
					session.getAttributes().put("currentAppid", appid);
					session.getAttributes().put("currentOpenid", _openid);
					session.getAttributes().put("sessionkey", sessionkey);

					return Mono.zip(
							reactiveMongoTemplate.findById(_openid, MiniUser.class)
									.switchIfEmpty(reactiveMongoTemplate
											.save(new MiniUser().setId(_openid).setMini(new Mini().setId(appid))))
									.map(MiniUser::getId),
							reactiveMongoTemplate.findById(_openid, MiniUserSessionKey.class)
									.switchIfEmpty(Mono.just(new MiniUserSessionKey().setId(_openid)))
									.map(entity -> entity.setSessionKey(sessionkey))
									.flatMap(entity -> reactiveMongoTemplate.save(entity))
									.map(MiniUserSessionKey::getId))
							.map(tuple2 -> {
								return tuple2.getT2();
							}).onErrorResume(e -> {
								logger.error(e.getMessage(), e);
								return Mono.error(new AuthenticationServiceException("Internal Server error"));
							});
				} else {
					String errmsg = responseBody.getString("errmsg");
					return Mono.error(new AuthenticationServiceException(errmsg));
				}
			}).onErrorResume(e -> {
				logger.error(e.getMessage(), e);
				return Mono.error(new AuthenticationServiceException("Internal Server error"));
			}).switchIfEmpty(Mono.error(new AuthenticationServiceException("unknow invalid")));
		}

		return Mono.just(openid);
	}

}
