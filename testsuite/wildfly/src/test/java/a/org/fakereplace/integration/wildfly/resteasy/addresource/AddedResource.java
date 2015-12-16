package a.org.fakereplace.integration.wildfly.resteasy.addresource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author Stuart Douglas
 */
@Path("/added")
public class AddedResource {

    @GET
    public String get() {
        return "added";
    }

}
