package org.talend.daikon.kafka.example.consumer.pojo;

/**
 * Based on this schema:
 * {
 *   "$id": "https://org.talend.schema/example.schema.json",
 *   "$schema": "https://json-schema.org/draft/2020-12/schema",
 *   "title": "ExampleEvent",
 *   "type": "object",
 *   "properties": {
 *     "payload": {
 *       "type": "object",
 *       "properties": {
 *         "after": {
 *           "type": "object",
 *           "properties": {
 *             "extractDate": { "type": "string" },
 *             "apiId": { "type": "string" }
 *           },
 *           "additionalProperties": false
 *         },
 *         "filter": { "type": "null" }
 *       },
 *       "additionalProperties": false
 *     }
 *   },
 *   "additionalProperties": false
 * }
 * @param payload
 */
public record Data(Payload payload) {
}
