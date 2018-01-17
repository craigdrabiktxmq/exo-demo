package io.swagger.api.factories;

import io.swagger.api.ZooApiService;
import io.swagger.api.impl.ZooApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-01-08T17:35:13.100Z")
public class ZooApiServiceFactory {
    private final static ZooApiService service = new ZooApiServiceImpl();

    public static ZooApiService getZooApi() {
        return service;
    }
}
