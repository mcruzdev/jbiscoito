package dev.matheuscruz;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.qute.Qute;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "jbiscoito", mixinStandardHelpOptions = true)
public class JBiscoitoCommand implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(JBiscoitoCommand.class);
  private static final String JBISCOITO_JSON = "jbiscoito.json";

  @Parameters(
      paramLabel = "repository",
      defaultValue = "https://github.com/mcruzdev/jbiscoito-template",
      description = "Repository URL")
  String repoURL;

  @CommandLine.Option(
      names = {"-f", "--overwrite-if-exists"},
      defaultValue = "true",
      description = "Overwrite the output directory if it already exists")
  Boolean overwriteIfExists = Boolean.TRUE;

  private static final ObjectMapper jsonMapper = new ObjectMapper();

  @Override
  public void run() {

    try {
      String userDir = System.getProperty("user.dir");

      RepoURL repoURL = RepoURL.create(this.repoURL);

      Path cloneDirPath = buildCloneDir(userDir, repoURL);

      if (removeDirIfNecessary(cloneDirPath)) {
        return;
      }

      if (doGitClone(repoURL)) {
        return;
      }

      String dotGit = buildDotGitDir(userDir, repoURL);

      Map<String, Object> context = buildJBiscoitContext(userDir);

      Set<String> ignoreDirs = Set.of(dotGit);

      Set<Path> removeDirs = new HashSet<>();

      Files.walkFileTree(
          cloneDirPath,
          EnumSet.noneOf(FileVisitOption.class),
          Integer.MAX_VALUE,
          new SimpleFileVisitor<>() {
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
                LOGGER.error("Error while visiting file {}", file);
              }
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {
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

      removeDirs.forEach(
          dir -> {
            try {
              deleteDir(dir);
            } catch (IOException e) {
              LOGGER.error("Error while deleting directory {}", dir);
            }
          });

    } catch (Throwable e) {
      LOGGER.error(e.getMessage());
    }
  }

  private static String buildDotGitDir(String userDir, RepoURL repoURL) {
    return String.format("%s/%s/%s", userDir, repoURL.repositoryName(), ".git");
  }

  private static Map<String, Object> buildJBiscoitContext(String userDir) throws IOException {
    String ctx = String.format("%s/%s", userDir, JBISCOITO_JSON);
    return jsonMapper.readValue(new File(ctx), Map.class);
  }

  private static boolean doGitClone(RepoURL repoURL) throws IOException, InterruptedException {
    String cloneCommand = buildCloneCommand(repoURL);
    Process process = new ProcessBuilder().command(List.of("bash", "-c", cloneCommand)).start();
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

    String line;
    while ((line = reader.readLine()) != null) {
      LOGGER.info(line);
    }

    reader.close();
    int exitVal = process.waitFor();
    return exitVal != 0;
  }

  private static Path buildCloneDir(String userDir, RepoURL repoURL) {
    String cloneDir = String.format("%s/%s", userDir, repoURL.repositoryName());
    return Path.of(cloneDir);
  }

  private boolean removeDirIfNecessary(Path cloneDirPath) throws IOException {
    if (Files.exists(cloneDirPath)) {
      if (overwriteIfExists) {
        deleteDir(cloneDirPath);
      } else {
        LOGGER.info(
            "Couldn't replace the {} folder. "
                + "Please, turn on the --overwrite-if-exists option to true or delete the existing folder.",
            cloneDirPath.getFileName().toString());
        return true;
      }
    }
    return false;
  }

  private void deleteDir(Path path) throws IOException {
    if (!Files.exists(path)) {
      return;
    }
    Files.walkFileTree(
        path,
        EnumSet.noneOf(FileVisitOption.class),
        Integer.MAX_VALUE,
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
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

  public static String buildCloneCommand(final RepoURL repoURL) {
    return String.format("git clone %s", repoURL.value());
  }
}
