package com.belisty.function.util;

import java.util.List;

import com.demo.exception.DemoAppException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailRequest {

    private String from;
    private List<String> to;
    private Body body;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Body {
        private String subject;
        private Content content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private String html;
    }

    // Utility method to convert to JSON string
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            throw new DemoAppException("Failed to convert EmailRequest to JSON", e);
        }
    }
}
