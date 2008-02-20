package com.example.pet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.NoOpTestScopeListener;
import com.google.inject.testing.guiceberry.TestScopeListener;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Env;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;

@GuiceBerryEnv("com.example.pet.Example1HelloWorldGetsInjectedTest$HelloWorldGuiceBerryEnv")
public class Example1GetsInjectedTest extends GuiceBerryJunit3TestCase {
	
	@Inject
	@PortNumber
	private int portNumber;
	
	public void testHello() throws Exception {
		assertEquals(100, portNumber);
	}
	
	static final class HelloWorldGuiceBerryEnv extends GuiceBerryJunit3Env {
		@Override
		protected Class<? extends TestScopeListener> getTestScopeListener() {
			return NoOpTestScopeListener.class;
		}
		
		@Override
		protected void configure() {
			super.configure();
			bind(Integer.class).annotatedWith(PortNumber.class).toInstance(100);
		}
	}
	
	@Retention(RetentionPolicy.RUNTIME) 
	@Target(ElementType.FIELD) 
	@BindingAnnotation
	private @interface PortNumber {
	}
}
