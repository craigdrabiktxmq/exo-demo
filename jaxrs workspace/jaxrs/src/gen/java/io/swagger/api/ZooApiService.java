package io.swagger.api;

import io.swagger.api.*;
import io.swagger.model.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import io.swagger.model.Animal;

import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-01-08T17:35:13.100Z")
public abstract class ZooApiService {
    public abstract Response addAnimal(Animal animal,SecurityContext securityContext) throws NotFoundException;
    public abstract Response getZoo(SecurityContext securityContext) throws NotFoundException;
}
