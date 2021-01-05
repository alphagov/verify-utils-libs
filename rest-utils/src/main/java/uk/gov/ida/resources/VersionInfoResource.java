package uk.gov.ida.resources;

import uk.gov.ida.common.CommonUrls;
import uk.gov.ida.common.VersionInfoDto;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

// See http://stackoverflow.com/questions/1272648/reading-my-own-jars-manifest for implementation details.
@Path(CommonUrls.VERSION_INFO_ROOT)
@Produces(MediaType.APPLICATION_JSON)
public class VersionInfoResource {

    @GET
    public VersionInfoDto getVersionInfo() {
        Attributes manifest = getManifest();
        String buildNumber = manifest.getValue("Build-Number");
        String gitCommit = manifest.getValue("Git-Commit");
        String buildTimestamp = manifest.getValue("Build-Timestamp");

        return new VersionInfoDto(buildNumber, gitCommit, buildTimestamp);
    }

    private Attributes getManifest() {
        ClassLoader cl = getClass().getClassLoader();
        Manifest manifest;
        try {
            URL url = cl.getResource("META-INF/MANIFEST.MF");
            manifest = new Manifest(url.openStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return manifest.getMainAttributes();
    }

}
