package org.talend.daikon.logging.ecs.field;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.talend.daikon.logging.ecs.EcsFields;
import org.talend.daikon.logging.ecs.EcsFieldsMarker;

import jakarta.annotation.Nullable;

/**
 * Allows to represent a ECS field set Event.
 * ECS version: 1.8
 * 
 * @see <a href="https://www.elastic.co/guide/en/ecs/current/ecs-event.html">ECS field event</a>
 */
public final class EventFieldSet extends EcsFieldSet {

    public static Builder builder() {
        return new Builder();
    }

    private EventFieldSet(final Map<EcsFields, ?> fields) {
        super(fields);
    }

    public enum Category {

        AUTHENTICATION("authentication"),
        CONFIGURATION("configuration"),
        DATABASE("database"),
        DRIVER("driver"),
        FILE("file"),
        HOST("host"),
        IAM("iam"),
        INTRUSION_DETECTION("intrusion_detection"),
        MALWARE("malware"),
        NETWORK("network"),
        PACKAGE("package"),
        PROCESS("process"),
        REGISTRY("registry"),
        SESSION("session"),
        THREAT("threat"),
        WEB("web");

        public final String value;

        private Category(final String value) {
            this.value = value;
        }
    }

    public enum Kind {

        ALERT("alert"),
        ENRICHMENT("enrichment"),
        EVENT("event"),
        METRIC("metric"),
        STATE("state"),
        PIPELINE_ERROR("pipeline_error"),
        SIGNAL("signal");

        public final String value;

        private Kind(final String value) {
            this.value = value;
        }
    }

    public enum Outcome {

        FAILURE("failure"),
        SUCCESS("success"),
        UNKNOWN("unknown");

        public final String value;

        private Outcome(final String value) {
            this.value = value;
        }
    }

    public enum Type {

        ACCESS("access"),
        ADMIN("admin"),
        ALLOWED("allowed"),
        CHANGE("change"),
        CONNECTION("connection"),
        CREATION("creation"),
        DELETION("deletion"),
        DENIED("denied"),
        END("end"),
        ERROR("error"),
        GROUP("group"),
        INDICATOR("indicator"),
        INFO("info"),
        INSTALLATION("installation"),
        PROTOCOL("protocol"),
        START("start"),
        USER("user");

        public final String value;

        private Type(final String value) {
            this.value = value;
        }
    }

    public static final class Builder {

        private final Map<EcsFields, Object> fields;

        private Builder() {
            fields = new HashMap<>();
        }

        public Builder action(@Nullable final String action) {
            fields.put(EcsFields.EVENT_ACTION, action);
            return this;
        }

        public Builder category(@Nullable final Category category) {
            fields.put(EcsFields.EVENT_CATEGORY, category.value);
            return this;
        }

        public Builder code(@Nullable final String code) {
            fields.put(EcsFields.EVENT_CODE, code);
            return this;
        }

        public Builder created(@Nullable final Date created) {
            fields.put(EcsFields.EVENT_CREATED, created);
            return this;
        }

        public Builder dataset(@Nullable final String dataset) {
            fields.put(EcsFields.EVENT_DATASET, dataset);
            return this;
        }

        public Builder duration(@Nullable final Long duration) {
            fields.put(EcsFields.EVENT_DURATION, duration);
            return this;
        }

        public Builder end(@Nullable final Date end) {
            fields.put(EcsFields.EVENT_END, end);
            return this;
        }

        public Builder hash(@Nullable final String hash) {
            fields.put(EcsFields.EVENT_HASH, hash);
            return this;
        }

        public Builder id(@Nullable final String id) {
            fields.put(EcsFields.EVENT_ID, id);
            return this;
        }

        public Builder ingested(@Nullable final Date ingested) {
            fields.put(EcsFields.EVENT_INGESTED, ingested);
            return this;
        }

        public Builder kind(@Nullable final Kind kind) {
            fields.put(EcsFields.EVENT_KIND, kind.value);
            return this;
        }

        public Builder module(@Nullable final String module) {
            fields.put(EcsFields.EVENT_MODULE, module);
            return this;
        }

        public Builder original(@Nullable final String original) {
            fields.put(EcsFields.EVENT_ORIGINAL, original);
            return this;
        }

        public Builder outcome(@Nullable final Outcome outcome) {
            fields.put(EcsFields.EVENT_OUTCOME, outcome.value);
            return this;
        }

        public Builder provider(@Nullable final String provider) {
            fields.put(EcsFields.EVENT_PROVIDER, provider);
            return this;
        }

        public Builder reason(@Nullable final String reason) {
            fields.put(EcsFields.EVENT_REASON, reason);
            return this;
        }

        public Builder reference(@Nullable final String reference) {
            fields.put(EcsFields.EVENT_REFERENCE, reference);
            return this;
        }

        public Builder riskScore(@Nullable final Float riskScore) {
            fields.put(EcsFields.EVENT_RISK_SCORE, riskScore);
            return this;
        }

        public Builder riskScoreNorm(@Nullable final Float riskScoreNorm) {
            fields.put(EcsFields.EVENT_RISK_SCORE_NORM, riskScoreNorm);
            return this;
        }

        public Builder sequence(@Nullable final Long sequence) {
            fields.put(EcsFields.EVENT_SEQUENCE, sequence);
            return this;
        }

        public Builder severity(@Nullable final Long severity) {
            fields.put(EcsFields.EVENT_SEVERITY, severity);
            return this;
        }

        public Builder start(@Nullable final Date start) {
            fields.put(EcsFields.EVENT_START, start);
            return this;
        }

        public Builder timeZone(@Nullable final String timeZone) {
            fields.put(EcsFields.EVENT_TIMEZONE, timeZone);
            return this;
        }

        public Builder type(@Nullable final Type type) {
            fields.put(EcsFields.EVENT_TYPE, type.value);
            return this;
        }

        public Builder url(@Nullable final String url) {
            fields.put(EcsFields.EVENT_URL, url);
            return this;
        }

        public EventFieldSet build() {
            return new EventFieldSet(fields);
        }
    }

    public EcsFieldsMarker toMarker() {
        return EcsFieldsMarker.builder().event(this).build();
    }

}
