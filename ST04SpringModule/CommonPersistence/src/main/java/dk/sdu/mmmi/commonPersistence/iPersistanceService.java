package dk.sdu.mmmi.commonPersistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface iPersistanceService {
    boolean saveOperation(String productionId, int operationSequenceId, String operation, String assetName, LocalDateTime timestamp);

    List<TreeMap<Integer, String>> getPersistedProductions();

    Map<String, Object> getProductionMetaData(int index);
}
