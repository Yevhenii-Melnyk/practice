package essentials.actor.java.chapter2.message;

import java.util.Map;

public final class ReduceData {

    private final Map<String, Integer> reduceDataList;

    public Map<String, Integer> getReduceDataList() {
        return reduceDataList;
    }

    public ReduceData(Map<String, Integer> reduceDataList) {
        this.reduceDataList = reduceDataList;
    }

}
