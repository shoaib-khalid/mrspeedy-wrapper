package com.kalsym.mrspeedywrapper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                //.paths(PathSelectors.any())
                .apis(RequestHandlerSelectors.basePackage("com.kalsym.mrspeedywrapper"))
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("mrspeedy-wrapper API Document")
                .description("Used to deliver item via mrspeedy-wrapper")
                .termsOfServiceUrl("not added yet")
                .license("not added yet")
                .licenseUrl("").version("1.0").build();
    }
}

