package org.talend.dsel.exception;

public class DQCategoryNotFoundException extends RuntimeException {

    public DQCategoryNotFoundException(String categoryName) {
        super(categoryName);
    }
}
