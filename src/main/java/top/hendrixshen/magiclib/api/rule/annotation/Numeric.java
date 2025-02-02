package top.hendrixshen.magiclib.api.rule.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Numeric {
    double maxValue();

    double minValue();

    boolean canMaxEquals() default false;

    boolean canMinEquals() default false;
}
