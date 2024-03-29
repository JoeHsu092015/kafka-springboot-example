package com.example.kafka.StreamStartApp;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.Stores;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class KafkaStreamApp {

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamApp.class, args);

		// step 1: properties
		Properties config = new Properties();
		config.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-starter-app");
		config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.1.202:9092");
		config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
		config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

		// setting offset reset to earliest so that we can re-run the demo code with the same pre-loaded data
		config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");


		final Topology builder = new Topology();



		builder.addSource("Source", "streams-plaintext-input");

		builder.addProcessor("Process", new WordProcessor(), "Source");
		builder.addStateStore(Stores.keyValueStoreBuilder(
				Stores.inMemoryKeyValueStore("Counts"),
				Serdes.String(),
				Serdes.Integer()),
				"Process");

		// send to output topic
		builder.addSink("Sink", "streams-wordcount-output", "Process");

		final KafkaStreams   streams = new KafkaStreams(builder, config);
		final CountDownLatch latch   = new CountDownLatch(1);

		// attach shutdown handler to catch control-c
		Runtime.getRuntime().addShutdownHook(new Thread("streams-wordcount-shutdown-hook") {
			@Override
			public void run() {
				streams.close();
				latch.countDown();
			}
		});

		try {
			streams.start();
			latch.await();
		} catch (final Throwable e) {
			System.exit(1);
		}
		System.exit(0);
	}
}
