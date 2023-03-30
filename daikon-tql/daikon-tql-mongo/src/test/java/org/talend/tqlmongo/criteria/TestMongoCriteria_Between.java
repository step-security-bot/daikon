package org.talend.tqlmongo.criteria;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Criteria;

/**
 * Created by gmzoughi on 06/07/16.
 */
public class TestMongoCriteria_Between extends TestMongoCriteria_Abstract {

    @Test
    public void testParseFieldBetweenQuoted() {
        Criteria criteria = doTest("name between ['A', 'Z']");
        Criteria expectedCriteria = Criteria.where("name").gte("A").lte("Z");
        assertCriteriaEquals(expectedCriteria, criteria);
        List<Record> records = this.getRecords(criteria);
        assertEquals(3, records.size());
        assertEquals(1, records.stream().filter(r -> r.getName().equals("Benoit")).count());
        assertEquals(1, records.stream().filter(r -> r.getName().equals("Benoit 2eme")).count());
        assertEquals(1, records.stream().filter(r -> r.getName().equals("Ghassen")).count());
    }

    @Test
    public void testParseFieldBetweenInt() {
        Criteria criteria = doTest("age between [27, 29]");
        Criteria expectedCriteria = Criteria.where("age").gte(27L).lte(29L);
        assertCriteriaEquals(expectedCriteria, criteria);
        List<Record> records = this.getRecords(criteria);
        assertEquals(3, records.size());
        assertEquals(2, records.stream().filter(r -> r.getAge() == 28.8).count());
        assertEquals(1, records.stream().filter(r -> r.getAge() == 29).count());
    }

    @Test
    public void testParseFieldBetweenIntOpenLowerBound() {
        Criteria criteria = doTest("age between ]27, 29]");
        Criteria expectedCriteria = Criteria.where("age").gt(27L).lte(29L);
        assertCriteriaEquals(expectedCriteria, criteria);
        List<Record> records = this.getRecords(criteria);
        assertEquals(3, records.size());
        assertEquals(2, records.stream().filter(r -> r.getAge() == 28.8).count());
        assertEquals(1, records.stream().filter(r -> r.getAge() == 29).count());
    }

    @Test
    public void testParseFieldBetweenIntOpenUpperBound() {
        Criteria criteria = doTest("age between [27, 29[");
        Criteria expectedCriteria = Criteria.where("age").gte(27L).lt(29L);
        assertCriteriaEquals(expectedCriteria, criteria);
        List<Record> records = this.getRecords(criteria);
        assertEquals(2, records.size());
        assertEquals(2, records.stream().filter(r -> r.getAge() == 28.8).count());
    }

    @Test
    public void testParseFieldBetweenIntBothOpenBounds() {
        Criteria criteria = doTest("age between ]27, 29[");
        Criteria expectedCriteria = Criteria.where("age").gt(27L).lt(29L);
        assertCriteriaEquals(expectedCriteria, criteria);
        List<Record> records = this.getRecords(criteria);
        assertEquals(2, records.size());
        assertEquals(2, records.stream().filter(r -> r.getAge() == 28.8).count());
    }

    @Test
    public void testParseFieldBetweenDecimal() {
        Criteria criteria = doTest("age between [27.0, 29.0]");
        Criteria expectedCriteria = Criteria.where("age").gte(27.0).lte(29.0);
        assertCriteriaEquals(expectedCriteria, criteria);
        List<Record> records = this.getRecords(criteria);
        assertEquals(3, records.size());
        assertEquals(2, records.stream().filter(r -> r.getAge() == 28.8).count());
        assertEquals(1, records.stream().filter(r -> r.getAge() == 29.0).count());
    }
}
