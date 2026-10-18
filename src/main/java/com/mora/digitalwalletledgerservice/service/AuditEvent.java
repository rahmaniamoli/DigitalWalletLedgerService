package com.mora.digitalwalletledgerservice.service;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditEvent {
    String value();
}
