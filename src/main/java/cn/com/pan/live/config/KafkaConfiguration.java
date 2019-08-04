package cn.com.pan.live.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.fastjson.JSONObject;

import cn.com.pan.kafka.annotation.ReactiveKafkaListenerAnnotationBeanPostProcessor;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

@Configuration
public class KafkaConfiguration {

	@Value(value = "${kafka.consumer.groupId}")
	private String groupId;

	@Value(value = "${kafka.bootstrapServers}")
	private String[] bootstrapServers;

	@Value(value = "${kafka.producer.transactional.id}")
	private String transactionalId;

	@Bean
	public ReactiveKafkaListenerAnnotationBeanPostProcessor reactiveKafkaListenerAnnotationBeanPostProcessor() {
	  Map<String, Object> consumerProperties = new HashMap<String, Object>();
	  consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Arrays.asList(bootstrapServers));
	  consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
	  consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
	  consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
	  consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

	  return new ReactiveKafkaListenerAnnotationBeanPostProcessor(consumerProperties);
	}

	@Bean
	public KafkaSender<Integer, String> kafkaSender() {
		JSONObject producerProperties = new JSONObject();
		producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Arrays.asList(bootstrapServers));
		producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
		producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		producerProperties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId);

		SenderOptions<Integer, String> senderOptions = SenderOptions.create(producerProperties);
		KafkaSender<Integer, String> kafkaSender = KafkaSender.create(senderOptions);

		return kafkaSender;
	}

}
