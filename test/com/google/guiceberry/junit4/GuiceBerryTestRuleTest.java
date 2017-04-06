package com.google.guiceberry.junit4;

import com.google.guiceberry.GuiceBerryModule;
import com.google.inject.AbstractModule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.model.Statement;

@RunWith(JUnit4.class)
public class GuiceBerryTestRuleTest {
  @Rule public ExpectedException thrown = ExpectedException.none();
  TestStatement base = new TestStatement();
  Description description = Description.createTestDescription(GuiceBerryTestRuleTest.class, "name");

  @Test
  public void ruleUsingThis_ok() throws Throwable {
    GuiceBerryTestRule testRule = new GuiceBerryTestRule(this, Env.class);
    testRule.apply(base, description).evaluate();
    Assert.assertEquals(1, base.getEvaluations());
  }

  @Test
  public void ruleNotUsingThis_throws() throws Throwable {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(GuiceBerryTestRuleTest.class.getSimpleName());
    GuiceBerryTestRule testRule = new GuiceBerryTestRule("not the test object", Env.class);
    testRule.apply(base, description).evaluate();
    Assert.assertEquals(0, base.getEvaluations());
  }
  
  private static class TestStatement extends Statement {
    private int evaluations = 0;

    @Override
    public void evaluate() throws Throwable {
      evaluations++;
    }
    
    public int getEvaluations() {
      return evaluations;
    }
  }

  public static final class Env extends AbstractModule {
    @Override
    protected void configure() {
      install(new GuiceBerryModule());      
    }
  }
}
