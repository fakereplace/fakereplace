package a.org.fakereplace.integration.jbossas.clientproxy.remoteejb;

import javax.ejb.Remote;

/**
 * @author Stuart Douglas
 */
@Remote
public interface RemoteInterface {

    String getValue();

    void setValue(String value);
}
