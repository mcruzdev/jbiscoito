package dev.matheuscruz.git;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RepoURLTest {


    @Test
    void should_return_the_repository_name_correctly() {
        RepoURL repoURL = RepoURL.create("git@github.com:quarkiverse/quarkus-openapi-generator.git");
        Assertions.assertEquals("quarkus-openapi-generator", repoURL.repositoryName());
    }
}