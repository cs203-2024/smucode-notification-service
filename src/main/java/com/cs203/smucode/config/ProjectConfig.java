package com.cs203.smucode.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "com.cs203.smucode.controllers",
        "com.cs203.smucode.repositories",
        "com.cs203.smucode.services"
})
public class ProjectConfig {
}
