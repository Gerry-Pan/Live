package cn.com.pan.live.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import cn.com.pan.live.entity.User;
import cn.com.pan.live.service.LiveService;
import cn.com.pan.live.util.algorithm.Md5Encoder;
import reactor.core.publisher.Mono;

@Service(value = "userDetailsService")
public class CustomReactiveUserDetailsService extends LiveService implements ReactiveUserDetailsService {

	@Override
	public Mono<UserDetails> findByUsername(String username) {
		return Mono.subscriberContext().map(context -> context.get(ServerWebExchange.class))
				.flatMap(exchange -> exchange.getSession()).flatMap(session -> {
					GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("AUTHENTICATION");
					List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
					grantedAuthorities.add(grantedAuthority);

					Integer loginType = (Integer) session.getAttribute("loginType");

					if (loginType == null) {
						User u = new User().setUsername(username);
						Example<User> example = Example.of(u);

						return reactiveMongoTemplate.findOne(new Query(Criteria.byExample(example)), User.class)
								.map(uu -> uu.setAuthorities(new HashSet<GrantedAuthority>(grantedAuthorities))
										.setEnabled(true).setAccountNonExpired(true).setAccountNonLocked(true)
										.setCredentialsNonExpired(true))
								.switchIfEmpty(Mono.error(new UsernameNotFoundException(username)));
					} else if (1 == loginType) {
						String openid = (String) session.getAttribute("currentOpenid");

						if (StringUtils.hasText(openid) && openid.equals(username)) {
							return Mono.just(new User(username, Md5Encoder.encode(username), true, true, true, true,
									grantedAuthorities));
						} else {
							return Mono.error(new UsernameNotFoundException(username));
						}
					}

					return Mono.error(new UsernameNotFoundException(username));
				});
	}

}
