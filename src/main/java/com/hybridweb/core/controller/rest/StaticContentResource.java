package com.hybridweb.core.controller.rest;

import com.hybridweb.core.controller.staticcontent.StaticContentController;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/_controller/api/staticcontent/")
@Produces(MediaType.APPLICATION_JSON)
public class StaticContentResource {

    public static final String CONTEXT = "/_controller/api/staticcontent/";

    @Inject
    StaticContentController staticContentController;

    @GET
    @Path("")
    public static List<String> apis() {
        List<String> apis = new ArrayList<>();
        apis.add(CONTEXT + "components");
        apis.add(CONTEXT + "update/{name}");
        return apis;
    }

    @GET
    @Path("components")
    public Uni<JsonObject> listComponents() {
        return staticContentController.listComponents();
    }

    @GET
    @Path("update/{name}")
    public Uni<JsonObject> componentHook(@PathParam("name") String name) {
        return staticContentController.refreshComponent(name);
    }


}
