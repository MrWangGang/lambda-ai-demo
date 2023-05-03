package com.lambda.ai.demo;

import com.lambda.ai.demo.req.OpenAiReq;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lambda.framework.common.exception.EventException;
import org.lambda.framework.sub.openai.OpenAiContract;
import org.lambda.framework.sub.openai.OpenAiUniqueParam;
import org.lambda.framework.sub.openai.service.chat.OpenAiChatService;
import org.lambda.framework.sub.openai.service.chat.OpenAiFAQService;
import org.lambda.framework.sub.openai.service.chat.param.OpenAiChatParam;
import org.lambda.framework.sub.openai.service.chat.param.OpenAiFAQParam;
import org.lambda.framework.sub.openai.service.image.OpenAiImageService;
import org.lambda.framework.sub.openai.service.image.param.OpenAiImageParam;
import org.lambda.framework.web.core.handler.WebResponseHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static org.lambda.framework.common.enums.ExceptionEnum.EI00000000;
import static org.lambda.framework.sub.openai.OpenAiContract.encoding;
import static org.lambda.framework.sub.openai.OpenAiContract.imageTokens;


@SpringBootApplication(scanBasePackages = {"com.lambda.ai.demo","org.lambda.framework"} )
@EnableWebFlux
@RestController
@RequestMapping
@Slf4j
public class OpenAiChatGptApplication extends WebResponseHandler implements WebFluxConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(OpenAiChatGptApplication.class, args);
    }
    @Bean
    public WebFilter faviconFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {
            if (exchange.getRequest().getURI().getPath().equals("/favicon.ico")) {
                exchange.getResponse().setStatusCode(HttpStatus.NO_CONTENT);
                return exchange.getResponse().setComplete();
            }
            return chain.filter(exchange);
        };
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/","/favicon.ico");
    }

    @Resource
    private OpenAiChatService openAiChatService;

    @Resource
    private OpenAiFAQService openAiFAQService;

    @Resource
    private OpenAiImageService openAiImageService;

    private static String openAiApiKey = "your key";

    private static String userId = "1";

    private static Long quato = 300000L;
    @PostMapping("/lambda/openai/uniqueId")
    public Mono uniqueId(){
        return returning(openAiChatService.uniqueId(userId));
    }
    @PostMapping("/lambda/openai/chat")
    public Mono chat(@RequestBody OpenAiReq req){
        if(req == null)throw new EventException(EI00000000,"不能发送空消息");
        if(StringUtils.isBlank(req.getPrompt()))throw new EventException(EI00000000,"不能发送空消息");
        if(StringUtils.isBlank(req.getUniqueId()))throw new EventException(EI00000000,"chatId不能为空");
        if(StringUtils.isBlank(req.getUniqueTime()))throw new EventException(EI00000000,"uniqueTime");

        OpenAiUniqueParam openAiUniqueParam = OpenAiUniqueParam.builder().uniqueId(req.getUniqueId()).uniqueTime(req.getUniqueTime()).build();

        OpenAiChatParam param =  OpenAiChatParam.builder()
                .prompt(req.getPrompt())
                .uniqueParam(openAiUniqueParam)
                .userId(userId)
                .apiKey(openAiApiKey)
                .frequencyPenalty(.5)
                .temperature(.7)
                .n(1)
                .maxTokens(2048)
                .persona("你是一个高情商,幽默的AI")
                .timeOut(OpenAiContract.clientTimeOut)
                .quota(quato)
                .build();
        return returning(openAiChatService.execute(param).flatMap(e->{
            //模拟扣减配额
            quato = quato-e.getTotalTokens();
            return Mono.just(e);
        }));

    }

    @PostMapping("/lambda/openai/faq")
    public Mono QA(@RequestBody OpenAiReq req){

        if(req == null)throw new EventException(EI00000000,"不能发送空消息");
        if(StringUtils.isBlank(req.getPrompt()))throw new EventException(EI00000000,"不能发送空消息");
        if(StringUtils.isBlank(req.getUniqueId()))throw new EventException(EI00000000,"chatId不能为空");
        if(StringUtils.isBlank(req.getUniqueTime()))throw new EventException(EI00000000,"uniqueTime");
        OpenAiUniqueParam openAiUniqueParam = OpenAiUniqueParam.builder().uniqueId(req.getUniqueId()).uniqueTime(req.getUniqueTime()).build();
        OpenAiFAQParam param =  OpenAiFAQParam.builder()
                .prompt(req.getPrompt())
                .uniqueParam(openAiUniqueParam)
                .userId(userId)
                .apiKey(openAiApiKey)
                .frequencyPenalty(.5)
                .temperature(.7)
                .n(1)
                .maxTokens(2048)
                .persona("你是一个知识渊博,做事严谨,庄重严肃的大学教授")
                .timeOut(OpenAiContract.clientTimeOut)
                .quota(quato)
                .build();
        return returning(openAiFAQService.execute(param).flatMap(e->{
            //模拟扣减配额
            quato = quato-e.getTotalTokens();
            return Mono.just(e);
        }));
    }

    @PostMapping("/lambda/openai/image")
    public Mono paint(@RequestBody OpenAiReq req){

        if(req == null)throw new EventException(EI00000000,"不能发送空消息");
        if(StringUtils.isBlank(req.getPrompt()))throw new EventException(EI00000000,"不能发送空消息");
        if(StringUtils.isBlank(req.getUniqueId()))throw new EventException(EI00000000,"chatId不能为空");
        if(StringUtils.isBlank(req.getUniqueTime()))throw new EventException(EI00000000,"uniqueTime");

        OpenAiUniqueParam openAiUniqueParam = OpenAiUniqueParam.builder().uniqueId(req.getUniqueId()).uniqueTime(req.getUniqueTime()).build();
        OpenAiImageParam param =  OpenAiImageParam.builder()
                .prompt(req.getPrompt())
                .uniqueParam(openAiUniqueParam)
                .userId(userId)
                .apiKey(openAiApiKey)
                .n(4)
                .size(OpenAiContract.image_size_512)
                .responseFormat(OpenAiContract.responseFormat)
                .timeOut(OpenAiContract.clientTimeOut)
                .quota(quato)
                .maxTokens(imageTokens(OpenAiContract.image_size_512,4) + encoding(req.getPrompt()))
                .build();
        return returning(openAiImageService.execute(param).flatMap(e->{
            //模拟扣减配额
            quato = quato-e.getTotalTokens();
            return Mono.just(e);
        }));
    }
}
