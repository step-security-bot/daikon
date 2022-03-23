package org.talend.tqlmongo.criteria;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.talend.tql.TqlLexer;
import org.talend.tql.TqlParser;
import org.talend.tql.model.TqlElement;
import org.talend.tql.parser.TqlExpressionVisitor;
import org.talend.tqlmongo.ASTVisitor;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class TestMongoCriteria_Abstract {

    private final static String DB_NAME = "tql-mongo";

    private final static String COLLECTION_NAME = "record";

    private final static Map<String, Double> RECORDS = Collections.unmodifiableMap(Stream
            .of(new AbstractMap.SimpleEntry<>("ghassen", 30d), new AbstractMap.SimpleEntry<>("Ghassen", 31.2d),
                    new AbstractMap.SimpleEntry<>("Benoit", 29d), new AbstractMap.SimpleEntry<>("Benoit 2eme", 28.8d),
                    new AbstractMap.SimpleEntry<>("+?'n$", 28.8d))
            .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));

    public static MongoTemplate mongoTemplate;

    private static MongodExecutable mongodExecutable;

    @BeforeAll
    public static void setUpClass() throws IOException {
        MongodStarter starter = MongodStarter.getDefaultInstance();

        String bindIp = "localhost";
        int port = 12345;
        MongodConfig mongodConfig = MongodConfig.builder().version(Version.Main.V3_4)
                .net(new Net(bindIp, port, Network.localhostIsIPv6())).build();
        mongodExecutable = starter.prepare(mongodConfig);
        mongodExecutable.start();

        MongoClient mongo = MongoClients.create(new ConnectionString("mongodb://" + bindIp + ":" + port));
        mongoTemplate = new MongoTemplate(mongo, DB_NAME);
    }

    @BeforeEach
    public void cleanDBAndInsertData() {
        Set<String> collectionsName = mongoTemplate.getCollectionNames();
        for (String collectionName : collectionsName) {
            mongoTemplate.remove(new Query(), collectionName);

            if (!collectionName.contains("system.indexes"))
                mongoTemplate.remove(new Query(), collectionName);
        }
        insertData();
    }

    @AfterAll
    public static void tearDown() {
        mongodExecutable.stop();
    }

    private void insertData() {
        RECORDS.forEach((name, age) -> {
            Document document = new Document();
            document.put("name", name);
            document.put("age", age);
            document.put("isGoodBoy", age % 2 == 0);
            mongoTemplate.insert(document, COLLECTION_NAME);
        });
    }

    protected void assertCriteriaEquals(Criteria criteria, Criteria expectedCriteria) {
        assertEquals(expectedCriteria.getCriteriaObject().toJson(), criteria.getCriteriaObject().toJson());
    }

    protected Criteria doTest(String query) {
        CodePointCharStream input = CharStreams.fromString(query);
        TqlLexer lexer = new TqlLexer(input);
        TqlParser parser = new TqlParser(new CommonTokenStream(lexer));
        TqlParser.ExpressionContext expression = parser.expression();
        TqlElement tqlElement = expression.accept(new TqlExpressionVisitor());
        Object accept = tqlElement.accept(new ASTVisitor());
        return (Criteria) accept;
    }

    List<Record> getRecords(Criteria criteria) {
        Query query = new Query();
        query.addCriteria(criteria);
        return mongoTemplate.find(query, Record.class);
    }

    @org.springframework.data.mongodb.core.mapping.Document(collection = COLLECTION_NAME)
    public class Record {

        private final String name;

        private final double age;

        public Record(String name, double age) {
            this.name = name;
            this.age = age;
        }

        String getName() {
            return name;
        }

        double getAge() {
            return age;
        }
    }
}
