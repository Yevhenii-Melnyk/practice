package chapter2.actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.RoundRobinPool;
import chapter2.message.MapData;
import chapter2.message.ReduceData;
import chapter2.message.Result;

public class MasterActor extends UntypedActor {

    ActorRef mapActor = context().actorOf(Props.create(MapActor.class, MapActor::new).withRouter(new RoundRobinPool(5)), "map");
    ActorRef reduceActor = context().actorOf(Props.create(ReduceActor.class, ReduceActor::new)
            .withRouter(new RoundRobinPool(5)), "reduce");
    ActorRef aggregateActor = getContext().actorOf(Props.create(AggregateActor.class, AggregateActor::new), "aggregate");

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof String) {
            mapActor.tell(message, self());
        } else if (message instanceof MapData) {
            reduceActor.tell(message, self());
        } else if (message instanceof ReduceData) {
            aggregateActor.tell(message, self());
        } else if (message instanceof Result) {
            aggregateActor.forward(message, getContext());
        } else
            unhandled(message);
    }

}