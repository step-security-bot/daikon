package org.talend.tqlmongo.criteria;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public abstract class TestMongoCriteria_Abstract {

    private final static String DB_NAME = "tql-mongo";

    private final static String COLLECTION_NAME = "record";

    private final static Map<String, Double> RECORDS = Collections.unmodifiableMap(Stream
            .of(new AbstractMap.SimpleEntry<>("ghassen", 30d), new AbstractMap.SimpleEntry<>("Ghassen", 31.2d),
                    new AbstractMap.SimpleEntry<>("Benoit", 29d), new AbstractMap.SimpleEntry<>("Benoit 2eme", 28.8d),
                    new AbstractMap.SimpleEntry<>("+?'n$", 28.8d))
            .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));

    @Container
    public static MongoDBContainer container = new MongoDBContainer(DockerImageName.parse("mongo:5"));

    public static MongoTemplate mongoTemplate;

    @BeforeAll
    public static void setUpClass() {
        container.start();

        MongoClient mongo = MongoClients.create(container.getConnectionString());
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
        container.stop();
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
