package mg.itu.prom16.annotation;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})

public @interface GET {
    String value() default "";
}
