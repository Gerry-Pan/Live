package cn.com.pan.live.config;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.HeaderWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

import com.alibaba.fastjson.JSONObject;

import cn.com.pan.live.enumerate.ErrorCode;
import cn.com.pan.live.security.MiniServerAuthenticationConverter;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFlux
@EnableWebFluxSecurity
@EnableRedisWebSession(redisNamespace = "spring:session:cn:pan:live", maxInactiveIntervalInSeconds = 1800)
@AutoConfigureAfter(value = { GlobalConfiguration.class })
public class SecurityConfiguration {

	@Value(value = "${tokenKey}")
	private String tokenKey;

	@Value(value = "${excludePaths}")
	private String[] excludePaths;

	@Autowired
	private ServerProperties serverProperties;

	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
			ReactiveAuthenticationManager authenticationManager,
			MiniServerAuthenticationConverter miniServerAuthenticationConverter, Map<Integer, String> errorMap) {
		String contextPath = serverProperties.getServlet().getContextPath();

		ServerAuthenticationEntryPoint authenticationEntryPoint = (exchange, e) -> {
			ServerHttpResponse response = exchange.getResponse();

			JSONObject result = new JSONObject();
			result.put("code", ErrorCode.CODE2);
			result.put("message", errorMap.get(ErrorCode.CODE2));

			DataBuffer wrap = response.bufferFactory().wrap(result.toString().getBytes(StandardCharsets.UTF_8));

			response.setStatusCode(HttpStatus.OK);
			return response.writeWith(Mono.just(wrap));
		};

		ServerAccessDeniedHandler accessDeniedHandler = (exchange, e) -> {
			ServerHttpResponse response = exchange.getResponse();

			JSONObject result = new JSONObject();
			result.put("code", ErrorCode.CODE3);
			result.put("message", errorMap.get(ErrorCode.CODE3));

			DataBuffer wrap = response.bufferFactory().wrap(result.toString().getBytes(StandardCharsets.UTF_8));

			return response.writeWith(Mono.just(wrap));
		};

		ServerAuthenticationSuccessHandler authenticationSuccessHandler = (webFilterExchange, e) -> {
			ServerWebExchange exchange = webFilterExchange.getExchange();
			ServerHttpResponse response = exchange.getResponse();

			return exchange.getSession().flatMap(session -> {
				JSONObject result = new JSONObject();
				result.put("code", ErrorCode.OK);
				result.put("message", errorMap.get(ErrorCode.OK));
				result.put(this.tokenKey, session.getId());

				DataBuffer wrap = response.bufferFactory().wrap(result.toString().getBytes(StandardCharsets.UTF_8));

				response.setStatusCode(HttpStatus.OK);
				return response.writeWith(Mono.just(wrap));
			});
		};

		ServerAuthenticationFailureHandler authenticationFailureHandler = (webFilterExchange, e) -> {
			ServerWebExchange exchange = webFilterExchange.getExchange();
			ServerHttpResponse response = exchange.getResponse();

			JSONObject result = new JSONObject();

			String message = "";

			if (e instanceof BadCredentialsException || e instanceof UsernameNotFoundException) {
				message = errorMap.get(ErrorCode.CODE4);
			} else {
				message = e.getMessage();
			}

			result.put("code", ErrorCode.CODE4);
			result.put("message", message);

			DataBuffer wrap = response.bufferFactory().wrap(result.toString().getBytes(StandardCharsets.UTF_8));

			response.setStatusCode(HttpStatus.OK);
			return response.writeWith(Mono.just(wrap));
		};

		ServerSecurityContextRepository securityContextRepository = new WebSessionServerSecurityContextRepository();

		AuthenticationWebFilter miniAuthenticationWebFilter = new AuthenticationWebFilter(authenticationManager);

		miniAuthenticationWebFilter.setRequiresAuthenticationMatcher(
				ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/mini/login"));
		miniAuthenticationWebFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
		miniAuthenticationWebFilter.setServerAuthenticationConverter(miniServerAuthenticationConverter);
		miniAuthenticationWebFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
		miniAuthenticationWebFilter.setSecurityContextRepository(securityContextRepository);

		http.csrf().disable().authorizeExchange().pathMatchers(excludePaths).permitAll().anyExchange().authenticated()
				.and().formLogin()
				.requiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/login"))
				.securityContextRepository(securityContextRepository)
				.authenticationSuccessHandler(authenticationSuccessHandler)
				.authenticationFailureHandler(authenticationFailureHandler)
				.authenticationEntryPoint(authenticationEntryPoint).and().exceptionHandling()
				.accessDeniedHandler(accessDeniedHandler).authenticationEntryPoint(authenticationEntryPoint).and()
				.addFilterAt((exchange, chain) -> {
					ServerHttpRequest request = exchange.getRequest();
					ServerHttpResponse response = exchange.getResponse();

					String path = request.getURI().getPath();
					if (path.startsWith(contextPath)) {
						String tokenKey = this.tokenKey;
						MultiValueMap<String, String> headers = request.getHeaders();
						MultiValueMap<String, String> params = request.getQueryParams();

						if (params.containsKey(tokenKey) && StringUtils.hasText(params.getFirst(tokenKey))
								&& !headers.containsKey(tokenKey)) {
							String token = params.getFirst(tokenKey);

							return chain.filter(exchange.mutate()
									.request(request.mutate().contextPath(contextPath).header(tokenKey, token).build())
									.build());
						}

						return chain.filter(
								exchange.mutate().request(request.mutate().contextPath(contextPath).build()).build());
					}

					response.setStatusCode(HttpStatus.NOT_FOUND);
					return Mono.empty();
				}, SecurityWebFiltersOrder.FIRST)
				.addFilterAt(miniAuthenticationWebFilter, SecurityWebFiltersOrder.FORM_LOGIN).logout()
				.logoutSuccessHandler((webFilterExchange, authentication) -> {
					ServerWebExchange exchange = webFilterExchange.getExchange();
					ServerHttpResponse response = exchange.getResponse();

					JSONObject result = new JSONObject();

					result.put("code", ErrorCode.OK);
					result.put("message", errorMap.get(ErrorCode.OK));

					DataBuffer wrap = response.bufferFactory().wrap(result.toString().getBytes(StandardCharsets.UTF_8));

					response.setStatusCode(HttpStatus.OK);
					return response.writeWith(Mono.just(wrap));
				});

		return http.build();
	}

	@Bean
	public ReactiveAuthenticationManager authenticationManager(ReactiveUserDetailsService userDetailsService) {
		UserDetailsRepositoryReactiveAuthenticationManager authenticationManager = new UserDetailsRepositoryReactiveAuthenticationManager(
				userDetailsService);
		return authenticationManager;
	}

	@Bean
	public WebSessionIdResolver webSessionIdResolver() {
		return new CustomWebSessionIdResolver(this.tokenKey);
	}

	@NoArgsConstructor
	protected static class CustomWebSessionIdResolver implements WebSessionIdResolver {

		private final CookieWebSessionIdResolver cookieWebSessionIdResolver = new CookieWebSessionIdResolver();

		private final HeaderWebSessionIdResolver headerWebSessionIdResolver = new HeaderWebSessionIdResolver();

		public CustomWebSessionIdResolver(String tokenKey) {
			headerWebSessionIdResolver.setHeaderName(tokenKey);
		}

		@Override
		public List<String> resolveSessionIds(ServerWebExchange exchange) {
			List<String> sessionIds = cookieWebSessionIdResolver.resolveSessionIds(exchange);

			if (sessionIds == null || sessionIds.size() == 0) {
				sessionIds = headerWebSessionIdResolver.resolveSessionIds(exchange);
			}

			return sessionIds;
		}

		@Override
		public void setSessionId(ServerWebExchange exchange, String sessionId) {
			cookieWebSessionIdResolver.setSessionId(exchange, sessionId);
			headerWebSessionIdResolver.setSessionId(exchange, sessionId);
		}

		@Override
		public void expireSession(ServerWebExchange exchange) {
			cookieWebSessionIdResolver.expireSession(exchange);
			headerWebSessionIdResolver.expireSession(exchange);
		}

	}

}
