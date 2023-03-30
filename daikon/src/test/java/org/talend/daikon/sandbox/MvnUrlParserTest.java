/*
 * Copyright 2007 Alin Dreghiciu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.daikon.sandbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.MalformedURLException;

import org.junit.jupiter.api.Test;

public class MvnUrlParserTest {

    @Test
    public void constructorWithNullPath() {
        assertThrows(MalformedURLException.class, () -> {
            new MvnUrlParser(null);
        });
    }

    @Test
    public void urlStartingWithRepositorySeparator() {
        assertThrows(MalformedURLException.class, () -> {
            new MvnUrlParser("!group");
        });
    }

    @Test
    public void urlEndingWithRepositorySeparator() {
        assertThrows(MalformedURLException.class, () -> {
            new MvnUrlParser("http://repository!");
        });
    }

    @Test
    public void urlWithRepositoryAndNoGroup() {
        assertThrows(MalformedURLException.class, () -> {
            new MvnUrlParser("http://repository!");
        });
    }

    @Test
    public void urlWithoutRepositoryAndNoGroup() {
        assertThrows(MalformedURLException.class, () -> {
            new MvnUrlParser("");
        });
    }

    @Test
    public void urlWithRepositoryAndNoArtifact() {
        assertThrows(MalformedURLException.class, () -> {
            new MvnUrlParser("http://repository!group");
        });
    }

    @Test
    public void urlWithoutRepositoryAndNoArtifact() {
        assertThrows(MalformedURLException.class, () -> {
            new MvnUrlParser("group");
        });
    }

    @Test
    public void urlWithRepositoryAndGroupArtifact() throws MalformedURLException {
        MvnUrlParser MvnUrlParser = new MvnUrlParser("http://repository@id=fake!group/artifact");
        assertEquals("group", MvnUrlParser.getGroup(), "Group");
        assertEquals("artifact", MvnUrlParser.getArtifact(), "Artifact");
        assertEquals("LATEST", MvnUrlParser.getVersion(), "Version");
        assertEquals("jar", MvnUrlParser.getType(), "Type");
        assertEquals(null, MvnUrlParser.getClassifier(), "Classifier");
        assertEquals("group/artifact/LATEST/artifact-LATEST.jar", MvnUrlParser.getArtifactPath(), "Artifact path");
    }

    @Test
    public void urlWithoutRepositoryAndGroupArtifact() throws MalformedURLException {
        MvnUrlParser MvnUrlParser = new MvnUrlParser("group/artifact");
        assertEquals("group", MvnUrlParser.getGroup(), "Group");
        assertEquals("artifact", MvnUrlParser.getArtifact(), "Artifact");
        assertEquals("LATEST", MvnUrlParser.getVersion(), "Version");
        assertEquals("jar", MvnUrlParser.getType(), "Type");
        assertEquals(null, MvnUrlParser.getClassifier(), "Classifier");
        assertEquals("group/artifact/LATEST/artifact-LATEST.jar", MvnUrlParser.getArtifactPath(), "Artifact path");
    }

    @Test
    public void urlWithRepositoryAndGroupArtifactVersionType() throws MalformedURLException {
        MvnUrlParser MvnUrlParser = new MvnUrlParser("http://repository@id=fake!group/artifact/version/type");
        assertEquals("group", MvnUrlParser.getGroup(), "Group");
        assertEquals("artifact", MvnUrlParser.getArtifact(), "Artifact");
        assertEquals("version", MvnUrlParser.getVersion(), "Version");
        assertEquals("type", MvnUrlParser.getType(), "Type");
        assertEquals(null, MvnUrlParser.getClassifier(), "Classifier");
        assertEquals("group/artifact/version/artifact-version.type", MvnUrlParser.getArtifactPath(), "Artifact path");
    }

    @Test
    public void urlWithRepositoryAndGroupArtifactVersionTypeClassifier() throws MalformedURLException {
        MvnUrlParser MvnUrlParser = new MvnUrlParser("http://repository@id=fake!group/artifact/version/type/classifier");
        assertEquals("group", MvnUrlParser.getGroup(), "Group");
        assertEquals("artifact", MvnUrlParser.getArtifact(), "Artifact");
        assertEquals("version", MvnUrlParser.getVersion(), "Version");
        assertEquals("type", MvnUrlParser.getType(), "Type");
        assertEquals("classifier", MvnUrlParser.getClassifier(), "Classifier");
        assertEquals("group/artifact/version/artifact-version-classifier.type", MvnUrlParser.getArtifactPath(), "Artifact path");
    }

    @Test
    public void urlWithoutRepositoryAndGroupArtifactVersionType() throws MalformedURLException {
        MvnUrlParser MvnUrlParser = new MvnUrlParser("group/artifact/version/type");
        assertEquals("group", MvnUrlParser.getGroup(), "Group");
        assertEquals("artifact", MvnUrlParser.getArtifact(), "Artifact");
        assertEquals("version", MvnUrlParser.getVersion(), "Version");
        assertEquals("type", MvnUrlParser.getType(), "Type");
        assertEquals(null, MvnUrlParser.getClassifier(), "Classifier");
        assertEquals("group/artifact/version/artifact-version.type", MvnUrlParser.getArtifactPath(), "Artifact path");
    }

    @Test
    public void urlWithoutRepositoryAndGroupArtifactVersionTypeClassifier() throws MalformedURLException {
        MvnUrlParser MvnUrlParser = new MvnUrlParser("group/artifact/version/type/classifier");
        assertEquals("group", MvnUrlParser.getGroup(), "Group");
        assertEquals("artifact", MvnUrlParser.getArtifact(), "Artifact");
        assertEquals("version", MvnUrlParser.getVersion(), "Version");
        assertEquals("type", MvnUrlParser.getType(), "Type");
        assertEquals("classifier", MvnUrlParser.getClassifier(), "Classifier");
        assertEquals("group/artifact/version/artifact-version-classifier.type", MvnUrlParser.getArtifactPath(), "Artifact path");
    }

    @Test
    public void urlWithoutRepositoryAndGroupArtifactVersionClassifier() throws MalformedURLException {
        MvnUrlParser MvnUrlParser = new MvnUrlParser("group/artifact/version//classifier");
        assertEquals("group", MvnUrlParser.getGroup(), "Group");
        assertEquals("artifact", MvnUrlParser.getArtifact(), "Artifact");
        assertEquals("version", MvnUrlParser.getVersion(), "Version");
        assertEquals("jar", MvnUrlParser.getType(), "Type");
        assertEquals("classifier", MvnUrlParser.getClassifier(), "Classifier");
        assertEquals("group/artifact/version/artifact-version-classifier.jar", MvnUrlParser.getArtifactPath(), "Artifact path");
    }

    @Test
    public void urlWithoutRepositoryAndGroupArtifactTypeClassifier() throws MalformedURLException {
        MvnUrlParser MvnUrlParser = new MvnUrlParser("group/artifact//type/classifier");
        assertEquals("group", MvnUrlParser.getGroup(), "Group");
        assertEquals("artifact", MvnUrlParser.getArtifact(), "Artifact");
        assertEquals("LATEST", MvnUrlParser.getVersion(), "Version");
        assertEquals("type", MvnUrlParser.getType(), "Type");
        assertEquals("classifier", MvnUrlParser.getClassifier(), "Classifier");
        assertEquals("group/artifact/LATEST/artifact-LATEST-classifier.type", MvnUrlParser.getArtifactPath(), "Artifact path");
    }

    @Test
    public void urlWithoutRepositoryAndGroupArtifactClassifier() throws MalformedURLException {
        MvnUrlParser MvnUrlParser = new MvnUrlParser("group/artifact///classifier");
        assertEquals("group", MvnUrlParser.getGroup(), "Group");
        assertEquals("artifact", MvnUrlParser.getArtifact(), "Artifact");
        assertEquals("LATEST", MvnUrlParser.getVersion(), "Version");
        assertEquals("jar", MvnUrlParser.getType(), "Type");
        assertEquals("classifier", MvnUrlParser.getClassifier(), "Classifier");
        assertEquals("group/artifact/LATEST/artifact-LATEST-classifier.jar", MvnUrlParser.getArtifactPath(), "Artifact path");
    }

    @Test
    public void urlWithJarRepository() throws MalformedURLException {
        MvnUrlParser MvnUrlParser = new MvnUrlParser("jar:http://repository/repository.jar!/@id=fake!group/artifact/0.1.0");
        assertEquals("group/artifact/0.1.0/artifact-0.1.0.jar", MvnUrlParser.getArtifactPath(), "Artifact path");
    }

    @Test
    public void trailingSpace() throws MalformedURLException {
        MvnUrlParser MvnUrlParser = new MvnUrlParser(" http://repository/repository@id=fake!group/artifact/0.1.0");
        assertEquals("group/artifact/0.1.0/artifact-0.1.0.jar", MvnUrlParser.getArtifactPath(), "Artifact path");
    }

    @Test
    public void snapshotPath() throws MalformedURLException {
        MvnUrlParser MvnUrlParser = new MvnUrlParser("group/artifact/version-SNAPSHOT");
        assertEquals("group/artifact/version-SNAPSHOT/artifact-version-timestamp-build.jar",
                MvnUrlParser.getSnapshotPath("version-SNAPSHOT", "timestamp", "build"), "Artifact snapshot path");
    }

    @Test
    public void artifactPathWithVersion() throws MalformedURLException {
        MvnUrlParser MvnUrlParser = new MvnUrlParser("group/artifact/version");
        assertEquals("group/artifact/version2/artifact-version2.jar", MvnUrlParser.getArtifactPath("version2"), "Artifact path");
    }

    @Test
    public void versionMetadataPath() throws MalformedURLException {
        MvnUrlParser MvnUrlParser = new MvnUrlParser("group/artifact/version");
        assertEquals("group/artifact/version2/maven-metadata.xml", MvnUrlParser.getVersionMetadataPath("version2"),
                "Version metadata path");
    }

    @Test
    public void artifactMetadataPath() throws MalformedURLException {
        MvnUrlParser MvnUrlParser = new MvnUrlParser("group/artifact/version");
        assertEquals("group/artifact/maven-metadata.xml", MvnUrlParser.getArtifactMetdataPath(), "Artifact metadata path");
    }

    @Test
    public void artifactLocalMetadataPath() throws MalformedURLException {
        MvnUrlParser MvnUrlParser = new MvnUrlParser("group/artifact/version");
        assertEquals("group/artifact/maven-metadata-local.xml", MvnUrlParser.getArtifactLocalMetdataPath(),
                "Artifact local metadata path");
    }

}
