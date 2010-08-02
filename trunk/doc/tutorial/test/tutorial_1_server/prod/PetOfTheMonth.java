package tutorial_1_server.prod;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes which {@link Pet} is the special pet to be advertised this month.
 */
@Retention(RetentionPolicy.RUNTIME) 
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER}) 
@BindingAnnotation
public @interface PetOfTheMonth {}