package org.talend.daikon.logging.ecs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Marker;
import org.talend.daikon.logging.ecs.field.EcsFieldSet;
import org.talend.daikon.logging.ecs.field.EventFieldSet;
import org.talend.daikon.logging.ecs.field.LabelsFieldSet;

import lombok.Getter;

/**
 * Allows wrapping ECS fields for logging.
 *
 * @see <a href="https://www.elastic.co/guide/en/ecs/current/ecs-field-reference.html">ECS fields</a>
 *
 * This class implements Marker interface in order to follow the SL4J interface.
 * The property fields represents all the ECS fields which should be added to the log.
 * @see EcsFieldSet
 */
@Getter
public class EcsFieldsMarker implements Marker {

    public static final String ECS_FIELDS_MARKER_NAME = "ecs-fields-marker";

    private final List<Marker> referenceList = new CopyOnWriteArrayList<Marker>();

    private final Map<EcsField, EcsFieldSet> ecsFieldSets;

    private EcsFieldsMarker(final Map<EcsField, EcsFieldSet> ecsFieldSets) {
        this.ecsFieldSets = ecsFieldSets;
    }

    public static Builder builder() {
        return new Builder();
    }

    private enum EcsField {
        EVENT,
        LABELS;
    }

    @Override
    public String getName() {
        return ECS_FIELDS_MARKER_NAME;
    }

    @Override
    public void add(final Marker reference) {
        if (reference == null) {
            return; // A null value cannot be added to a Marker as reference but, we don't want to throw exception
        }
        if (this.contains(reference)) { // no point in adding the reference multiple times
            return;
        } else if (reference.contains(this)) { // avoid recursion
            // a potential reference should not hold its future "parent" as a reference
            return;
        } else {
            referenceList.add(reference);
        }
    }

    @Override
    public boolean remove(final Marker reference) {
        return referenceList.remove(reference);
    }

    @Override
    public boolean hasChildren() {
        return hasReferences();
    }

    @Override
    public boolean hasReferences() {
        return !referenceList.isEmpty();
    }

    @Override
    public Iterator<Marker> iterator() {
        return referenceList.iterator();
    }

    @Override
    public boolean contains(final Marker other) {
        if (other == null) {
            return false;
        }
        if (this.equals(other)) {
            return true;
        }
        if (hasReferences()) {
            for (final Marker ref : referenceList) {
                if (ref.contains(other)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean contains(final String name) {
        if (name == null) {
            return false;
        }
        if (getName().equals(name)) {
            return true;
        }
        if (hasReferences()) {
            for (final Marker ref : referenceList) {
                if (ref.contains(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static final class Builder {

        private final Map<EcsField, EcsFieldSet> ecsFieldSet;

        private Builder() {
            ecsFieldSet = new HashMap<>();
        }

        public Builder event(final EventFieldSet event) {
            if (event != null) {
                ecsFieldSet.put(EcsField.EVENT, event);
            }
            return this;
        }

        public Builder labels(final LabelsFieldSet labels) {
            if (labels != null) {
                ecsFieldSet.put(EcsField.LABELS, labels);
            }
            return this;
        }

        public EcsFieldsMarker build() {
            return new EcsFieldsMarker(ecsFieldSet);
        }
    }
}
