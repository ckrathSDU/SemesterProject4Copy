package dk.sdu.mmmi.agv;

import dk.sdu.mmmi.common.data.Asset;
import dk.sdu.mmmi.common.data.AssetManager;
import dk.sdu.mmmi.commonAgv.AGV;
import dk.sdu.mmmi.commonAgv.AGVSPI;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import dk.sdu.mmmi.common.services.ICommunicatorService;

import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.json.*;


@Component
@ComponentScan
public class AGVControlSystem implements ICommunicatorService, AGVSPI {

    @Override
    public void performOperation(String programName, AssetManager assetManager) {
        for (Asset asset : assetManager.getAssets(AGV.class)) {

            String data = "{\"Program name\": \"" + programName + "\", " + "\"State\": " + "\"1\"" + "}";
            execute(data, asset);
            System.out.println("AGV Executing: " + data);

            //Have to send state 2 in order for the AGV to execute (order 66)
            String data2 = "{\"State\": " + "\"2\"" + "}";
            execute(data2, asset);
        }
    }

    @Override
    public String getStatus(AssetManager assetManager) {
        for (Asset asset : assetManager.getAssets(AGV.class)) {
            String agvAPIUrl = "http://" + asset.getHost() + ":" + asset.getPort() + asset.getRequest();
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .GET().header("accept", "application/json")
                    .uri(URI.create(agvAPIUrl)).build();
            try {
                CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
                try {
                    return response.get().body();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void execute(String payload, Asset asset){
        String agvAPIUrl = "http://" + asset.getHost() + ":" + asset.getPort() + asset.getRequest();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(agvAPIUrl))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpClient client = HttpClient.newHttpClient();

        // CompletableFuture<HttpResponse<String>> response = null;
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

    }


    @Override
    public boolean isReady(AssetManager assetManager) {
        return getState(assetManager) == 1;
    }

    @Override
    public int getState(AssetManager assetManager) {
        String statusResponse = getStatus(assetManager);

        JSONObject json;
        String stateJson = null;
        try {
            json = new JSONObject(statusResponse);
            stateJson = json.getString("state");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert stateJson != null;
        return Integer.parseInt(stateJson);
    }
}
