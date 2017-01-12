package junit4.tutorial_0_basic;
import static org.junit.Assert.assertEquals;

import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.guiceberry.junit4.GuiceBerryTestRule;
import com.google.inject.AbstractModule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Example showing the use of {@link GuiceBerryTestRule} in a {@link RuleChain}.
 *
 * <p>In this example, the {@code DependentRule} updates some piece of global state that
 * the Guiceberry rule initializes.  In a more realistic example it might make some calls to
 * an external server that the Guiceberry rule started up.  Sharing of global state like this is
 * never ideal, but might be necessary in some cases.
 *
 * <p>Since the dependent rule depends on the Guiceberry rule having been initialized, it has
 * to run inside that rule.  Junit4 {@link RuleChain} is intended for such cases.  Note that
 * this only works for {@link GuiceBerryTestRule} which (unlike {@link GuiceBerryRule}) implements
 * {@link TestRule} as required by {@link RuleChain}.
 */
public class Example6TestRuleInChainTest {
  private static String globalState;

  public static String getGlobalState() {
    return globalState;
  }

  public static void setGlobalState(String newState) {
    globalState = newState;
  }

  @Rule
  public RuleChain rules = RuleChain.outerRule(new GuiceBerryTestRule(this, Env.class))
      .around(new DependentRule());

  @Test
  public void testOne() {
    assertEquals(globalState,
        "state plus more for testOne(junit4.tutorial_0_basic.Example6TestRuleInChainTest)");
  }

  public static final class DependentRule implements TestRule {

    @Override
    public Statement apply(Statement base, Description description) {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          setGlobalState(getGlobalState() + " plus more for " + description.getDisplayName());
          base.evaluate();
        }
      };
    }
  }

  public static final class Env extends AbstractModule {
    @Override
    protected void configure() {
      setGlobalState("state");
      install(new GuiceBerryModule());
    }
  }
}
