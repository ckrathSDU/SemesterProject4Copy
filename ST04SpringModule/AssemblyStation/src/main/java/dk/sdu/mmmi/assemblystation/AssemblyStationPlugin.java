package dk.sdu.mmmi.assemblystation;

import dk.sdu.mmmi.common.data.Asset;
import dk.sdu.mmmi.common.data.AssetManager;
import dk.sdu.mmmi.commonAssemblyStation.AssemblyStation;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import dk.sdu.mmmi.common.services.IAssetManagerPluginService;

@Component
@ComponentScan
public class AssemblyStationPlugin implements IAssetManagerPluginService {

    //Initiates the connection to the asset
    @Override
    public void start(AssetManager assetManager) {
        Asset assemblyStationAsset = createAssemblyStation();
        assetManager.addAsset(assemblyStationAsset);
    }

    @Override
    public void stop(AssetManager assetManager) {
        // Remove assets
        for (Asset agv : assetManager.getAssets(AssemblyStation.class)) {
            assetManager.removeAsset(agv);
        }
    }

    private Asset createAssemblyStation(){
        Asset assemblyStationAsset = new AssemblyStation("localhost", "1883", "/");
        return assemblyStationAsset;
    }
}
