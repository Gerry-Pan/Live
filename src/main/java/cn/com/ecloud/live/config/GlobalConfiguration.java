package cn.com.ecloud.live.config;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(basePackages = { "cn.com.ecloud" })
public class GlobalConfiguration {

	@Bean(name = "restTemplate")
	public RestTemplate restTemplate(@Qualifier("httpMessageConverters") HttpMessageConverters httpMessageConverters) {
		RestTemplate restTemplate = new RestTemplate();

		SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
		simpleClientHttpRequestFactory.setConnectTimeout(30000);
		simpleClientHttpRequestFactory.setReadTimeout(60000);

		restTemplate.setRequestFactory(simpleClientHttpRequestFactory);
		restTemplate.setMessageConverters(httpMessageConverters.getConverters());

		return restTemplate;
	}

	@Bean(name = "mappingJackson2HttpMessageConverter")
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
		MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();

		List<MediaType> supportedMediaTypes = new ArrayList<MediaType>();
		supportedMediaTypes.add(MediaType.ALL);

		mappingJackson2HttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes);

		return mappingJackson2HttpMessageConverter;
	}

	@Bean(name = "byteArrayHttpMessageConverter")
	public ByteArrayHttpMessageConverter byteArrayHttpMessageConverter() {
		ByteArrayHttpMessageConverter byteArrayHttpMessageConverter = new ByteArrayHttpMessageConverter();

		return byteArrayHttpMessageConverter;
	}

	@Bean(name = "stringHttpMessageConverter")
	public StringHttpMessageConverter stringHttpMessageConverter() {
		StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter(
				Charset.forName("UTF-8"));

		return stringHttpMessageConverter;
	}

	@Bean(name = "formHttpMessageConverter")
	public FormHttpMessageConverter formHttpMessageConverter() {
		FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();

		return formHttpMessageConverter;
	}

	@Bean(name = "jaxb2RootElementHttpMessageConverter")
	public Jaxb2RootElementHttpMessageConverter jaxb2RootElementHttpMessageConverter() {
		Jaxb2RootElementHttpMessageConverter jaxb2RootElementHttpMessageConverter = new Jaxb2RootElementHttpMessageConverter();

		List<MediaType> supportedMediaTypes = new ArrayList<MediaType>();
		supportedMediaTypes.add(MediaType.ALL);

		jaxb2RootElementHttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes);

		return jaxb2RootElementHttpMessageConverter;
	}

	@Bean(name = "httpMessageConverters")
	public HttpMessageConverters httpMessageConverters(
			@Qualifier("formHttpMessageConverter") HttpMessageConverter<?> formHttpMessageConverter,
			@Qualifier("mappingJackson2HttpMessageConverter") HttpMessageConverter<?> mappingJackson2HttpMessageConverter,
			@Qualifier("byteArrayHttpMessageConverter") HttpMessageConverter<?> byteArrayHttpMessageConverter,
			@Qualifier("stringHttpMessageConverter") HttpMessageConverter<?> stringHttpMessageConverter,
			@Qualifier("jaxb2RootElementHttpMessageConverter") HttpMessageConverter<?> jaxb2RootElementHttpMessageConverter) {
		LinkedList<HttpMessageConverter<?>> messageConverters = new LinkedList<HttpMessageConverter<?>>();

		messageConverters.add(formHttpMessageConverter);
		messageConverters.add(mappingJackson2HttpMessageConverter);
		messageConverters.add(byteArrayHttpMessageConverter);
		messageConverters.add(stringHttpMessageConverter);
		messageConverters.add(jaxb2RootElementHttpMessageConverter);

		HttpMessageConverters httpMessageConverters = new HttpMessageConverters(messageConverters);

		return httpMessageConverters;
	}

	@Bean(name = "bizcodeProperties")
	public Properties bizcodeProperties() throws Exception {
		ClassPathResource r = new ClassPathResource("bizcode.properties");

		Properties bizcodeProperties = new Properties();
		bizcodeProperties.load(r.getInputStream());

		return bizcodeProperties;
	}

}
