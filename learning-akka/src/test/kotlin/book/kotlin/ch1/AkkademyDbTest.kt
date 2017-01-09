package book.kotlin.ch1

import akka.actor.ActorSystem
import book.kotlin.messages.SetRequest
import book.kotlin.util.TestActorRef
import book.kotlin.util.tell
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.Assert.assertEquals
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
class AkkademyDbTest : Spek({

    val system = ActorSystem.create()

    describe("akkademyDb") {

        describe("given SetRequest") {

            it("should place key/value into map") {
                val actorRef = TestActorRef<AkkademyDb>(system)
                actorRef.tell(SetRequest("key", "value"))
                val akkademyDb = actorRef.underlyingActor()
                assertEquals(akkademyDb.map["key"], "value")
            }

        }

    }
})

