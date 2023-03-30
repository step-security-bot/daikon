package org.talend.daikon.logging.ecs.field;

import java.util.HashMap;
import java.util.Map;

import org.talend.daikon.logging.ecs.EcsFields;
import org.talend.daikon.logging.ecs.EcsFieldsMarker;

import jakarta.annotation.Nullable;

/**
 * Allows to represent a custom field ECS Labels.
 * ECS version: 1.8
 *
 * @see <a href="https://www.elastic.co/guide/en/ecs/current/ecs-custom-fields-in-ecs.html#_the_labels_field">ECS field
 * labels</a>
 */
public final class LabelsFieldSet extends EcsFieldSet {

    private LabelsFieldSet(final Map<EcsFields, ?> fields) {
        super(fields);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Map<String, String> fields;

        private Builder() {
            this.fields = new HashMap<>();
        }

        public Builder addLabel(final String key, @Nullable final String value) {
            if (key != null) {
                fields.put(String.format("%s.%s", EcsFields.LABELS.fieldName, key), value);
            }
            return this;
        }

        public Builder addLabels(final Map<String, String> labels) {
            if (labels != null) {
                labels.forEach(this::addLabel);
            }
            return this;
        }

        public LabelsFieldSet build() {
            final HashMap<EcsFields, Map<String, String>> mapLabels = new HashMap();
            mapLabels.put(EcsFields.LABELS, fields);
            return new LabelsFieldSet(mapLabels);
        }
    }

    protected EcsFieldsMarker toMarker() {
        return EcsFieldsMarker.builder().labels(this).build();
    }

}
