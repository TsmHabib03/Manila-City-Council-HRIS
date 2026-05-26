package com.ian.web.fileupload;

import java.util.Arrays;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StorageFactory {

    private final Environment environment;
    private final FileStorageStrategy fileStorageStrategy;

    @Lazy
    public StorageFactory(Environment environment, 
    		FileStorageStrategy fileStorageStrategy) {
        this.environment = environment;
        this.fileStorageStrategy = fileStorageStrategy;
    }


    public StorageStrategy createStrategy() {
        String[] activeProfiles = environment.getActiveProfiles();
        log.info("Active profiles '{}'", Arrays.toString(activeProfiles));

        if (Arrays.stream(environment.getActiveProfiles()).anyMatch(
                env -> (env.equalsIgnoreCase("dev")))) {
            return this.fileStorageStrategy;
        } else if (Arrays.stream(environment.getActiveProfiles()).anyMatch(
                env -> (env.equalsIgnoreCase("prod")))) {
            return this.fileStorageStrategy;
        } else {
            return this.fileStorageStrategy;
        }
    }
}