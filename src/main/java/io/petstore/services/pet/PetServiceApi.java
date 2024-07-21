package io.petstore.services.pet;

import static io.restassured.RestAssured.given;

import io.petstore.dto.pet.PetDTO;
import io.petstore.services.BaseServiceApiAbstract;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import java.io.File;

public class PetServiceApi extends BaseServiceApiAbstract {

    public PetServiceApi() {
        super();
        this.basePath = "/pet";
        spec(this.baseApiUrl, this.basePath);
    }

    public ValidatableResponse getPet(long id) {
        return given()
                .basePath(this.basePath + "/" + id)
                .when()
                .get()
                .then()
                .log().all();
    }

    public ValidatableResponse postPet(PetDTO petDTO) {
        return given()
                .body(petDTO)
                .when()
                .post()
                .then()
                .log().all();
    }

    public ValidatableResponse putPet(PetDTO petDTO) {
        return given()
                .body(petDTO)
                .when()
                .put()
                .then()
                .log().all();
    }

    public ValidatableResponse postPetViaForm(long id, String name, String status) {
        return given()
                .basePath(this.basePath + "/" + id)
                .contentType("application/x-www-form-urlencoded; charset=utf-8")
                .formParam("name", name)
                .formParam("status", status)
                .header("Accept-Charset", "UTF-8")
                .header("Accept-Encoding", "gzip, deflate, br")
                .when()
                .post()
                .then()
                .log().all();
    }

    public ValidatableResponse deletePet(long id) {
        return given()
                .basePath(this.basePath + "/" + id)
                .when()
                .delete()
                .then()
                .log().all();
    }

    public ValidatableResponse postUploadImage(long id, String additionalMetadata, File file) {
        return given()
                .basePath(this.basePath + "/" + id + "/uploadImage")
                .contentType(ContentType.MULTIPART)
                .formParam("additionalMetadata", additionalMetadata)
                .multiPart(file)
                .when()
                .post()
                .then()
                .log().all();
    }

    public ValidatableResponse postUploadImage(long id, File file) {
        return given()
                .basePath(this.basePath + "/" + id + "/uploadImage")
                .contentType(ContentType.MULTIPART)
                .multiPart(file)
                .when()
                .post()
                .then()
                .log().all();
    }
}