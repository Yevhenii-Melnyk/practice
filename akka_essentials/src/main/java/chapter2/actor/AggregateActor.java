package chapter2.actor;

import akka.actor.UntypedActor;
import chapter2.message.ReduceData;
import chapter2.message.Result;

import java.util.HashMap;
import java.util.Map;

public class AggregateActor extends UntypedActor {
    private Map<String, Integer> finalReducedMap = new HashMap<>();

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof ReduceData) {
            ReduceData reduceData = (ReduceData) message;
            aggregateInMemoryReduce(reduceData.getReduceDataList());
        } else if (message instanceof Result) {
            sender().tell(finalReducedMap.toString(), self());
        } else
            unhandled(message);
    }

    private void aggregateInMemoryReduce(Map<String, Integer> reducedList) {
        for (String key : reducedList.keySet()) {
            Integer count = reducedList.get(key) + finalReducedMap.getOrDefault(key, 0);
            finalReducedMap.put(key, count);
        }
    }

}