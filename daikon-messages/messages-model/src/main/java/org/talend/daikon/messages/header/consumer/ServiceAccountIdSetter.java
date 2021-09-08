package org.talend.daikon.messages.header.consumer;

@FunctionalInterface
public interface ServiceAccountIdSetter {

    void setCurrentServiceAccountId(String serviceAccountId);
}
