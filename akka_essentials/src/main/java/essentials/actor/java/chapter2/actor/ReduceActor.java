package essentials.actor.java.chapter2.actor;

import akka.actor.UntypedActor;
import essentials.actor.java.chapter2.message.MapData;
import essentials.actor.java.chapter2.message.ReduceData;
import essentials.actor.java.chapter2.message.WordCount;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReduceActor extends UntypedActor {

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof MapData) {
            MapData mapData = (MapData) message;
            // reduce the incoming data and forward the result to Master actor
            sender().tell(reduce(mapData.getDataList()), self());
        } else
            unhandled(message);
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
