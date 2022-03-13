package me.jyh.restapi.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE) //애노테이션을 붙인 코드를 얼마나 오래 가져갈 것인가
public @interface TestDescription {

    String value();

}
