package com.lambda.ai.demo.req;

import lombok.Data;

@Data
public class OpenAiReq {
    private String prompt;

    private String uniqueId;

    private String uniqueTime;
}
