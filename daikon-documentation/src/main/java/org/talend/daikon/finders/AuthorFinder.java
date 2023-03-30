package org.talend.daikon.finders;

import java.util.stream.Stream;

import org.talend.daikon.model.Author;

public interface AuthorFinder {

    Stream<Author> findAuthors();
}
