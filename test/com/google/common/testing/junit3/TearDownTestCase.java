package com.google.common.testing.junit3;

import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.common.testing.TearDownStack;

import junit.framework.TestCase;

/**
 * A private copy of TestLibrariesForJava's TearDownTestCase, so as to avoid
 * a dependency on that project just because of our tests and tutorial.
 */
public abstract class TearDownTestCase extends TestCase implements TearDownAccepter {

  /**
   * Creates a TearDownTestCase with the default (empty) name.
   */
  public TearDownTestCase() {}

  /**
   * Creates a TearDownTestCase with the specified name.
   */
  public TearDownTestCase(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  final TearDownStack stack = new TearDownStack(true);

  /**
   * Registers a TearDown implementor which will be run during {@link #tearDown()}
   */
  public final void addTearDown(TearDown tearDown) {
    stack.addTearDown(tearDown);
  }

  @Override protected final void tearDown() {
    stack.runTearDown();
  }

  // Override to run setUp() inside the try block, not outside
  @Override public final void runBare() throws Throwable {
    try {
      setUp();
      runTest();
    } finally {
      tearDown();
    }
  }
}
