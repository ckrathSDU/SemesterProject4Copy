package dk.sdu.mmmi.common.data;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ComponentScan
public class AssetManager {

    private final Map<String, Asset> assetMap = new ConcurrentHashMap<>();

    public String addAsset(Asset asset) {
        assetMap.put(asset.getID(), asset);
        return asset.getID();
    }

    public void removeAsset(String assetID) {
        assetMap.remove(assetID);
    }

    public void removeAsset(Asset asset) {
        assetMap.remove(asset.getID());
    }

    public Collection<Asset> getAssets() {
        return assetMap.values();
    }

    public <E extends Asset> List<Asset> getAssets(Class<E>... assetTypes) {
        List<Asset> r = new ArrayList<>();
        for (Asset e : getAssets()) {
            for (Class<E> assetType : assetTypes) {
                if (assetType.equals(e.getClass())) {
                    r.add(e);
                }
            }
        }
        return r;
    }

    public Asset getAsset(String ID) {
        return assetMap.get(ID);
    }
}
