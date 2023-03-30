package org.talend.tqlmongo.criteria;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Criteria;

/**
 * Created by gmzoughi on 06/07/16.
 */
public class TestMongoCriteria_Contain extends TestMongoCriteria_Abstract {

    @Test
    public void testParseFieldContainsValue1() {
        Criteria criteria = doTest("name contains 'ssen'");
        Criteria expectedCriteria = Criteria.where("name").regex("ssen");
        assertCriteriaEquals(expectedCriteria, criteria);
        List<Record> records = this.getRecords(criteria);
        assertEquals(2, records.size());
        assertEquals(1, records.stream().filter(r -> r.getName().equals("ghassen")).count());
        assertEquals(1, records.stream().filter(r -> r.getName().equals("Ghassen")).count());
    }

    @Test
    public void testParseFieldContainsValue2() {
        Criteria criteria = doTest("name contains 'noi'");
        Criteria expectedCriteria = Criteria.where("name").regex("noi");
        assertCriteriaEquals(expectedCriteria, criteria);
        List<Record> records = this.getRecords(criteria);
        assertEquals(2, records.size());
        assertEquals(1, records.stream().filter(r -> r.getName().equals("Benoit")).count());
        assertEquals(1, records.stream().filter(r -> r.getName().equals("Benoit 2eme")).count());
    }

    @Test
    public void testParseFieldContainsValue3() {
        Criteria criteria = doTest("name contains '2'");
        Criteria expectedCriteria = Criteria.where("name").regex("2");
        assertCriteriaEquals(expectedCriteria, criteria);
        List<Record> records = this.getRecords(criteria);
        assertEquals(1, records.size());
        assertEquals(1, records.stream().filter(r -> r.getName().equals("Benoit 2eme")).count());
    }

    @Test
    public void testParseFieldContainsValue4() {
        Criteria criteria = doTest("name contains 'azerty'");
        Criteria expectedCriteria = Criteria.where("name").regex("azerty");
        assertCriteriaEquals(expectedCriteria, criteria);
        List<Record> records = this.getRecords(criteria);
        assertEquals(0, records.size());
    }

    @Test
    public void testParseFieldContainsValue5() {
        Criteria criteria = doTest("name contains ''");
        Criteria expectedCriteria = Criteria.where("name").regex("");
        assertCriteriaEquals(expectedCriteria, criteria);
        List<Record> records = this.getRecords(criteria);
        assertEquals(5, records.size());
    }

    @Test
    public void testParseFieldContainsValue6() {
        Criteria criteria = doTest("name contains 'gha'");
        Criteria expectedCriteria = Criteria.where("name").regex("gha");
        assertCriteriaEquals(expectedCriteria, criteria);
        List<Record> records = this.getRecords(criteria);
        assertEquals(1, records.size());
        assertEquals(1, records.stream().filter(r -> r.getName().equals("ghassen")).count());
    }

    @Test
    public void testParseFieldContainsValue7() {
        Criteria criteria = doTest("name contains 'Gha'");
        Criteria expectedCriteria = Criteria.where("name").regex("Gha");
        assertCriteriaEquals(expectedCriteria, criteria);
        List<Record> records = this.getRecords(criteria);
        assertEquals(1, records.size());
        assertEquals(1, records.stream().filter(r -> r.getName().equals("Ghassen")).count());
    }

    @Test
    public void testParseFieldContainsValue8() {
        Criteria criteria = doTest("name contains '+'");
        Criteria expectedCriteria = Criteria.where("name").regex("\\+");
        assertCriteriaEquals(expectedCriteria, criteria);
        List<Record> records = this.getRecords(criteria);
        assertEquals(1, records.size());
        assertEquals(1, records.stream().filter(r -> r.getName().equals("+?'n$")).count());
    }

    @Test
    public void testParseFieldContainsValue9() {
        Criteria criteria = doTest("name contains '?'");
        Criteria expectedCriteria = Criteria.where("name").regex("\\?");
        assertCriteriaEquals(expectedCriteria, criteria);
        List<Record> records = this.getRecords(criteria);
        assertEquals(1, records.size());
        assertEquals(1, records.stream().filter(r -> r.getName().equals("+?'n$")).count());
    }

    @Test
    public void testParseFieldContainsValue10() {
        Criteria criteria = doTest("name contains '$'");
        Criteria expectedCriteria = Criteria.where("name").regex("\\$");
        assertCriteriaEquals(expectedCriteria, criteria);
        List<Record> records = this.getRecords(criteria);
        assertEquals(1, records.size());
        assertEquals(1, records.stream().filter(r -> r.getName().equals("+?'n$")).count());
    }

    @Test
    public void testParseFieldContainsValue11() {
        Criteria criteria = doTest("name contains '\\''");
        Criteria expectedCriteria = Criteria.where("name").regex("'");
        assertCriteriaEquals(expectedCriteria, criteria);
        List<Record> records = this.getRecords(criteria);
        assertEquals(1, records.size());
        assertEquals(1, records.stream().filter(r -> r.getName().equals("+?'n$")).count());
    }

    // ANTLR uses simple quotes as a delimiter, so if the string contains a simple quote, it should be escaped
    // otherwise all what comes after the first simple quote is ignored
    @Test
    public void testParseFieldContainsValueCheckSimpleQuoteShouldBeEscaped() {
        Criteria criteria = doTest("name contains '''"); // equals a search on an empty string
        List<Record> records = this.getRecords(criteria);
        assertEquals(5, records.size()); // returns all records

        criteria = doTest("name contains 'ghassen''"); // equals a search on 'ghassen'
        records = this.getRecords(criteria);
        assertEquals(1, records.size()); // returns only 'ghassen' record
        assertEquals(1, records.stream().filter(r -> r.getName().equals("ghassen")).count());

        criteria = doTest("name contains 'ghassen'xxxxxxxxxxxxxxxxxxxx"); // equals a search on on 'ghassen'
        records = this.getRecords(criteria);
        assertEquals(1, records.size()); // returns only 'ghassen' record
        assertEquals(1, records.stream().filter(r -> r.getName().equals("ghassen")).count());
    }

}
