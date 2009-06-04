package tutorial_1_server;

public interface Tutorial1Envs {

  String PACKAGE = "tutorial_1_server.";

  String PET_STORE_ENV_0_SIMPLE = PACKAGE + 
    "PetStoreEnv0Simple";
  
  // Example 1 uses the same env as example 0
  
  String PET_STORE_ENV_2_GLOBAL_STATIC_POTM = PACKAGE + 
    "PetStoreEnv2GlobalStaticControllablePotm";
  
  String PET_STORE_ENV_3_COOKIES_BASED_POTM = PACKAGE + 
    "PetStoreEnv3CookiesControlledPotm";
}
