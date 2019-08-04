package cn.com.pan.live.config;

import java.util.List;
import java.util.Properties;

import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientCodecCustomizer;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@ComponentScan(basePackages = { "cn.com.pan" })
public class GlobalConfiguration {

	@Bean
	public WebClient webClient(WebClient.Builder webClientBuilder) {
		return webClientBuilder.build();
	}

	@Bean
	@Order(-1)
	@Primary
	public WebClientCodecCustomizer exchangeStrategiesCustomizer(List<CodecCustomizer> codecCustomizers) {
		return new WebClientCodecCustomizer(codecCustomizers);
	}

	@Bean
	@Order(1)
	@Primary
	public CodecCustomizer ecloudJacksonCodecCustomizer(ObjectMapper objectMapper) {
		return (configurer) -> {
			CodecConfigurer.DefaultCodecs defaults = configurer.defaultCodecs();
			defaults.jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, MediaType.ALL));
			defaults.jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.ALL));
		};
	}

	@Bean(name = "miniProperties")
	public Properties miniProperties() throws Exception {
		ClassPathResource r = new ClassPathResource("mini.properties");

		Properties bizcodeProperties = new Properties();
		bizcodeProperties.load(r.getInputStream());

		return bizcodeProperties;
	}

}
