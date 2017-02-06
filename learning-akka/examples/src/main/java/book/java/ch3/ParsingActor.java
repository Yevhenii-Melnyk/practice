package book.java.ch3;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import book.kotlin.ch3.ArticleBody;
import book.kotlin.ch3.ParseHtmlArticle;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

public class ParsingActor extends AbstractActor {

	public PartialFunction<Object, BoxedUnit> receive() {
		return ReceiveBuilder.
				match(ParseHtmlArticle.class, msg -> {
					String body = ArticleExtractor.INSTANCE.getText(msg.getHtmlString());
					sender().tell(new ArticleBody(msg.getUri(), body), self());
				}).build();
	}

}