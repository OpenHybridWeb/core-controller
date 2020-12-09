package com.hybridweb.core.controller.rest;

import com.hybridweb.core.controller.website.WebsiteConfigService;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.IOException;

@Path("/_api/github/")
public class GithubHookResource {

    @Inject
    WebsiteConfigService websiteConfigService;


    @GET
    @Path("websiteredeploy")
    public String websiteHook() throws GitAPIException, IOException {
        websiteConfigService.redeploy();
        return "DONE";
    }

}
