package tutorial_1_server;

public interface Tutorial1Envs {

  String PACKAGE = "com.google.inject.testing.guiceberry.tutorial_1_server.";

  String REGULAR_PET_STORE_AT_8080_ENV = PACKAGE + 
    "prod_0_simple.RegularPetStoreAt8080Env";
  
  String MANUAL_CONTROLLABLE_INJECTION_PET_STORE_AT_8080_ENV = PACKAGE + 
    "prod_1_manual_controllable_injection.ManualCIPetStoreAt8080Env";
  
  String MANUAL_CONTROLLABLE_INJECTION_THROUGH_COOKIE_PET_STORE_AT_8080_ENV = PACKAGE + 
    "prod_2_manual_controllable_injection.ManualCIWithCookiesPetStoreAt8080Env";
}
