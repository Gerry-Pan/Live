package cn.com.pan.live.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;

@Configuration
public class WebSocketConfiguration {

	@Value(value = "${webSocketHandlerPath}")
	private String webSocketHandlerPath;

	@Bean
	public UnicastProcessor<String> messageProcessor() {
		return UnicastProcessor.create();
	}

	@Bean
	public WebSocketHandlerAdapter webSocketHandlerAdapter() {
		return new WebSocketHandlerAdapter();
	}

	@Bean
	public WebSocketHandler webSocketHandler() {
		UnicastProcessor<String> messageProcessor = this.messageProcessor();
		Flux<String> messages = messageProcessor.replay(0).autoConnect();
		Flux<String> outputMessages = Flux.from(messages);

		return (session) -> {
			System.out.println(session);

			session.receive().map(WebSocketMessage::getPayloadAsText).subscribe(messageProcessor::onNext, (e) -> {
				e.printStackTrace();
			});

			return session.getHandshakeInfo().getPrincipal().flatMap((p) -> {
				session.getAttributes().put("username", p.getName());
				return session.send(outputMessages.filter((payload) -> this.filterUser(session, payload))
						.map((payload) -> this.generateMessage(session, payload)));
			}).switchIfEmpty(Mono.defer(() -> {
				return Mono.error(new BadCredentialsException("Bad Credentials."));
			})).then();
		};
	}

	@Bean
	public HandlerMapping handlerMapping(WebSocketHandler webSocketHandler) {
		Map<String, WebSocketHandler> map = new HashMap<String, WebSocketHandler>();
		map.put(webSocketHandlerPath, webSocketHandler);

		SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
		mapping.setUrlMap(map);
		mapping.setOrder(10);

		return mapping;
	}

	/**
	 * 按照payload中的指定的用户名发送给对应用户，do it in future
	 * 
	 * @param session
	 * @param payload
	 * @return
	 */
	protected boolean filterUser(WebSocketSession session, String payload) {
		System.out.println(session.getAttributes());
		System.out.println("filterUser-----" + Thread.currentThread().getName());
		return true;
	}

	protected WebSocketMessage generateMessage(WebSocketSession session, String payload) {
		return session.textMessage(payload);
	}

}
