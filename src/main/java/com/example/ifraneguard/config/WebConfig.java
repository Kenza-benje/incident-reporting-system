package com.example.ifraneguard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Serves uploaded files from the local filesystem under a browser-accessible URL.
 *
 * app.upload.dir = "uploads/incidents"   (relative to the working directory)
 *
 * We register two handlers:
 *   /uploads/incidents/** → <workdir>/uploads/incidents/
 *   /uploads/**           → <workdir>/uploads/          (catch-all, future-proof)
 *
 * Both use the absolute URI form (file:///...) which Spring's ResourceHttpRequestHandler
 * requires for file-system locations.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Value is the full upload directory, e.g. "uploads/incidents".
     * We derive the parent "uploads/" directory for the broader handler.
     */
    @Value("${app.upload.dir:uploads/incidents}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Resolve absolute paths
        Path incidentsPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        // Parent is the "uploads/" folder one level up
        Path uploadsRoot   = incidentsPath.getParent() != null
                             ? incidentsPath.getParent()
                             : incidentsPath;

        // Convert to file URI strings — must end with "/"
        String incidentsUri = toResourceUri(incidentsPath);
        String uploadsUri   = toResourceUri(uploadsRoot);

        // /uploads/incidents/uuid.jpg  → <workdir>/uploads/incidents/uuid.jpg
        registry.addResourceHandler("/uploads/incidents/**")
                .addResourceLocations(incidentsUri);

        // /uploads/** catch-all (covers future sub-directories like /uploads/resolutions/)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadsUri);
    }

    private static String toResourceUri(Path path) {
        String uri = path.toUri().toString();
        return uri.endsWith("/") ? uri : uri + "/";
    }
}
