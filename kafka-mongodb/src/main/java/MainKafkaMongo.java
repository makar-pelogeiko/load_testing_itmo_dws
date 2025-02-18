import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.bson.Document;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class MainKafkaMongo {

    public static class KafkaConfig {

        private static final String TOPIC = "customtopic";
        private static final String GROUP_ID = "files_consumer_group_docker";
        private static final String BOOTSTRAP_SERVERS = "kafka:29092";

        public static KafkaConsumer<String, String> getKafkaConsumer() {
            Properties props = new Properties();
            props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
            props.put("group.id", GROUP_ID);
            props.put("enable.auto.commit", "true");
            props.put("auto.commit.interval.ms", "1000");
            props.put("key.deserializer",
                    "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer",
                    "org.apache.kafka.common.serialization.StringDeserializer");
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
            System.out.println("Topic name: " + TOPIC);
            System.out.println("group: " + GROUP_ID);
            System.out.println("connect to: " + BOOTSTRAP_SERVERS);
            consumer.subscribe(Collections.singletonList(TOPIC));
            return consumer;
        }
    }

    public static class MongoConfig {
        public static final String MONGODB_HOST_PORT = "mongodb:27017";
        public static final String DB_NAME = "custom-file-manager";
        public static final String COLLECTION_NAME = "saved-fms-file";
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println("Start kafka to mongoDB step");
        // mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database.collection]
        try (MongoClient mongoClient = MongoClients.create("mongodb://" + MongoConfig.MONGODB_HOST_PORT);
             KafkaConsumer<String, String> consumer = KafkaConfig.getKafkaConsumer()) {
            MongoDatabase database = mongoClient.getDatabase(MongoConfig.DB_NAME);
            MongoCollection<Document> collection = database.getCollection(MongoConfig.COLLECTION_NAME);

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    String message = record.value();
                    System.out.println("message: " + message);
                    FileResponseDTO FileToSave = objectMapper.readValue(message, FileResponseDTO.class);
                    Document doc = new Document("id", FileToSave.getId())
                            .append("name", FileToSave.getName())
                            .append("data", FileToSave.getData());
                    collection.insertOne(doc);

                }
            }
        }
//                var connection = new ConnectionString("mongodb://" + MongoConfig.USERNAME + ":"
//                + MongoConfig.PASS + "@" + MongoConfig.MONGODB_HOST_PORT + "/" + MongoConfig.DB_NAME);
//        var cred = MongoCredential.createCredential(MongoConfig.USERNAME, MongoConfig.DB_NAME,
//                MongoConfig.PASS.toCharArray());
//        MongoClientSettings settings = MongoClientSettings.builder()
//                .applyConnectionString(connection)
//                .credential(cred)
//                .build();
//        MongoClient myMongoClient = MongoClients.create(settings);
    }
}
