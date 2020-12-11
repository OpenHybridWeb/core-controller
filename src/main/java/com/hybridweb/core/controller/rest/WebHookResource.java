package com.hybridweb.core.controller.rest;

import com.hybridweb.core.controller.staticcontent.StaticContentController;
import com.hybridweb.core.controller.website.WebsiteConfigService;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Path("/_controller/api/webhook/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WebHookResource {

    public static final String CONTEXT = "/_controller/api/webhook/";

    @Inject
    WebsiteConfigService websiteConfigService;

    @Inject
    StaticContentController staticContentController;

    @GET
    @Path("")
    public static List<String> apis() {
        List<String> apis = new ArrayList<>();
        apis.add(CONTEXT + "website");
        apis.add(CONTEXT + "component/{name}");
        return apis;
    }

    @GET
    @Path("website")
    public String websiteHook() throws GitAPIException, IOException {
        // TODO: Change to POST and consume webhook call from github etc.
        // Check if the web.yaml has changed
        websiteConfigService.reload();
        return "DONE";
    }

    @GET
    @Path("component/{name}")
    public String componentHook(@PathParam("name") String name) throws GitAPIException, IOException {
        staticContentController.refreshComponent(name);
        return "DONE";
    }


}
