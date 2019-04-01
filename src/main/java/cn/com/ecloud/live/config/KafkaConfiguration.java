package cn.com.ecloud.live.config;

import java.util.Arrays;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.fastjson.JSONObject;

import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

@Configuration
public class KafkaConfiguration {

	@Value(value = "${kafka.consumer.groupId}")
	private String groupId;

	@Value(value = "${kafka.consumer.topics}")
	private String[] topics;

	@Value(value = "${kafka.bootstrapServers}")
	private String[] bootstrapServers;

	@Bean
	public KafkaReceiver<Integer, String> kafkaReceiver() {
		JSONObject consumerProperties = new JSONObject();
		consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Arrays.asList(bootstrapServers));
		consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		consumerProperties.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
		consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
		consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

		ReceiverOptions<Integer, String> receiverOptions = ReceiverOptions.<Integer, String>create(consumerProperties)
				.subscription(Arrays.asList(topics));

		KafkaReceiver<Integer, String> kafkaReceiver = KafkaReceiver.create(receiverOptions);

		return kafkaReceiver;
	}

	@Bean
	public KafkaSender<Integer, String> kafkaSender() {
		JSONObject producerProperties = new JSONObject();
		producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Arrays.asList(bootstrapServers));
		producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
		producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		producerProperties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "ecloudLiveTransactionId");

		SenderOptions<Integer, String> senderOptions = SenderOptions.create(producerProperties);
		KafkaSender<Integer, String> kafkaSender = KafkaSender.create(senderOptions);

		return kafkaSender;
	}

	@Bean(name = "kafkaProcessor")
	public UnicastProcessor<ReceiverRecord<Integer, String>> kafkaProcessor(KafkaSender<Integer, String> kafkaSender,
			KafkaReceiver<Integer, String> kafkaReceiver) {
		UnicastProcessor<ReceiverRecord<Integer, String>> kafkaProcessor = UnicastProcessor.create();

		Flux<ReceiverRecord<Integer, String>> outputMessages = Flux.from(kafkaProcessor.replay(0).autoConnect());

		kafkaReceiver.receive().subscribe(kafkaProcessor::onNext, kafkaProcessor::onError, kafkaProcessor::onComplete);

		kafkaSender.sendTransactionally(outputMessages.map(receiverRecord -> {
			System.out.println(receiverRecord);

			SenderRecord<Integer, String, ReceiverOffset> senderRecord = SenderRecord
					.create(new ProducerRecord<Integer, String>("test", 1, "111111"), receiverRecord.receiverOffset());

			return Flux.just(senderRecord);
		})).concatMap(f -> f).doOnError(e -> {
			e.printStackTrace();
		}).doOnNext(senderRecord -> {
			senderRecord.correlationMetadata().acknowledge();
		}).subscribe();

		return kafkaProcessor;
	}

}
