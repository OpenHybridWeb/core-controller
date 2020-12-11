package com.hybridweb.core.controller.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/_controller/api/")
@Produces(MediaType.APPLICATION_JSON)
public class ApiRootResource {

    @GET
    @Path("")
    public List<String> apis() {
        List<String> apis = WebHookResource.apis();
        return apis;
    }

}
