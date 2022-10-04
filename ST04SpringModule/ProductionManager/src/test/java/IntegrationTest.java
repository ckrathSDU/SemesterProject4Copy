import dk.sdu.mmmi.agv.AGVControlSystem;
import dk.sdu.mmmi.agv.AGVPlugin;
import dk.sdu.mmmi.assemblystation.AssemblyStationControlSystem;
import dk.sdu.mmmi.assemblystation.AssemblyStationPlugin;
import dk.sdu.mmmi.common.data.Asset;
import dk.sdu.mmmi.common.data.AssetManager;
import dk.sdu.mmmi.commonAgv.AGV;
import dk.sdu.mmmi.commonAssemblyStation.AssemblyStation;
import dk.sdu.mmmi.persistence.PersistenceControlSystem;
import dk.sdu.mmmi.persistence.PersistencePlugin;
import dk.sdu.mmmi.productionManager.MesSpringClient;
import dk.sdu.mmmi.warehouse.WarehouseControlSystem;
import dk.sdu.mmmi.warehouse.WarehousePlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.annotation.AfterTestMethod;

import java.util.Collection;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntegrationTest {
    /*

    @Autowired
    static AssetManager assetManager = new AssetManager();

    @Autowired
    static AGVPlugin agvPlugin = new AGVPlugin();

    @Autowired
    static WarehousePlugin warehousePlugin = new WarehousePlugin();

    @Autowired
    static AssemblyStationPlugin assemblyStationPlugin = new AssemblyStationPlugin();

    @Autowired
    static PersistencePlugin persistencePlugin = new PersistencePlugin();

    @Autowired
    AGVControlSystem agvControlSystem = new AGVControlSystem();

    @Autowired
    WarehouseControlSystem warehouseControlSystem = new WarehouseControlSystem();

    @Autowired
    AssemblyStationControlSystem assemblyStationControlSystem = new AssemblyStationControlSystem();

    @Autowired
    PersistenceControlSystem persistenceControlSystem = new PersistenceControlSystem();

    @Autowired
    MesSpringClient productionManager = new MesSpringClient();

    @BeforeAll
    static void init() {
        agvPlugin.start(assetManager);
        warehousePlugin.start(assetManager);
        assemblyStationPlugin.start(assetManager);
        persistencePlugin.start(assetManager);
    }

    @Test
    void testStartProduction() throws InterruptedException, JSONException {
        TreeMap<Integer, String> productionSequence = new TreeMap<>();
        productionSequence.put(1, "Warehouse:PickItem,1");
        productionSequence.put(2, "AGV:MoveToStorageOperation");
        productionSequence.put(3, "AssemblyStation:1004");
        productionManager.startProduction(productionSequence);
        Thread.sleep(1000);
        JSONObject json = new JSONObject(warehouseControlSystem.getStatus(assetManager));
        JSONArray arrJson = json.getJSONArray("Inventory");
        JSONObject inventoryItem = new JSONObject(arrJson.getString(0));
        String content = inventoryItem.get("Content").toString();
        assertTrue(content.isBlank());
    }
    */


}
