package dk.sdu.mmmi.commmonProductionManager;


import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface IProductionManager {

    void startProduction(TreeMap<Integer, String> productionMap);

    void deleteProduction(Map<Integer, String> productionMap);

    void queueProduction(TreeMap<Integer, String> productionMap);

    void stopProduction(Map<Integer, String> productionMap);

    List<TreeMap<Integer, String>> getProductionQueue();

    List<TreeMap<Integer, String>> getPersistedProductions();

    Map<String, Object> getProductionMetaData(int index);
}
