package dev.matheuscruz;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.matheuscruz.git.Git;
import dev.matheuscruz.git.RepoURL;
import io.quarkus.qute.Qute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Command(name = "jbiscoito", mixinStandardHelpOptions = true)
public class JBiscoitoCommand implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(JBiscoitoCommand.class);
    private static final String JBISCOITO_JSON = "jbiscoito.json";
    @Parameters(paramLabel = "repository", defaultValue = "https://github.com/mcruzdev/quarkus-template",
            description = "Repository URL")
    String repoURL;

    @CommandLine.Option(names = {"-f", "--overwrite-if-exists"}, defaultValue = "true",
            description = "Overwrite the output directory if it already exists")
    Boolean overwriteIfExists = Boolean.TRUE;

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public void run() {
        try {

            RepoURL repoURL = RepoURL.create(this.repoURL);
            String userDir = System.getProperty("user.dir");
            String cloneDir = String.format("%s/%s", userDir, repoURL.repositoryName());
            Path cloneDirPath = Path.of(cloneDir);

            if (Files.exists(cloneDirPath)) {
                if (overwriteIfExists) {
                    deleteDir(cloneDirPath);
                } else {
                    LOGGER.error("Couldn't replace the {} folder. " +
                                    "Please, turn on the --overwrite-if-exists option to true or delete the existing folder.",
                            cloneDirPath.getFileName().toString());
                    return;
                }
            }

            String cloneCommand = new Git().cloneCommand(repoURL);
            Process process = new ProcessBuilder().command("bash", "-c", cloneCommand).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                LOGGER.info(line);
            }

            reader.close();
            int exitVal = process.waitFor();
            if (exitVal != 0) {
                return;
            }

            String ctx = String.format("%s/%s", userDir, JBISCOITO_JSON);
            String dotGit = String.format("%s/%s/%s", userDir, repoURL.repositoryName(), ".git");
            Map<String, Object> context = jsonMapper.readValue(new File(ctx), Map.class);

            Set<String> ignoreDirs = Set.of(dotGit);
            Set<Path> removeDirs = new HashSet<>();

            Files.walkFileTree(cloneDirPath, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (ignoreDirs.stream().anyMatch(file::startsWith)) {
                        return FileVisitResult.CONTINUE;
                    }

                    try {
                        String fileName = file.toString();
                        String newName = Qute.fmt(fileName, context);
                        Path newPath = Path.of(newName);

                        if (!fileName.equals(newName)) {
                            Files.copy(file, newPath, StandardCopyOption.REPLACE_EXISTING);
                        }

                        String content = Files.readString(newPath);
                        String fmt = Qute.fmt(content, context);
                        Files.writeString(newPath, fmt);
                    } catch (Exception e) {
                        LOGGER.error("Error while visiting file {}", file, e);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (ignoreDirs.stream().anyMatch(dir::startsWith)) {
                        return FileVisitResult.CONTINUE;
                    }

                    try {
                        String fileName = dir.getFileName().toString();
                        String newName = Qute.fmt(fileName, context);
                        Path newPath = dir.resolveSibling(newName);

                        if (!fileName.equals(newName)) {
                            Files.copy(dir, newPath, StandardCopyOption.REPLACE_EXISTING);
                            removeDirs.add(dir);
                        }

                        return FileVisitResult.CONTINUE;
                    } catch (Exception e) {
                        LOGGER.error("Error while visiting dir {}", dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            removeDirs.forEach(dir -> {
                try {
                    deleteDir(dir);
                } catch (IOException e) {
                    LOGGER.error("Error while deleting directory {}", dir);
                }
            });

        } catch (Exception e) {
            LOGGER.error("Error while cloning the repository", e);
            throw new RuntimeException(e);
        }
    }

    private void deleteDir(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                } else {
                    throw exc;
                }
            }
        });
    }

}
