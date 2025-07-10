package com.kuit.findyou.global.common.config;

import com.kuit.findyou.global.common.annotation.CustomExceptionDescription;
import com.kuit.findyou.global.common.response.BaseErrorResponse;
import com.kuit.findyou.global.common.response.BaseResponse;
import com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus;
import com.kuit.findyou.global.common.swagger.ExampleHolder;
import com.kuit.findyou.global.common.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;

@OpenAPIDefinition(
        info = @Info(
                title = "찾아유 V2 API 명세서",
                description = "Springdoc을 이용한 Swagger API 문서입니다.",
                version = "1.0"
        )
)
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI();
    }

    @Bean
    public OperationCustomizer customize() {
        return (Operation operation, HandlerMethod handlerMethod) -> {

            CustomExceptionDescription customExceptionDescription = handlerMethod.getMethodAnnotation(
                    CustomExceptionDescription.class);

            // CustomExceptionDescription 어노테이션 단 메소드 적용
            if (customExceptionDescription != null) {
                generateErrorCodeResponseExample(operation, customExceptionDescription.value());
            }

            return operation;
        };
    }


    private void generateErrorCodeResponseExample(
            Operation operation, SwaggerResponseDescription type) {

        ApiResponses responses = operation.getResponses();

        Set<BaseExceptionResponseStatus> baseExceptionResponseStatusSet = type.getExceptionResponseStatusSet();

        Map<Integer, List<ExampleHolder>> statusWithExampleHolders =
                baseExceptionResponseStatusSet.stream()
                        .map(
                                baseExceptionResponseStatus -> {
                                    return ExampleHolder.builder()
                                            .holder(
                                                    getSwaggerExample(baseExceptionResponseStatus))
                                            .code(baseExceptionResponseStatus.getCode())
                                            .name(baseExceptionResponseStatus.toString())
                                            .build();
                                }
                        ).collect(groupingBy(ExampleHolder::getCode));
        addExamplesToResponses(responses, statusWithExampleHolders);
    }


    private Example getSwaggerExample(BaseExceptionResponseStatus status) {
        Example example = new Example();

        BaseErrorResponse errorResponse = new BaseErrorResponse(status);
        example.setValue(errorResponse);
        example.description(status.getMessage());

        return example;
    }


    private void addExamplesToResponses(
            ApiResponses responses, Map<Integer, List<ExampleHolder>> statusWithExampleHolders) {
        statusWithExampleHolders.forEach(
                (status, v) -> {
                    Content content = new Content();
                    MediaType mediaType = new MediaType();
                    ApiResponse apiResponse = new ApiResponse();
                    v.forEach(
                            exampleHolder -> {
                                mediaType.addExamples(
                                        exampleHolder.getName(), exampleHolder.getHolder());
                            });
                    content.addMediaType("application/json", mediaType);
                    apiResponse.setDescription("");
                    apiResponse.setContent(content);
                    responses.addApiResponse(status.toString(), apiResponse);
                });
    }
}
