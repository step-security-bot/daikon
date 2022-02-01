package org.talend.daikon.logging.ecs.field;

import java.util.Map;

import org.talend.daikon.logging.ecs.EcsFields;

import lombok.Getter;

@Getter
public abstract class EcsFieldSet {

    private final Map<EcsFields, ?> itemsToSerialize;

    protected EcsFieldSet(final Map<EcsFields, ?> itemsToSerialize) {
        this.itemsToSerialize = itemsToSerialize;
    }

}
