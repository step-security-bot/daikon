package org.talend.daikon.model;

import org.eclipse.jgit.revwalk.RevCommit;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GitCommit {

    private RevCommit commit;

    private PullRequest pullRequest;

}
