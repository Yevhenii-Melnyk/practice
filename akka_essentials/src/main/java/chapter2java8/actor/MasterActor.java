package chapter2java8.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import akka.routing.RoundRobinPool;
import chapter2.message.MapData;
import chapter2.message.ReduceData;
import chapter2.message.Result;

public class MasterActor extends AbstractActor {

    private ActorRef mapActor = context().actorOf(Props.create(MapActor.class, MapActor::new).withRouter(new RoundRobinPool(5)), "map");
    private ActorRef reduceActor = context().actorOf(Props.create(ReduceActor.class, ReduceActor::new)
            .withRouter(new RoundRobinPool(5)), "reduce");
    private ActorRef aggregateActor = getContext().actorOf(Props.create(AggregateActor.class, AggregateActor::new), "aggregate");

    public MasterActor() {
        receive(ReceiveBuilder.
                match(String.class, message -> mapActor.tell(message, self()))
                .match(MapData.class, message -> reduceActor.tell(message, self()))
                .match(ReduceData.class, message -> aggregateActor.tell(message, self()))
                .match(Result.class, message -> aggregateActor.forward(message, context()))
                .build());
    }

}