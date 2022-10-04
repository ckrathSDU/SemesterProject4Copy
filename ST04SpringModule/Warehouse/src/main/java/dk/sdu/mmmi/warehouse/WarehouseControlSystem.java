package dk.sdu.mmmi.warehouse;

import dk.sdu.mmmi.common.data.AssetManager;
import dk.sdu.mmmi.commonWarehouse.WarehouseSPI;
import dk.sdu.mmmi.warehouse.SOAPMethods.IEmulatorService;
import dk.sdu.mmmi.warehouse.SOAPMethods.IEmulatorService_Service;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import dk.sdu.mmmi.common.services.ICommunicatorService;

import org.json.*;

import java.util.Arrays;

@Component
@ComponentScan
public class WarehouseControlSystem implements ICommunicatorService, WarehouseSPI {

    IEmulatorService_Service emulatorService_service = new IEmulatorService_Service();
    IEmulatorService warehouseService = emulatorService_service.getBasicHttpBindingIEmulatorService();

    @Override
    public void performOperation(String s, AssetManager assetManager) {
        //Required format "MethodName,<Integer>,<String>"

        String[] arrOfStr = s.split(",");

        System.out.println("Warehouse Executing: " + Arrays.toString(arrOfStr));

        if(arrOfStr[0].equals("InsertItem")){
            warehouseService.insertItem(Integer.parseInt(arrOfStr[1]), arrOfStr[2]);
        } else if (arrOfStr[0].equals("PickItem")){
            warehouseService.pickItem(Integer.parseInt(arrOfStr[1]));
        }
    }

    @Override
    public boolean isReady(AssetManager assetManager) {
        return getState(assetManager) == 0;
    }

    @Override
    public String getStatus(AssetManager assetManager) {
        return warehouseService.getInventory();
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
}
