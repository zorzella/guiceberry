# Getting Started #
We're developing a web app that lets consumers order pizza. It's a pretty standard setup: it uses a database to store the pizza orders and a webserver to talk to the customers' browsers. We'll test that the app works as desired from the customer's perspective: it'll drive a webbrowser through the site to order a pizza. Since we're not actually going to bake a pizza each time the test case is run, we'll verify that the order was successful by inspecting the backend database. Here's the outline as a JUnit test case:
```
import junit.framework.TestCase;

public class OrderPizzaTest extends TestCase {
  
  public void testOrderPizza() {
    // TODO
    // 1. open our pizza-ordering web app in a browser
    // 2. select a medium vegan pizza
    // 3. enter the delivery address
    // 4. click buy
    // 5. make sure the order shows up in the database
    fail("this test still needs to be written!");
  }
}
```
As is traditional with functional tests, there are many moving parts in even the simplest of use-cases. Before we can start writing the test's code, we need to launch an instance of the app to test against. We'll use a GuiceBerry environment to prepare our test's dependencies.

## Creating a GuiceBerry Environment ##
The **GuiceBerry environment** is a Guice module that prepares our application, its dependencies, and infrastructure for the test. In most applications, a single GuiceBerry environment is shared by all functional tests.

The primary job of the GuiceBerry environment is to bind an implementation for the `GuiceBerryEnvMain` interface. When this `Runnable`-like interface is bound, it will be executed before any tests.
```
import com.google.guiceberry.GuiceBerryEnvMain;
import com.google.guiceberry.GuiceBerryModule;
import com.google.inject.AbstractModule;

public class PizzaAppGuiceBerryEnv extends AbstractModule {

  @Override
  protected void configure() {
    install(new GuiceBerryModule());
    bind(GuiceBerryEnvMain.class).to(PizzaAppMain.class);
  }

  static class PizzaAppMain implements GuiceBerryEnvMain {
    public void run() {
      DatabaseRunner databaseRunner = new DatabaseRunner();
      databaseRunner.setPort(3306);
      databaseRunner.startAndWait(); // returns once the database is ready

      PizzaOrderApp pizzaOrderApp = new PizzaOrderApp();
      pizzaOrderApp.setDatabaseAddress("localhost", 3306);
      pizzaOrderApp.setHttpPort(80);
      pizzaOrderApp.startAndWait(); // returns once the web server is ready
    }
  }
}
```

## Linking the test to its GuiceBerry Environment ##
Once we've created our GuiceBerry environment, we need to configure our test to use it.

On JUnit4, this is done with the @Rule as below.

```
public class OrderPizzaTest {

  @Rule
  public final GuiceBerryRule guiceBerry =
      new GuiceBerryRule(PizzaAppGuiceBerryEnv.class);

  @Test
  public void testOrderPizza() {
    ...
    fail("this test still needs to be written!");
  }
}
```

## Executing the Guiceberry test ##
One nice aspect of Guiceberry tests is that they runs as regular JUnit test cases. You can run them from your build scripts, in your IDE, and in your continuous build without additional configuration. When executed, our test starts the database and webserver and then fails as expected.
```
starting database on 3306
starting pizza order app on 80

junit.framework.AssertionFailedError: this test still needs to be written!
	at com.publicobject.pizza.OrderPizzaTest.testOrderPizza(OrderPizzaTest.java:34)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
	at com.google.common.testing.junit3.TearDownTestCase.runBare(TearDownTestCase.java:106)
	at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:40)
```
To proceed, we'll need to actually browse through the web app from our test method. In the next section, [Injecting Tests](InjectingTests.md), we'll inject a WebDriver into our testcase.