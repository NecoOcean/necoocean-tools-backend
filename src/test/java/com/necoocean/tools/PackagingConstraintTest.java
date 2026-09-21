package com.necoocean.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 后端资源目录不放页面模板。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
class PackagingConstraintTest {

    @Test
    void mainResourcesContainNoHtml() throws IOException {
        Path resources = Path.of("src/main/resources");
        try (Stream<Path> walk = Files.walk(resources)) {
            List<Path> htmlFiles = walk.filter(path -> path.toString().endsWith(".html")).toList();
            assertThat(htmlFiles).isEmpty();
        }
    }

    @Test
    void classpathHasNoTemplateEngine() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assertThat(classLoader.getResource("org/thymeleaf/TemplateEngine.class")).isNull();
    }
}
