package dk.sdu.mmmi.agv;

import dk.sdu.mmmi.common.data.Asset;
import dk.sdu.mmmi.common.data.AssetManager;
import dk.sdu.mmmi.commonAgv.AGV;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import dk.sdu.mmmi.common.services.IAssetManagerPluginService;

@Component
@ComponentScan
public class AGVPlugin implements IAssetManagerPluginService {

    //Initiates the connection to the asset
    @Override
    public void start(AssetManager assetManager) {
        Asset agvAsset = createAgv();
        assetManager.addAsset(agvAsset);
    }

    @Override
    public void stop(AssetManager assetManager) {
        // Remove assets
        for (Asset agv : assetManager.getAssets(AGV.class)) {
            assetManager.removeAsset(agv);
        }
    }

    private Asset createAgv(){
        Asset agvAsset = new AGV("localhost", "8082", "/v1/status/");
        return agvAsset;
    }
}
