package chapter2java8.actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import chapter2.message.MapData;
import chapter2.message.WordCount;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class MapActor extends AbstractActor {

    private static final String[] STOP_WORDS = {"a", "am", "an", "and", "are", "as", "at",
            "be", "do", "go", "if", "in", "is", "it", "of", "on", "the", "to"};
    private static final List<String> STOP_WORDS_LIST = Arrays.asList(STOP_WORDS);

    public MapActor() {
        receive(ReceiveBuilder.
                match(String.class, str -> sender().tell(evaluateExpression(str), self()))
                .build());
    }

    private MapData evaluateExpression(String line) {
        List<WordCount> dataList = new ArrayList<>();
        StringTokenizer parser = new StringTokenizer(line);
        while (parser.hasMoreTokens()) {
            String word = parser.nextToken().toLowerCase();
            if (!STOP_WORDS_LIST.contains(word)) {
                dataList.add(new WordCount(word, 1));
            }
        }
        return new MapData(dataList);
    }
}
