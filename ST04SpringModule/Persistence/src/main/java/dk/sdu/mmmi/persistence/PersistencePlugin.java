package dk.sdu.mmmi.persistence;

import dk.sdu.mmmi.common.data.Asset;
import dk.sdu.mmmi.common.data.AssetManager;
import dk.sdu.mmmi.common.services.IAssetManagerPluginService;
import dk.sdu.mmmi.commonPersistence.Persistence;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Component
@ComponentScan
public class PersistencePlugin implements IAssetManagerPluginService {

    //Initiates the connection to the asset
    @Override
    public void start(AssetManager assetManager) {
        Asset databaseAsset = createDatabase();
        assetManager.addAsset(databaseAsset);
    }

    @Override
    public void stop(AssetManager assetManager) {
        // Remove assets
        for (Asset agv : assetManager.getAssets(Persistence.class)) {
            assetManager.removeAsset(agv);
        }
    }

    private Asset createDatabase(){
        Asset databaseAsset = new Persistence("st4-mysqldb", "", "");
        return databaseAsset;
    }
}
