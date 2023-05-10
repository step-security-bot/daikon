package org.talend.daikon.cloudevent;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

import io.cloudevents.CloudEventExtension;
import io.cloudevents.CloudEventExtensions;
import io.cloudevents.core.extensions.impl.ExtensionUtils;

/**
 * This extension embeds context from Talend so that distributed systems can include traces that span an event-driven system.
 *
 * @see <a href=
 * "https://github.com/Talend/policies/blob/master/official/kafka-application/README_event_specification.adoc#talend-cloudevents-extension">https://github.com/Talend/policies/blob/master/official/kafka-application/README_event_specification.adoc#talend-cloudevents-extension</a>
 */
public class TalendCloudEventExtension implements CloudEventExtension {

    /**
     * The key of the {@code tenantid} extension
     */
    public static final String TENANTID = "tenantid";

    /**
     * The key of the {@code correlationid} extension
     */
    public static final String CORRELATIONID = "correlationid";

    private static final Set<String> KEY_SET = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(TENANTID, CORRELATIONID)));

    private String tenantid;

    private String correlationid;

    @Override
    public void readFrom(CloudEventExtensions cloudEventExtensions) {
        Object tp = cloudEventExtensions.getExtension(TENANTID);
        if (tp != null) {
            this.tenantid = tp.toString();
        }
        Object ts = cloudEventExtensions.getExtension(CORRELATIONID);
        if (ts != null) {
            this.correlationid = ts.toString();
        }
    }

    @Override
    public Object getValue(String key) throws IllegalArgumentException {
        switch (key) {
        case TENANTID:
            return this.tenantid;
        case CORRELATIONID:
            return this.correlationid;
        }
        throw ExtensionUtils.generateInvalidKeyException(this.getClass(), key);
    }

    @Override
    public Set<String> getKeys() {
        return KEY_SET;
    }

    public String getTenantid() {
        return tenantid;
    }

    public void setTenantid(String tenantid) {
        this.tenantid = tenantid;
    }

    public String getCorrelationid() {
        return correlationid;
    }

    public void setCorrelationid(String correlationid) {
        this.correlationid = correlationid;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TalendCloudEventExtension.class.getSimpleName() + "[", "]").add("tenantid=" + tenantid)
                .add("correlationid='" + correlationid + "'").toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        org.talend.daikon.cloudevent.TalendCloudEventExtension that = (org.talend.daikon.cloudevent.TalendCloudEventExtension) o;
        return Objects.equals(getCorrelationid(), that.getCorrelationid()) && Objects.equals(getTenantid(), that.getTenantid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTenantid(), getCorrelationid());
    }
}
