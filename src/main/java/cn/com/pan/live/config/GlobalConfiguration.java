package cn.com.pan.live.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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

import com.alibaba.fastjson.JSONObject;
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

		Properties miniProperties = new Properties();
		miniProperties.load(r.getInputStream());

		return miniProperties;
	}

	/**
	 * 只有code=1时是正确码，其余全是错误码
	 * 
	 * @return
	 * @throws Exception
	 */
	@Bean(name = "errorMap")
	public Map<Integer, String> errorMap() throws Exception {
		ClassPathResource r = new ClassPathResource("error.json");
		Map<Integer, String> errorMap = new HashMap<Integer, String>();

		JSONObject o = JSONObject.parseObject(r.getInputStream(), JSONObject.class);

		Set<String> keySet = o.keySet();
		for (String key : keySet) {
			errorMap.put(Integer.parseInt(key), o.getString(key));
		}

		return errorMap;
	}

}
