package io.petstore.modules;

import com.github.javafaker.Faker;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.petstore.services.pet.PetServiceApi;

public class GuiceModule extends AbstractModule {
    private Faker faker = new Faker();
    private PetServiceApi petServiceApi = new PetServiceApi();

    @Provides
    public Faker getFaker() { return faker; }

    @Provides
    public PetServiceApi getPetServiceApi() { return petServiceApi; }
}
