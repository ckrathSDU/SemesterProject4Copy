package dk.sdu.mmmi.common.data;

import java.io.Serializable;
import java.util.UUID;

public class Asset implements Serializable {

    private final UUID ID = UUID.randomUUID();

    private String host;
    private String port;
    private String request;

    public Asset(String host, String port, String request) {
        this.host = host;
        this.port = port;
        this.request = request;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getID() {
        return ID.toString();
    }
}
