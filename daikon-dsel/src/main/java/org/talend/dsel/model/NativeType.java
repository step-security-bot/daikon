package org.talend.dsel.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum NativeType {

    INTEGER("INTEGER", "LONG"),
    DECIMAL("INTEGER", "LONG", "FLOAT", "DOUBLE"),
    BOOLEAN("BOOLEAN"),
    DATE("DATE", "DATETIME", "LOCALDATE", "LOCALDATETIME");

    private Set<String> equivalentJavaTypes;

    NativeType(String... equivalentJavaTypes) {
        this.equivalentJavaTypes = new HashSet<>(Arrays.asList(equivalentJavaTypes));
    }

    public Set<String> getEquivalentJavaTypes() {
        return equivalentJavaTypes;
    }
}
