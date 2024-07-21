package io.petstore.pet;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.github.javafaker.Faker;
import com.google.inject.Inject;
import io.petstore.dto.inner.parts.Category;
import io.petstore.dto.inner.parts.TagDTO;
import io.petstore.dto.pet.PetDTO;
import io.petstore.dto.rp.RPCodeTypeMessageDTO;
import io.petstore.extensions.PetExtension;
import io.petstore.services.pet.PetServiceApi;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

@ExtendWith(PetExtension.class)
@Tag("@homework3")
public class PetTest {
    @Inject
    private Faker faker;
    @Inject
    private PetServiceApi petServiceApi;

    @Test
    @DisplayName("Проверить запросы: POST, PUT, GET, DELETE + '/pet' и их body")
    @Tag("@method0")
    @Tag("@method00test0")
    public void postGetDeletePetCompareRqBodyAndRsBody() {
        /*
            Step 1   - сравнить DTO объекта rqPostPetDTO с rpGetPetDTO  POST+GET
            Step 1.1 - проверить Pet методом Get
            Step 2   - сравнить DTO объекта rqPutPetDTO с rpGetPetDTO,
                       проверить генерацию  объекта, отличающегося от POST запроса PUT+GET
            Step 2.1 - проверить Pet через Get
            Step 3   - проверить удаление DTO DELETE+GET
            Step 3.1 - проверить Pet через Get (зачистка)
        */

        long id = 11L;
        // Step 1 - сравнить DTO объекта rqPostPetDTO с rpGetPetDTO (POST+GET)
        PetDTO rqPostPetDTO = createNewPet(id);
        ValidatableResponse rsPost = petServiceApi.postPet(rqPostPetDTO);
        rsPost.statusCode(200);

        // Step 1.1 - проверить Pet через Get
        ValidatableResponse rsGet = petServiceApi.getPet(id);
        rsGet.statusCode(200);
        PetDTO rpGetPetDTO = rsGet.extract().body().as(PetDTO.class);
        comparePetDTO(rpGetPetDTO, rqPostPetDTO, "rpGetPetDTO00", "rqPostPetDTO");

        // Step 2 - сравнить DTO объекта rqPutPetDTO с rpGetPetDTO,
        // проверить генерацию объекта, отличающегося от POST запроса PUT+GET
        PetDTO rqPutPetDTO = createNewPet(id);
        assertFalse(rqPutPetDTO.equals(rqPostPetDTO), "rqPostPetDTO is equal to rqPutPetDTO. Need different DTO");
        ValidatableResponse rsPut = petServiceApi.putPet(rqPutPetDTO);
        rsPut.statusCode(200);

        // Step 2.1 - проверить Pet через Get
        rsGet = petServiceApi.getPet(id);
        rsGet.statusCode(200);
        rpGetPetDTO = rsGet.extract().body().as(PetDTO.class);
        comparePetDTO(rpGetPetDTO, rqPutPetDTO, "rpGetPetDTO01", "rqPutPetDTO");

        // Step 3 - проверить удаление DTO DELETE+GET
        ValidatableResponse rsDelete = petServiceApi.deletePet(id);
        rsDelete.statusCode(200)
                .body(matchesJsonSchemaInClasspath("schema/rs/RSCodeTypeMessageSchema.json"));
        RPCodeTypeMessageDTO rpDeleteBody = rsDelete.extract().body().as(RPCodeTypeMessageDTO.class);
        Assertions.assertAll("rsCodeTypeMessageDTO",
                () -> assertEquals(200L, rpDeleteBody.getCode()),
                () -> assertEquals("unknown", rpDeleteBody.getType()),
                () -> assertEquals(Long.toString(id), rpDeleteBody.getMessage())
        );

        // Step 3.1 - зачистка (проверить Pet через Get)
        rsGet = petServiceApi.getPet(id);
        rsGet.statusCode(404)
                .body(matchesJsonSchemaInClasspath("schema/rs/RSCodeTypeMessageSchema.json"));
        RPCodeTypeMessageDTO rpGetErrorBody = rsGet.extract().body().as(RPCodeTypeMessageDTO.class);
        Assertions.assertAll("rsCodeTypeMessageDTO",
                () -> assertEquals(1, rpGetErrorBody.getCode()),
                () -> assertEquals("error", rpGetErrorBody.getType()),
                () -> assertEquals("Pet not found", rpGetErrorBody.getMessage())
        );

    }

    @Test
    @DisplayName("Проверить загрузку изображения методом '/pet/{id}/uploadImage' c 'additionalMetadata'")
    @Tag("@method01")
    @Tag("@method01test01")
    public void postUploadImage() {
        /*
            Создать объект Pet (POST /pet + 'body'),
            Отправить изображение (POST /pet/{id}/uploadImage),
            Провепить ответ на (POST /pet/{id}/uploadImage)
        */
        long id = 12L;
        String metaData = "MyMetaData";
        File file = new File("src/main/resources/test.data/images/testFotoPet.jpg");
        String fileName = "/testFotoPet.jpg";

        petServiceApi.postPet(createNewPet(id)).statusCode(200);

        ValidatableResponse rs = petServiceApi.postUploadImage(id, metaData, file);

        rs.statusCode(200)
                .body(matchesJsonSchemaInClasspath("schema/rs/RSCodeTypeMessageSchema.json"));

        RPCodeTypeMessageDTO rpCodeTypeMessageDTO = rs.extract().body().as(RPCodeTypeMessageDTO.class);
        Assertions.assertAll("rsCodeTypeMessageDTO",
                () -> Assertions.assertEquals(200L, rpCodeTypeMessageDTO.getCode()),
                () -> Assertions.assertEquals("unknown", rpCodeTypeMessageDTO.getType()),
                () -> Assertions.assertEquals(
                        String.format("additionalMetadata: %s\nFile uploaded to .%s, %s bytes", metaData, fileName, file.length()),
                        rpCodeTypeMessageDTO.getMessage()
                )
        );
    }

    @Test
    @DisplayName("Проверить загрузку изображения  посредством метода '/pet/{id}/uploadImage' без 'additionalMetadata'")
    @Tag("@method01")
    @Tag("@method01test02")
    public void postUploadImageNoAdditionalMetadata() {
        // Проверить необязательность поля 'additionalMetadata'

        /*
            Создать объект Pet (POST /pet + 'body'),
            Отправить изображение (POST /pet/{id}/uploadImage)
             через перезагруженный метод без поля 'additionalMetadata',
            Проверить ответ на запрос от (POST /pet/{id}/uploadImage)
        */

        long id = 13L;
        File file = new File("src/main/resources/test.data/images/testFotoPet.jpg");
        String fileName = "/testFotoPet.jpg";

        petServiceApi.postPet(createNewPet(id)).statusCode(200);

        ValidatableResponse rs = petServiceApi.postUploadImage(id, file);

        rs.statusCode(200)
                .body(matchesJsonSchemaInClasspath("schema/rs/RSCodeTypeMessageSchema.json"));

        RPCodeTypeMessageDTO rpCodeTypeMessageDTO = rs.extract().body().as(RPCodeTypeMessageDTO.class);
        Assertions.assertAll("rsCodeTypeMessageDTO",
                () -> Assertions.assertEquals(200L, rpCodeTypeMessageDTO.getCode()),
                () -> Assertions.assertEquals("unknown", rpCodeTypeMessageDTO.getType()),
                () -> Assertions.assertEquals(
                        String.format("additionalMetadata: null\nFile uploaded to .%s, %s bytes", fileName, file.length()),
                        rpCodeTypeMessageDTO.getMessage()
                )
        );
    }

    @Test
    @DisplayName("200 при замене изображения текстом '/pet/{id}/uploadImage'")
    @Tag("@method02")
    @Tag("@method02test03")
    public void postUploadImageButThisIsText() throws IOException{
        // Проверить необязательность поля 'additionalMetadata'

        /*
            Создать объект Pet (POST /pet + 'body'),
            Заменить изображение текстом (POST /pet/{id}/uploadImage),
            Проверить ответ на (POST /pet/{id}/uploadImage)
        */

        long id = 14L;
        String metaData = "MyMetaData";
        String fileName = "/testFileMethod02test03.txt";
        File file = getTestTextFile(fileName);

        petServiceApi.postPet(createNewPet(id)).statusCode(200);

        ValidatableResponse rs = petServiceApi.postUploadImage(id, metaData, file);

        rs.statusCode(200)
                .body(matchesJsonSchemaInClasspath("schema/rs/RSCodeTypeMessageSchema.json"));

        RPCodeTypeMessageDTO rpCodeTypeMessageDTO = rs.extract().body().as(RPCodeTypeMessageDTO.class);
        Assertions.assertAll("rsCodeTypeMessageDTO",
                () -> Assertions.assertEquals(200L, rpCodeTypeMessageDTO.getCode()),
                () -> Assertions.assertEquals("unknown", rpCodeTypeMessageDTO.getType()),
                () -> Assertions.assertEquals(
                        String.format("additionalMetadata: %s\nFile uploaded to .%s, %s bytes", metaData, fileName, file.length()),
                        rpCodeTypeMessageDTO.getMessage()
                )
        );
        Assertions.assertTrue(file.delete(), String.format("Файл '%s' не удалён", file.getAbsolutePath()));
    }

    @Test
    @DisplayName("200 для удаленного Pet при отпракке изображения в методе '/pet/{id}/uploadImage'")
    @Tag("@method02")
    @Tag("@method02test04")
    public void postUploadImageButPetWasDeleted() {
        // Проверить необязательность поля 'additionalMetadata'

        /*
            Создать объект Pet (POST /pet + 'body'),
            Удалить объект Pet (DELETE /pet/{id})
            Отправить изображение (POST /pet/{id}/uploadImage),
            Проверить ответ на (POST /pet/{id}/uploadImage)
        */

        long id = 15L;
        String metaData = "MyMetaData";
        File file = new File("src/main/resources/test.data/images/testPhotoBRA.PNG");
        String fileName = "/testPhotoBRA.PNG";

        petServiceApi.postPet(createNewPet(id)).statusCode(200);
        petServiceApi.deletePet(id).statusCode(200);

        ValidatableResponse rs = petServiceApi.postUploadImage(id, metaData, file);

        rs.statusCode(200)
                .body(matchesJsonSchemaInClasspath("schema/rs/RSCodeTypeMessageSchema.json"));

        RPCodeTypeMessageDTO rpCodeTypeMessageDTO = rs.extract().body().as(RPCodeTypeMessageDTO.class);
        Assertions.assertAll("rsCodeTypeMessageDTO",
                () -> Assertions.assertEquals(200L, rpCodeTypeMessageDTO.getCode()),
                () -> Assertions.assertEquals("unknown", rpCodeTypeMessageDTO.getType()),
                () -> Assertions.assertEquals(
                        String.format("additionalMetadata: %s\nFile uploaded to .%s, %s bytes", metaData, fileName, file.length()),
                        rpCodeTypeMessageDTO.getMessage()
                )
        );
    }

    private PetDTO createNewPet(Long id) {
        return PetDTO.builder()
                .id(id)
                .category(new Category(faker.random().nextLong(), faker.name().firstName()))
                .name(faker.name().firstName())
                .photoUrls(Arrays.asList("photoUrl1", "photoUrl2"))
                .tags(Arrays.asList(
                        new TagDTO(faker.random().nextLong(), faker.name().firstName()),
                        new TagDTO(faker.random().nextLong(), faker.name().firstName())
                ))
                .status("sold")
                .build();
    }

    private void comparePetDTO(PetDTO petDTO1, PetDTO petDTO2, String petDTO1Name, String petDTO2Name) {
        assertThat(petDTO1)
                .as(String.format("DTO '%s' is not equals DTO '%s'", petDTO1Name, petDTO2Name))
                .isEqualTo(petDTO2);
    }

    private File getTestTextFile(String fileNameAndExtension) throws IOException{
        File file = new File(System.getProperty("project.base.dir") + "\\target" + fileNameAndExtension);
        try(FileWriter writer = new FileWriter(file, false)) {
            writer.write(faker.country().name());
            writer.flush();
        }
        return file;
    }
}
