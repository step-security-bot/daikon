package org.talend.daikon.model;

import java.io.PrintWriter;

import com.atlassian.jira.rest.client.api.domain.Issue;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@AllArgsConstructor
public class JiraReleaseNoteItem implements ReleaseNoteItem {

    private final Issue issue;

    private final String jiraServerUrl;

    private final PullRequest pullRequest;

    @Override
    public ReleaseNoteItemType getIssueType() {
        if (issue != null) {
            return ReleaseNoteItemType.fromJiraIssueType(issue.getIssueType());
        }
        return ReleaseNoteItemType.MISC;
    }

    @Override
    public void writeTo(PrintWriter writer) {
        if (issue != null) {
            writer.print(
                    "- link:" + jiraServerUrl + "/browse/" + issue.getKey() + "[" + issue.getKey() + "]: " + issue.getSummary());
        }
        if (pullRequest != null) {
            writer.println(" (link:" + pullRequest.getUrl() + "[#" + pullRequest.getDisplay() + "])");
        } else {
            writer.println();
        }
    }

    @Override
    public String toString() {
        return "JiraReleaseNoteItem{" + getIssueType() + ", " + "issue=" + issue != null ? issue.getSummary() : "" + '}';
    }

}
