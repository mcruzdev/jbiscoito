package dev.matheuscruz.git;

public class Git {

    public String cloneCommand(final RepoURL repoURL) {
        return String.format("git clone %s", repoURL.value());
    }
}
