package dk.sdu.mmmi.productionManager;

import dk.sdu.mmmi.agv.AGVControlSystem;
import dk.sdu.mmmi.assemblystation.AssemblyStationControlSystem;
import dk.sdu.mmmi.commmonProductionManager.IProductionManager;
import dk.sdu.mmmi.common.data.AssetManager;
import dk.sdu.mmmi.persistence.PersistenceControlSystem;
import dk.sdu.mmmi.warehouse.WarehouseControlSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@ComponentScan
public class MesSpringClient implements IProductionManager {
    @Autowired
    private AGVControlSystem agvControlSystem;

    @Autowired
    private AssemblyStationControlSystem assemblyStationControlSystem;

    @Autowired
    private WarehouseControlSystem warehouseControlSystem = new WarehouseControlSystem();

    @Autowired
    private PersistenceControlSystem persistenceControlSystem;

    @Autowired
    private AssetManager assetManager;

    private List<TreeMap<Integer,String>> productionQueue = new ArrayList<TreeMap<Integer, String>>();

    private final Lock lock = new ReentrantLock();
    private final Condition notSend  = lock.newCondition();

    @Override
    public void startProduction(TreeMap<Integer, String> productionMap) {
        /* Format of map:
        map<Integer, String> where the strong os formatted as: "<AssetName>: <Operation>"
            1: AGV: MoveToChargerOperation
            2: Warehouse: PickItem,
         */
        
        String productionId = UUID.randomUUID().toString();
        int operationSeqId = 1;
        for (Map.Entry<Integer, String> e: productionMap.entrySet()) {
            String[] operation = e.getValue().split(":");
            String operationLocation = operation[0];
            String operationMethod = operation[1];

            switch (operationLocation) {
                case ("AGV"):
                    agvControlSystem.performOperation(operationMethod, assetManager);
                    boolean agvStarted = false;
                    while (agvControlSystem.isReady(assetManager)) {
                        if (!agvStarted) {
                            agvStarted = true;
                        }
                    }
                    persistenceControlSystem.saveOperation(productionId, operationSeqId, operationMethod, "AGV", LocalDateTime.now());
                    break;
                case ("AssemblyStation"):
                    assemblyStationControlSystem.performOperation(operationMethod, assetManager);
                    boolean assemblyStarted = false;
                    while (assemblyStationControlSystem.isReady(assetManager)) {
                        if (!assemblyStarted) {
                            assemblyStarted = true;
                        }
                    }
                    persistenceControlSystem.saveOperation(productionId, operationSeqId, operationMethod, "Assembly Station", LocalDateTime.now());
                    break;
                case ("Warehouse"):
                    warehouseControlSystem.performOperation(operationMethod, assetManager);
                    boolean warehouseStarted = false;
                    while (warehouseControlSystem.isReady(assetManager)) {
                        if (!warehouseStarted) {
                            warehouseStarted = true;
                        }
                    }
                    persistenceControlSystem.saveOperation(productionId, operationSeqId, operationMethod, "Warehouse", LocalDateTime.now());
                    break;
            }
            waitingForAsset(operationLocation);
            operationSeqId++;
        }
    }

    private void waitingForAsset(String asset) {
        boolean waitingForAsset = false;
        while (!agvControlSystem.isReady(assetManager)
                || !assemblyStationControlSystem.isReady(assetManager)
                || !warehouseControlSystem.isReady(assetManager)) {

            if (!waitingForAsset) {
                waitingForAsset = true;
                System.out.println("Waiting for " + asset);
            }
        }
    }

    @Override
    public void deleteProduction(Map<Integer, String> productionMap) {

    }

    @Override
    public void stopProduction(Map<Integer, String> productionMap) {

    }

    @Override
    public void queueProduction(TreeMap<Integer, String> productionMap) {
            this.productionQueue.add(productionMap);
    }

    @Override
    public List<TreeMap<Integer, String>> getPersistedProductions() {
        return persistenceControlSystem.getPersistedProductions();
    }

    @Override
    public Map<String, Object> getProductionMetaData(int index) {
        return persistenceControlSystem.getProductionMetaData(index);
    }

    // Accessor
    public List<TreeMap<Integer, String>> getProductionQueue() {
        return this.productionQueue;
    }
}
