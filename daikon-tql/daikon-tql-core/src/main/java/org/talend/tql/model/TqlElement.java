package org.talend.tql.model;

import org.talend.tql.visitor.IASTVisitor;

/*
 * All Tql elements implement this interface.
 */

/**
 * Created by gmzoughi on 23/06/16.
 */
public interface TqlElement {

    <T> T accept(IASTVisitor<T> visitor);

    String toQueryString();
}
