package org.jeffrey.core;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@ComponentScan("org.jeffrey.core")
@EnableAspectJAutoProxy
public class CoreAutoConfig {
}
