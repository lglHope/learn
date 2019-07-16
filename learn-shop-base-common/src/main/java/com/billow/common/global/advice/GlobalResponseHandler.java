package com.billow.common.global.advice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.billow.tools.enums.ResCodeEnum;
import com.billow.tools.resData.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by billow.
 *
 * @author: bilow
 * @version: 1.0
 * @date: 2019/5/25 12:36
 * @apiNote: 统一返回数据格式
 */
@Slf4j
@ControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private AntPathMatcher matcher = new AntPathMatcher();

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 判断支持的类型，因为我们定义的BaseResponseVo 里面的data可能是任何类型，这里就不判断统一放过
        // 如果你想对执行的返回体进行操作，可将上方的Object换成你自己的类型
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        log.info("请求的方法：{}", returnType.getMethod());
        if (body == null) {
            log.info("返回的类型：void");
        } else {
            log.info("返回的类型：{}", body.getClass().getName());
        }

        ServletServerHttpRequest httpRequest = (ServletServerHttpRequest) request;
        HttpServletRequest servletRequest = httpRequest.getServletRequest();
        String requestURI = servletRequest.getRequestURI();
        log.info("请求的方法URI：{}", requestURI);
        if (matcher.match("", requestURI)
                || matcher.match("/**/v2/api-docs", requestURI)) {
            return body;
        }

        BaseResponse baseResponse;

        if (body instanceof BaseResponse) {
            baseResponse = (BaseResponse) body;
        } else if (body instanceof LinkedHashMap) {
            Map<String, Object> map = (LinkedHashMap<String, Object>) body;
            ResCodeEnum resCode = (ResCodeEnum) map.get("resCode");
            if (resCode == null) {
                resCode = ResCodeEnum.RESCODE_OTHER_ERROR;
            }
            baseResponse = new BaseResponse(resCode);
        } else {
            baseResponse = BaseResponse.success(body);
        }

        log.debug("\n响应参数：{} ", JSON.toJSONString(baseResponse));

        // 处理返回值是String的情况
        if (body instanceof String) {
            return JSONObject.toJSONString(BaseResponse.success(body));
        }

        return baseResponse;
    }
}