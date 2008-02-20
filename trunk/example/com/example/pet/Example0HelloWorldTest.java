package com.example.pet;

import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.NoOpTestScopeListener;
import com.google.inject.testing.guiceberry.TestScopeListener;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Env;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;

@GuiceBerryEnv("com.example.pet.Example0HelloWorldTest$HelloWorldGuiceBerryEnv")
public class Example0HelloWorldTest extends GuiceBerryJunit3TestCase {

	public void testHello() throws Exception {
		assertTrue(true);
	}
	
	static final class HelloWorldGuiceBerryEnv extends GuiceBerryJunit3Env {
		@Override
		protected Class<? extends TestScopeListener> getTestScopeListener() {
			return NoOpTestScopeListener.class;
		}
	}
}
