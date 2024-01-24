package dev.matheuscruz.git;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RepoURL {

    private static final Pattern gitUrlPattern = Pattern.compile(".*/(.*?)(\\.git)?$");
    private final String value;
    private final String repositoryName;

    private RepoURL(String value) {
        Matcher matcher = gitUrlPattern.matcher(value);
        if (matcher.find()) {
            final int repositoryNameGroup = 1;
            this.value = value;
            this.repositoryName = matcher.group(repositoryNameGroup);
        } else {
            throw new IllegalArgumentException("The repository URL is invalid");
        }
    }

    public static RepoURL create(final String value) {
        return new RepoURL(value);
    }

    public String value() {
        return this.value;
    }

    public String repositoryName() {
        return this.repositoryName;
    }
}
