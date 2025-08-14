package com.izipay.IziPay.aspect;

import com.izipay.IziPay.model.enums.SystemAction;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogAction {
    SystemAction action();
    String details() default "";
}