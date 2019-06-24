# kafka-springboot-example


## Kafka stream configuration


- APPLICATION_ID_CONFIG: app 的名稱在 kafka cluster 要是唯一值
- BOOTSTRAP_SERVERS_CONFIG: kafka broker 的位置

```
props.put(StreamsConfig.APPLICATION_ID_CONFIG, "stream-app");
props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-broker1:9092,kafka-broker2:9092");
```

## Kafka streams builder

- 如果想要連續的 log 串流, 使用 KStream
- 想要得到某個 key 最新的值 就可以使用 KTable

```
StreamsBuilder builder = new StreamsBuilder();

KStream<String, String> textLines = builder.stream("TextLinesTopic");
KKTable<String, Long> wordCounts = textLines
            // Split each text line, by whitespace, into words.
            .flatMapValues(textLine -> Arrays.asList(textLine.toLowerCase().split("\\W+")))
            
            // Group the text words as message keys
            .groupBy((key, word) -> word)
            
            // Count the occurrences of each word (message key)
            .count();
            
// Store the running counts as a changelog stream to the output topic.
wordCounts.toStream().to("streams-wordcount-output", Produced.with(Serdes.String(), Serdes.Long()));
```

## Stream Processing Topology

- high-level  Stream DSL

- lower-level Processor API