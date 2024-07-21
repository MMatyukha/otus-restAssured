package io.petstore.services;

import io.petstore.specs.BaseSpec;
import io.restassured.specification.RequestSpecification;

public abstract class BaseServiceApiAbstract extends BaseSpec{
    protected String baseApiUrl;
    protected String basePath;
    private RequestSpecification requestSpec;

    protected BaseServiceApiAbstract() {
        this.baseApiUrl = System.getProperty("base.api.url");
    }

    protected void spec(String baseApiURL, String basePath) {
        this.requestSpec = this.requestSpec(baseApiURL, basePath);
        this.installSpec(this.requestSpec, this.responseSpec());
    }
}