package dk.sdu.mmmi.common.services;

import dk.sdu.mmmi.common.data.AssetManager;

public interface IAssetManagerPluginService {

    void start(AssetManager assetManager);

    void stop(AssetManager assetManager);
}
