# Motivation #

Writing good tests is an important step in writing good programs. Automated tests help to discover problems early in the development process. They also give confidence to improve and refactor code as the application grows.

In Java land, there are well established tools for **unit testing**. [JUnit](http://www.junit.org/) and [TestNG](http://testng.org/) are simple frameworks that make it easy to test the isolated units of an application. In traditional use of these frameworks, each test validates the behaviour of a single class or small group of classes. Test structure is often very simple: instantiate the objects under test, then probe them by invoking their methods.

Unit testing is necessary, but not sufficient for verifying the correctness of an application. In **functional testing**, complete groups of behaviour are tested in aggregate. These tests ensure that the composed units interact as designed and that the application functions as intended. Test structure for functional testing can be complicated: servers, databases, configuration and sample data may all be required for a single test.

GuiceBerry makes it easier to write functional tests by borrowing idioms from unit testing and dependency injection. Each GuiceBerry test specifies the environment in which it runs: this is the application under test plus application dependencies and test infrastructure. GuiceBerry tests gain access to their environment via [Guice injection](http://code.google.com/p/google-guice/).

[Getting Started](GettingStarted.md) will walk through writing a new GuiceBerry test.