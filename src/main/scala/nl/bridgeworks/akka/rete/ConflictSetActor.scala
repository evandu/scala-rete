package nl.bridgeworks.akka.rete

import akka.actor.{ActorLogging, ActorRef, Actor}

class ConflictSetActor extends Actor with ReteNodeActor with ActorLogging {
  //makes sure known facts aren't passed through
  var workingMemory = List[(Fact, String)]()
  private var rootNodes = List[ActorRef]()

  def receive = {
    case a:Assertion =>
      //extract uknown facts, pass them only - this makes sure a known fact produced from a terminal node doesn't cause an infinite loop
      val diff:Assertion = delta(workingMemory, withAssertion = ensureSafety(a))
      //make the facts from the assertion suitable to be added to working memory
      val diffForWM = diff.facts map {f => (f, a.inferenceRunId)}
      workingMemory = diffForWM.toList ++ workingMemory
      fire(diff, rootNodes)
    case ("add child", a:ActorRef) =>
      rootNodes = a :: rootNodes
    case "print" => log.debug(s"$workingMemory")
    case _ => log.warning("CS: confused.")
  }
}
