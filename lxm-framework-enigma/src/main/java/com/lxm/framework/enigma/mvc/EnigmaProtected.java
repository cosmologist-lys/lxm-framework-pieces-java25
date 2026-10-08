package com.lxm.framework.enigma.mvc;

import java.lang.annotation.*;

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnigmaProtected {
    Mode value() default Mode.ENCRYPT;
    enum Mode { ENCRYPT, SIGN }
}
