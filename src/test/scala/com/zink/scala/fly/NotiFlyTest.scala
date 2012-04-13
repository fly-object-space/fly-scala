package com.zink.scala.fly

import com.zink.scala.fly.kit.FlyFactory

import org.specs2.mutable._
import org.specs2.specification._
import org.specs2.execute._

class NotiFlyTest extends Specification {
  
  val instance = FlyFactory()
  
  "Notify Match" in {
        
    // set up a notify counting handler
    val handler = new ExampleNotifyHandler()

    // make the notify template
    val template = new TestEntry(name = "Test Entry", reference = BigInt(5))   
        
    // ask the Space to set up the notify for us
    val result = instance.notifyWrite(template, handler, 10*60*1000)
        
    // check that it was set up ok
    result must beTrue
        
    // make a matching test entry 
    val entry = new TestEntry(name = "Test Entry", reference = BigInt(5), payload = "Dennis")
        
    // Make a load of writes one of which matches the the notify template
    instance.write(entry, 1*1000)
    
    // allow the reader thread to get the message
    Thread.sleep(1000)
        
    // check that the handler got call once and once only
    handler.getMatchCalled mustEqual 1
  }
    
  
  "Notify No Match" in {
    
    // set up a notify counting handler
    val handler = new ExampleNotifyHandler()
        
    // make the notify template anb
    val template = new TestEntry(name = "Test 2 Entry", reference = BigInt(5))
        
    // ask the Space to set up the notify for us
    val result = instance.notifyWrite(template, handler, 10*60*1000)
        
    // check that it was set up ok
    result must beTrue
        
    // make a non matching test entry 
    val entry = new TestEntry(name = "Non Matching Entry", reference = BigInt(7), payload = "Dennis")
        
    // Make a load of writes one of which matches the the notify template
    instance.write(entry, 1*1000)
        
    // allow the reader thread to get the message
    Thread.sleep(1000)
        
    // chack that the handler did not get called
    handler.getMatchCalled mustEqual 0
  }

  
  "Notify Lease" in {
        
    val template = new TestEntry(name = "Test 3 Entry", reference = BigInt(5))   
     
    // set up a counting handler
    val handler = new ExampleNotifyHandler()
        
    // ask the Space to set up the notify for us on which the lease will
    // expire quickly
    val setupOK_? = instance.notifyWrite(template, handler, 1000)
        
    // check that it was set up ok
    setupOK_? must beTrue
        
    // make an entry that matches the template
    val entry = new TestEntry(name = "Test 3 Entry", reference = BigInt(5), payload = "Dennis")
        
    // write the entry to make sure that it is matched 
    instance.write(entry, 1000)
        
    Thread.sleep(100)
        
    // chack that the handler got called once and once only
    handler.getMatchCalled mustEqual 1
        
    // now wait for the lease to expire 
    Thread.sleep(1200)
        
    // write the entry to make sure that it is matched 
    instance.write(entry, 1000)
        
    // check that the handler is still only one
    handler.getMatchCalled mustEqual 1
  }

      
  class ExampleNotifyHandler extends NotifyHandler {
    
    private var matchCalled = 0 
        
    override def templateMatched() {
      setMatchCalled(getMatchCalled + 1)
    }

    def getMatchCalled = matchCalled

    def setMatchCalled(matchCalled:Int) {
      this.matchCalled = matchCalled
    }   
  }
}