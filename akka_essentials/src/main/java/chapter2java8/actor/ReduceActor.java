package chapter2java8.actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import chapter2.message.MapData;
import chapter2.message.ReduceData;
import chapter2.message.WordCount;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReduceActor extends AbstractActor {

    public ReduceActor() {
        receive(ReceiveBuilder.
                match(MapData.class, mapData -> sender().tell(reduce(mapData.getDataList()), self()))
                .build());
    }

    private ReduceData reduce(List<WordCount> dataList) {
        Map<String, Integer> reducedMap = new HashMap<>();
        for (WordCount wordCount : dataList) {
            Integer value = reducedMap.getOrDefault(wordCount.getWord(), 0);
            value++;
            reducedMap.put(wordCount.getWord(), value);
        }
        return new ReduceData(reducedMap);
    }

}
