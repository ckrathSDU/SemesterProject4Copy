package dk.sdu.mmmi.coreinit;

import dk.sdu.mmmi.assemblystation.AssemblyStationControlSystem;
import dk.sdu.mmmi.common.data.AssetManager;
import dk.sdu.mmmi.common.services.IAssetManagerPluginService;
import dk.sdu.mmmi.productionManager.MesSpringClient;
import dk.sdu.mmmi.warehouse.WarehouseControlSystem;
import javafx.application.Application;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CoreInitApplicationTests {
    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    AssetManager assetManager;

    @Autowired
    MesSpringClient productionManager;

    @Autowired
    WarehouseControlSystem warehouseControlSystem;

    @Autowired
    AssemblyStationControlSystem assemblyStationControlSystem;

    @Test
    @Order(1)
    void contextLoads() {
        assertNotNull(applicationContext);
        AssetManager assetManager = applicationContext.getBean(AssetManager.class);

        for (Map.Entry<String, IAssetManagerPluginService> iAssetManagerPluginServiceEntry : applicationContext.getBeansOfType(IAssetManagerPluginService.class).entrySet()) {
            System.out.println(iAssetManagerPluginServiceEntry.getValue());
            iAssetManagerPluginServiceEntry.getValue().start(assetManager);
        }
    }

    @Test
    @Order(2)
    void testStartProduction() throws InterruptedException, JSONException {
        TreeMap<Integer, String> productionSequence = new TreeMap<>();
        productionSequence.put(1, "Warehouse:PickItem,1");
        productionSequence.put(2, "Warehouse:InsertItem,1,Drone");
        productionSequence.put(3, "AssemblyStation:1004");
        productionManager.startProduction(productionSequence);
        Thread.sleep(1000);
        JSONObject json = new JSONObject(warehouseControlSystem.getStatus(assetManager));
        JSONArray arrJson = json.getJSONArray("Inventory");
        JSONObject inventoryItem = new JSONObject(arrJson.getString(0));
        String content = inventoryItem.get("Content").toString();
        assertTrue(content.equals("Drone"));

        JSONObject json2 = new JSONObject(assemblyStationControlSystem.getStatus(assetManager));
        String lastOp = json2.get("CurrentOperation").toString();
        assertTrue(lastOp.equals("1004"));
 }

}
