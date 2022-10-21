package me.grenadinio.easytasks;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.function.Function;

public class MongoConnect {

    public static <T> T execute(Function<MongoCollection<Document>, T> query) {
        String ip = "localhost";
        int port = 27017;
        try (MongoClient client = MongoClients.create("mongodb://" + ip + ":" + port)) {
            MongoDatabase mcserverdb = client.getDatabase("minecraftserver");
            MongoCollection<Document> collection = mcserverdb.getCollection("server1162");

            return query.apply(collection);
        }
    }
}
