package distributedmap;

import java.io.Serializable;

public class MapEntry implements Serializable {
    private String key;
    private String value;
    private OperationType operationType;

    public MapEntry(String key, String value, OperationType operationType) {
        this.key = key;
        this.value = value;
        this.operationType = operationType;
    }

    public MapEntry(String key, OperationType operationType) {
        this.key = key;
        this.operationType = operationType;
    }

    public enum OperationType {
        PUT, REMOVE
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public OperationType getOperationType() {
        return operationType;
    }
}
