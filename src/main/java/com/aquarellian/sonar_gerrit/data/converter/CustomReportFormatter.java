package com.aquarellian.sonar_gerrit.data.converter;

import com.aquarellian.sonar_gerrit.data.entity.Issue;
import com.aquarellian.sonar_gerrit.data.entity.Severity;
import com.aquarellian.sonar_gerrit.data.predicates.ByExactSeverityPredicate;
import com.aquarellian.sonar_gerrit.data.predicates.ByMinSeverityPredicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Project: Sonar-Gerrit Plugin
 * Author:  Tatiana Didik
 * Created: 16.09.2015 13:17
 * <p/>
 * $Id$
 */
public class CustomReportFormatter implements TagFormatter<CustomReportFormatter.Tag> {
    public static final String DEFAULT_NO_ISSUES_TEXT = "Sonar violations have not been found.";
    public static final String DEFAULT_SOME_ISSUES_TEXT = "<total_count> Sonar violations have been found.";

    private String successMessage;
    private String failMessage;

    private Iterable<Issue> issues;

    public CustomReportFormatter(Iterable<Issue> issues, String failMessage, String successMessage) {
        this.issues = issues;
        this.failMessage = prepareText(failMessage, DEFAULT_SOME_ISSUES_TEXT);
        this.successMessage = prepareText(successMessage, DEFAULT_NO_ISSUES_TEXT);
    }

    private static String prepareText(String text, String defaultValue) {
        return text != null && !text.trim().isEmpty()? text.trim() : defaultValue;
    }

    public String getMessage() {
        String res = getSize(issues) > 0 ? failMessage : successMessage;
        for (Tag tag : Tag.values()) {
            res = res.replace(tag.getName(), getValueToReplace(tag));
        }
        return res;
    }

    public String getValueToReplace(Tag tag) {
        int value = 0;
        switch (tag) {
            case INFO_ISSUE_COUNT:
            case MINOR_ISSUE_COUNT:
            case MAJOR_ISSUE_COUNT:
            case CRITICAL_ISSUE_COUNT:
            case BLOCKER_ISSUE_COUNT:
                value = getSize(filterByExactSeverityPredicate(tag.getSeverity()));
                break;
            case AT_LEAST_MINOR_ISSUE_COUNT:
            case AT_LEAST_MAJOR_ISSUE_COUNT:
            case AT_LEAST_CRITICAL_ISSUE_COUNT:
            case TOTAL_COUNT:
                value = getSize(filterByMinSeverityPredicate(tag.getSeverity()));
                break;
        }
        return String.valueOf(value);
    }

    private int getSize(Iterable i) {
        return Lists.newArrayList(i).size();
    }

    private Iterable<Issue> filterByExactSeverityPredicate(Severity s) {
        return Iterables.filter(issues, ByExactSeverityPredicate.apply(s));
    }

    private Iterable<Issue> filterByMinSeverityPredicate(Severity s) {
        return Iterables.filter(issues, ByMinSeverityPredicate.apply(s));
    }

    public enum Tag {
        INFO_ISSUE_COUNT("<info_count>", Severity.INFO),
        MINOR_ISSUE_COUNT("<minor_count>", Severity.MINOR),
        MAJOR_ISSUE_COUNT("<major_count>", Severity.MAJOR),
        CRITICAL_ISSUE_COUNT("<critical_count>", Severity.CRITICAL),
        BLOCKER_ISSUE_COUNT("<blocker_count>", Severity.BLOCKER),
        AT_LEAST_MINOR_ISSUE_COUNT("<min_minor_count>", Severity.MINOR),
        AT_LEAST_MAJOR_ISSUE_COUNT("<min_major_count>", Severity.MAJOR),
        AT_LEAST_CRITICAL_ISSUE_COUNT("<min_critical_count>", Severity.CRITICAL),
        TOTAL_COUNT("<total_count>", Severity.INFO);

        private final String name;
        private final Severity severity;

        Tag(String name, Severity severity) {
            this.name = name;
            this.severity = severity;
        }

        public String getName() {
            return name;
        }

        public Severity getSeverity() {
            return severity;
        }
    }
}

