package com.stakater.nordmart.catalog.common;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class Utils
{
    public static HttpServletRequest getCurrentHttpRequest()
    {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes)
        {
            return ((ServletRequestAttributes)requestAttributes).getRequest();
        }
        return null;
    }
}
