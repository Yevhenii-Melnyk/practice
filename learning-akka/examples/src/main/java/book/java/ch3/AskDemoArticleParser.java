package book.java.ch3;


import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.japi.pf.ReceiveBuilder;
import akka.util.Timeout;
import book.kotlin.ch3.ArticleBody;
import book.kotlin.ch3.HttpResponse;
import book.kotlin.ch3.ParseArticle;
import book.kotlin.ch3.ParseHtmlArticle;
import book.kotlin.messages.GetRequest;
import scala.PartialFunction;
import scala.concurrent.Future;
import scala.runtime.BoxedUnit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static akka.pattern.Patterns.ask;
import static scala.compat.java8.FutureConverters.toJava;

public class AskDemoArticleParser extends AbstractActor {

	private final ActorSelection cacheActor;
	private final ActorSelection httpClientActor;
	private final ActorSelection artcileParseActor;
	private final Timeout timeout;

	public AskDemoArticleParser(String cacheActorPath, String httpClientActorPath, String artcileParseActorPath, Timeout timeout) {
		this.cacheActor = context().actorSelection(cacheActorPath);
		this.httpClientActor = context().actorSelection(httpClientActorPath);
		this.artcileParseActor = context().actorSelection(artcileParseActorPath);
		this.timeout = timeout;
	}

	@Override
	public PartialFunction<Object, BoxedUnit> receive() {
		return ReceiveBuilder.
				match(ParseArticle.class, msg -> {
					Future cacheAsk = ask(cacheActor, new GetRequest(msg.getUrl()), timeout);
					CompletionStage<ArticleBody> cacheResult = toJava(cacheAsk);
					CompletionStage<ArticleBody> result = cacheResult.handle((cachedBody, e) -> {
						if (cachedBody != null) {
							return CompletableFuture.completedFuture(cachedBody);
						} else {
							Future httpAsk = ask(httpClientActor, msg.getUrl(), timeout);
							CompletionStage<HttpResponse> httpFuture = toJava(httpAsk);
							return httpFuture.thenCompose(rawArticle -> {
										Future ask = ask(artcileParseActor, new ParseHtmlArticle(msg.getUrl(), rawArticle.getBody()), timeout);
										CompletionStage<ArticleBody> parseResult = toJava(ask);
										return parseResult;
									}
							);
						}
					}).thenCompose(x -> x);

					final ActorRef senderRef = sender();
					result.handle((resultForSender, t) -> {
						if (resultForSender != null) {
							String body = resultForSender.getBody();
							cacheActor.tell(body, self());
							senderRef.tell(body, self());
						} else {
							senderRef.tell(new akka.actor.Status.Failure(t), self());
						}
						return null;
					});
				}).build();
	}

}