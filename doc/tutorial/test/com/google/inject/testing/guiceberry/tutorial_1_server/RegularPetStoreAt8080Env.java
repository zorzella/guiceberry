package com.google.inject.testing.guiceberry.tutorial_1_server;

import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.testing.guiceberry.NoOpTestScopeListener;
import com.google.inject.testing.guiceberry.TestScopeListener;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Env;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class RegularPetStoreAt8080Env extends GuiceBerryJunit3Env {
  
  private static final int PORT_NUMBER = 8080;

  @Retention(RetentionPolicy.RUNTIME) 
  @Target({ElementType.FIELD, ElementType.PARAMETER}) 
  @BindingAnnotation
  private @interface PortNumber {}

  @Override
  protected void configure() {
    super.configure();
    bind(Integer.class).annotatedWith(PortNumber.class).toInstance(8080);
  }
  
  @Provides
  WebDriver getWebDriver(@PortNumber int portNumber) {
    WebDriver driver = new HtmlUnitDriver();
    driver.get("http://localhost:" + portNumber);
    return driver;
  }
  
  @Provides
  @Singleton
  MyPetStoreServer startServer() {
    MyPetStoreServer result = new MyPetStoreServer(RegularPetStoreAt8080Env.PORT_NUMBER);
    // It's always sane to separate the "start"ing of a server from the
    // constructor.
    result.start();
    return result;
  }
  
  @Override
  protected Class<? extends TestScopeListener> getTestScopeListener() {
    return NoOpTestScopeListener.class;
  }
}