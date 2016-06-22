package chapter2.actor;

import akka.actor.UntypedActor;
import chapter2.message.MapData;
import chapter2.message.WordCount;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class MapActor extends UntypedActor {

    private static final String[] STOP_WORDS = {"a", "am", "an", "and", "are", "as", "at",
            "be", "do", "go", "if", "in", "is", "it", "of", "on", "the", "to"};
    private static final List<String> STOP_WORDS_LIST = Arrays.asList(STOP_WORDS);

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof String) {
            String work = (String) message;
            // map the words in the sentence and send the result to MasterActor
            sender().tell(evaluateExpression(work), self());
        } else
            unhandled(message);
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



