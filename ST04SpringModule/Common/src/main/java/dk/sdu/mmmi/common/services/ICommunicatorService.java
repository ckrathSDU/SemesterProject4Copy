package dk.sdu.mmmi.common.services;

import dk.sdu.mmmi.common.data.Asset;
import dk.sdu.mmmi.common.data.AssetManager;

public interface ICommunicatorService {

    void performOperation(String s, AssetManager assetManager);

    boolean isReady(AssetManager assetManager);

    String getStatus(AssetManager assetManager);

    int getState(AssetManager assetManager);
}
