package com.hybridweb.core.controller.rest;

import com.hybridweb.core.controller.staticcontent.StaticContentController;
import com.hybridweb.core.controller.website.WebsiteConfigService;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.io.IOException;

@Path("/_controller/api/github/")
public class GithubHookResource {

    @Inject
    WebsiteConfigService websiteConfigService;

    @Inject
    StaticContentController staticContentController;

    @GET
    @Path("")
    public String root() throws GitAPIException, IOException {
        return "Github API";
    }

    @GET
    @Path("website")
    public String websiteHook() throws GitAPIException, IOException {
        websiteConfigService.redeploy();
        return "DONE";
    }

    @GET
    @Path("component/{name}")
    public String componentHook(@PathParam("name") String name) throws GitAPIException, IOException {
        staticContentController.refreshComponent(name);
        return "DONE";
    }


}
