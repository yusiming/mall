package com.mall.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 全局异常处理器
 *
 * @author yusiming
 * @date 2019/3/6 21:44
 */
public class GlobalExceptionResolver implements HandlerExceptionResolver {
    private static Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionResolver.class);

    /**
     * 处理异常
     *
     * @param httpServletRequest  request
     * @param httpServletResponse response
     * @param o                   handler处理器
     * @param e                   异常
     */
    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        LOGGER.error("URL: {} queryString： {} exception ", httpServletRequest.getRequestURL(), httpServletRequest.getQueryString(), e);
        // 如果jackson 的版本是2.x，应该使用 MappingJacksonJson2View
        ModelAndView modelAndView = new ModelAndView(new MappingJacksonJsonView());
        modelAndView.addObject("status", ResponseCode.ERROR.getCode());
        modelAndView.addObject("msg", "发生异常，请查看日志，查看具体信息");
        modelAndView.addObject("data", e.getMessage());
        return modelAndView;
    }
}
