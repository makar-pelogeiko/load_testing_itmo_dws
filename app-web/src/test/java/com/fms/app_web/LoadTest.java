package com.fms.app_web;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.time.Duration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fms.app_web.dto.FileRequestDTO;
import org.apache.http.entity.ContentType;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;

import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class LoadTest {

    private final Faker faker = new Faker();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private FileRequestDTO generateRequest() {
        return new FileRequestDTO(faker.food().ingredient(), faker.food().ingredient());
    }

    @Test
    public void testPerformance() throws Exception {
        TestPlanStats stats = testPlan(
                rpsThreadGroup()
                        .maxThreads(32)
                        .rampTo(10.0, Duration.ofSeconds(5))
                        .rampTo(100.0, Duration.ofSeconds(5))
                        .rampTo(1000.0, Duration.ofSeconds(10))
                        .rampTo(10000.0, Duration.ofSeconds(10))
                        .rampTo(100000.0, Duration.ofSeconds(10))
                        .children(
                                httpSampler("Create File", "http://localhost:8080/api/v1/file")
                                        .post((x) -> {
                                            try {
                                                return objectMapper.writeValueAsString(generateRequest());
                                            } catch (JsonProcessingException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }, ContentType.APPLICATION_JSON)
                                        .children(jsonExtractor("fileSavedId", "data.id")),

                                httpSampler("Get file", "http://localhost:8080/api/v1/file/${fileSavedId}")
                                        .method(HTTPConstants.GET)
                                        .contentType(ContentType.APPLICATION_JSON)
                                        .children(jsonAssertion("data.name"))
                        ),
                htmlReporter("target/jmeter/reports")
        ).run();

        assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofMillis(500));
        Assertions.assertEquals(0, stats.overall().errorsCount());
    }
}
