<h1>Release Notes</h2>

<!-- h2>Not yet released</h2 -->

<h2>GuiceBerry 4.2.0</h2>

* Make GuiceBerry compile with JDK 11 by moving the source and target versions
  to 1.6 (from 1.5): https://github.com/zorzella/guiceberry/issues/40

<h2>GuiceBerry 4.1.1</h2>

* Fix an issue introduced by the "GuiceBerry to fail fast if there are missing
  bindings" change that caused a change in the initialization order -- if there
  was a TestWrapper used, it would be provisioned before the
  GuiceBerryEnvMain.run method was run. This is particularly problematic if the
  GuiceBerryEnvMain is used to change some global static state that is then read
  by a singleton binding that is injected by the test wrapper, as that singleton
  would be built before the GuiceBerryEnvMain runs (i.e. before it is
  initialized).

<h2>GuiceBerry 4.1.0</h2>

* Create GuiceBerryTestRule -- a TestRule version of GuiceBerryRule

* Upgrade to apache common-collections-3.2.2

* GuiceBerry to fail fast if there are missing bindings

* Misc fixes (typos, polish etc)

<h2>GuiceBerry 4.0.0</h2>

* Replaced dependency to (deprecated) tl4j with a dependency to guava-testlibs. This required one change that is not strictly backwards-compatible, but really unlikely to break you. See details: https://github.com/zorzella/guiceberry/issues/27

<h2>GuiceBerry 3.3.2</h2>

* Moved to github
* Upgrade to guava 18
* Upgrade to junit 4.12

<h2>GuiceBerry 3.3.1</h2>

* remove bogus failing test

<h2>GuiceBerry 3.3.0</h2>

* bug fixes
* internal cleanup

<h2>GuiceBerry 3.2.0</h2>

* internal cleanup

<h2>GuiceBerry 3.1.0</h2>

* Upgraded many dependencies (Guice 3.0, Guava r09 etc)

<h2>GuiceBerry 3.0.0</h2>

* JUnit4 and TestNG support
* Paradigm shift: it's now possible (and preferred) to use GuiceBerry without the GuiceBerryEnv annotation
* Many deprecations
* Cleanup and modularization
* If you want to upgrade from the ancient version 2.0, see http://docs.google.com/document/pub?id=1IanQDC2-IEVtSViVirniEpdhTGZ8V6bTeo0DiBxjQts
* Guava r06 replacing the Google Collects jar (this should have no impact on you)

