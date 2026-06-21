package ai.haitham.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ResourceService {

    private ResourceService() {
    }

    /**
     * Reads a classpath resource into a UTF-8 string.
     */
    public static String readClasspathResource(String path) throws IOException {
        InputStream inputStream = ResourceService.class.getClassLoader().getResourceAsStream(path);

        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: " + path);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }

    /**
     * Lists resource file names inside a classpath directory.
     */
    public static List<String> listClasspathDirectory(String directoryName) throws IOException {
        URL resourceUrl = ResourceService.class.getClassLoader().getResource(directoryName);

        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource directory not found: " + directoryName);
        }

        if (!"file".equals(resourceUrl.getProtocol())) {
            throw new IOException("Resource directory is not file-based: " + directoryName);
        }

        try {
            Path directoryPath = Path.of(resourceUrl.toURI());
            try (var stream = Files.list(directoryPath)) {
                ArrayList<String> files = stream
                        .filter(Files::isRegularFile)
                        .map(path -> path.getFileName().toString())
                        .sorted(Comparator.naturalOrder())
                        .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
                return files;
            }
        } catch (URISyntaxException e) {
            throw new IOException("Unable to resolve resource directory: " + directoryName, e);
        }
    }
}