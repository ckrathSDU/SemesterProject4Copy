package dk.sdu.mmmi.warehouse;

import dk.sdu.mmmi.common.data.Asset;
import dk.sdu.mmmi.common.data.AssetManager;
import dk.sdu.mmmi.commonWarehouse.Warehouse;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import dk.sdu.mmmi.common.services.IAssetManagerPluginService;


@Component
@ComponentScan
public class WarehousePlugin implements IAssetManagerPluginService {

    //Initiates the connection to the asset
    @Override
    public void start(AssetManager assetManager) {
        Asset warehouseAsset = createWarehouse();
        assetManager.addAsset(warehouseAsset);
    }

    @Override
    public void stop(AssetManager assetManager) {
        // Remove assets
        for (Asset warehouse : assetManager.getAssets(Warehouse.class)) {
            assetManager.removeAsset(warehouse);
        }
    }

    private Asset createWarehouse(){
        Asset warehouseAsset = new Warehouse("localhost", "8081", "/Service.asmx");
        return warehouseAsset;
    }
}
