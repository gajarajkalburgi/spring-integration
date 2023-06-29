package spring.integration.core.config;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class IntegrationImportSelector implements ImportSelector {

    private static final String[] IMPORT_CLASSES = {
            "spring.integration.core.config.IntegrationConfig"
    };

    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        return IMPORT_CLASSES;
    }
}
