package io.petstore.extensions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.petstore.dto.pet.PetDTO;
import io.petstore.modules.GuiceModule;
import io.petstore.services.pet.PetServiceApi;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import java.util.*;

public class PetExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback {

    private GuiceModule guiceModule;
    private Injector guiceInjector;

    private PetServiceApi petServiceApi;

    private List<Map<Integer, PetDTO>> petDTOList = new ArrayList<>();
    private Integer startId = 10;
    private Integer endId = 15;

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        extensionContext.getTestInstance().ifPresent(instance -> guiceInjector.injectMembers(instance));
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        guiceModule = new GuiceModule();
        guiceInjector = Guice.createInjector(guiceModule);
        petServiceApi = guiceInjector.getInstance(PetServiceApi.class);

        guiceInjector.injectMembers(this);

        saveTestDataBeforeTests(startId, endId);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        returnTestDataAfterTests();
    }

    private void saveTestDataBeforeTests(int startId, int endId) {
        for (int id = startId; id <= endId; id++) {
            ValidatableResponse rsPet = petServiceApi.getPet(id);
            switch (rsPet.statusCode(anyOf(is(200), is(404))).extract().statusCode()) {
                case 200 -> petDTOList.add(Map.of(id, rsPet.extract().body().as(PetDTO.class)));
                case 404 -> {
                    Map<Integer, PetDTO> petDTOMap = new HashMap<>();
                    petDTOMap.put(id, null);
                    petDTOList.add(petDTOMap);
                }
            }
        }
    }

    private void returnTestDataAfterTests() {
        petDTOList.stream().forEach(petDTOMap -> {
            assertThat(petDTOMap.keySet().size())
                    .as(String.format("Размер Map равен '%d', а должен быть '1'", petDTOMap.keySet().size()))
                    .isEqualTo(1);
            petDTOMap.entrySet().stream().forEach(
                    entry -> {
                        if (entry.getValue() == null) {
                            switch (petServiceApi.getPet(entry.getKey()).statusCode(anyOf(is(200), is(404))).extract().statusCode()) {
                                case 200 -> petServiceApi.deletePet(entry.getKey()).statusCode(200);
                                case 404 -> petServiceApi.deletePet(entry.getKey()).statusCode(404);
                            }
                        } else {
                            petServiceApi.postPet(entry.getValue()).statusCode(200);
                        }
                    }
            );
        });
    }
}
