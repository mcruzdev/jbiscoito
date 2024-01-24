package dev.matheuscruz.qute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class QuteVisitor extends SimpleFileVisitor<Path> {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuteVisitor.class);

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        LOGGER.info(file.getFileName().toString());
        return FileVisitResult.CONTINUE;
    }
}
