package chapter2java8.actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import chapter2.message.ReduceData;
import chapter2.message.Result;

import java.util.HashMap;
import java.util.Map;

import static akka.japi.pf.ReceiveBuilder.match;

public class AggregateActor extends AbstractActor {

    private Map<String, Integer> finalReducedMap = new HashMap<>();

    public AggregateActor() {
        receive(ReceiveBuilder.
                match(ReduceData.class, reduceData -> aggregateInMemoryReduce(reduceData.getReduceDataList()))
                .match(Result.class, result -> sender().tell(finalReducedMap.toString(), self()))
                .build());
    }

    private void aggregateInMemoryReduce(Map<String, Integer> reducedList) {
        for (String key : reducedList.keySet()) {
            Integer count = reducedList.get(key) + finalReducedMap.getOrDefault(key, 0);
            finalReducedMap.put(key, count);
        }
    }

}