package com.study.forum.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wy
 * @version 1.0
 */
// 注解作用于方法
@Target(value = ElementType.METHOD)
// 程序运行时生效
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {
}
