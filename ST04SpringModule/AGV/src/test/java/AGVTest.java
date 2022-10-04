import dk.sdu.mmmi.agv.AGVControlSystem;
import dk.sdu.mmmi.agv.AGVPlugin;
import dk.sdu.mmmi.common.data.AssetManager;
import dk.sdu.mmmi.common.services.IAssetManagerPluginService;
import org.json.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class AGVTest {
    @Autowired
    static AssetManager assetManager = new AssetManager();

    @Autowired
    static AGVPlugin agvPlugin = new AGVPlugin();

    @Autowired
    AGVControlSystem agvControlSystem = new AGVControlSystem();

    @BeforeAll
    static void init() {
        agvPlugin.start(assetManager);
    }

    @Test
    void testGetState() {
        int state = agvControlSystem.getState(assetManager);
        assertTrue(state >= 1);
        assertTrue(state <= 3);
    }

    @Test
    void testGetStatus() {
        String status = agvControlSystem.getStatus(assetManager);
        assertFalse(status.isBlank());
        try {
            JSONObject json = new JSONObject(status);
            assertTrue(true);
        } catch (JSONException e) {
            fail();
        }
    }

    @Test
    void testPerformOperation() throws JSONException, InterruptedException {
        agvControlSystem.performOperation("MoveToAssemblyOperation", assetManager);
        Thread.sleep(1000);
        assertEquals(2, agvControlSystem.getState(assetManager));

        String newOperation = (new JSONObject(agvControlSystem.getStatus(assetManager))).getString("program name");
        assertEquals("MoveToAssemblyOperation", newOperation);
    }
}
