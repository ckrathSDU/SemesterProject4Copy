package dk.sdu.mmmi.assemblystation;

import dk.sdu.mmmi.common.data.AssetManager;
import dk.sdu.mmmi.commonAssemblyStation.AssemblyStationSPI;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import dk.sdu.mmmi.common.services.ICommunicatorService;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.json.*;


@Component
@ComponentScan
public class AssemblyStationControlSystem  implements ICommunicatorService, AssemblyStationSPI {

    private MqttAsyncClient client = null;

    private String message = "";

    final Lock lock = new ReentrantLock();
    final Condition notSet  = lock.newCondition();

    public void init(){
        try {
            client = new MqttAsyncClient("tcp://127.0.0.1:1883", "");

            IMqttToken token = client.connect();
            token.waitForCompletion();

            try {
                /*Subscribe to topic*/
                client.subscribe("emulator/status", 0);
            } catch (MqttException e) {
                e.printStackTrace();
            }

            messageStatus();

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void performOperation(String s, AssetManager assetManager) {
        if(client == null){
            init();
        }

        /*Publish messages to topic*/
        try {
            MqttMessage message = new MqttMessage();

            System.out.println("Assembly Executing ProcessID: " + s);

            String payload = "{\"ProcessID\": " + s + "}";
            message.setPayload(payload.getBytes(StandardCharsets.UTF_8));
            client.publish("emulator/operation", message);


        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isReady(AssetManager assetManager) {
        return getState(assetManager) == 0;
    }

    @Override
    public String getStatus(AssetManager assetManager){
        if(client == null){
            init();
        }

        lock.lock();
        try {
            while (message.equals("")) {
                try {
                    notSet.await();
                } catch (InterruptedException ignored){}
            }
        } finally{
            lock.unlock();
        }
        return getMessage();
    }

    @Override
    public int getState(AssetManager assetManager) {
        String statusResponse = getStatus(assetManager);

        JSONObject json;
        String stateJson = null;
        try {
            json = new JSONObject(statusResponse);
            stateJson = json.getString("State");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert stateJson != null;
        return Integer.parseInt(stateJson);
    }


    public void messageStatus(){
        assert client != null;
        client.setCallback(new MqttCallback() {

            @Override
            public void connectionLost(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                setMessage(mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                try {
                    iMqttDeliveryToken.getMessage();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message){
        lock.lock();

        try {
            this.message = message;
            notSet.signal();
        } finally {
            lock.unlock();
        }
    }

}
