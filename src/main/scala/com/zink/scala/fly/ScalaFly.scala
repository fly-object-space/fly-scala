package com.zink.scala.fly

import scala.actors.Actor
import scala.collection.JavaConverters._
import com.zink.fly.kit._
import com.zink.fly._
import com.zink.fly.stub.SerializingFieldCodec
import scala.util.{ Try, Success, Failure }

object ScalaFly {
  def makeFly(host: String = "localhost", codec: FieldCodec = new SerializingFieldCodec()): Either[Throwable, ScalaFly] = toScalaFly(FlyFactory.makeFly(host, codec))

  def find(): Either[Throwable, ScalaFly] = toScalaFly(new FlyFinder().find())
  def find(tag: String): Either[Throwable, ScalaFly] = toScalaFly(new FlyFinder().find(tag))
  def find(tags: Seq[String]): Either[Throwable, ScalaFly] = toScalaFly(new FlyFinder().find(tags.toArray[String]))

  private def toScalaFly(fly: ⇒ Fly): Either[Throwable, ScalaFly] =
    Try(fly) match {
      case Success(fly)   ⇒ Right(ScalaFly(fly))
      case Failure(error) ⇒ Left(error)
    }
}

case class ScalaFly(fly: Fly) {

  def read[T <: AnyRef](template: T, waitTime: Long): Option[T] = Option(fly.read(template, waitTime))
  def write(entry: AnyRef, leaseTime: Long): Long = fly.write(entry, leaseTime)
  def take[T <: AnyRef](template: T, waitTime: Long): Option[T] = Option(fly.take(template, waitTime))

  def writeMany(entries: Iterable[AnyRef], lease: Long): Long = fly.writeMany(entries.toList.asJava, lease)
  def readMany[T <: AnyRef](template: T, matchLimit: Long): Iterable[T] = fly.readMany(template, matchLimit).asScala
  def takeMany[T <: AnyRef](template: T, matchLimit: Long): Iterable[T] = fly.takeMany(template, matchLimit).asScala

  def notifyWrite(template: AnyRef, handler: Notifiable, leaseTime: Long): Boolean = fly.notifyWrite(template, handler, leaseTime)
  def notifyWrite(template: AnyRef, leaseTime: Long)(block: ⇒ Unit): Boolean = fly.notifyWrite(template, new Notifier(block), leaseTime)
  def notifyWrite(template: AnyRef, leaseTime: Long, actor: Actor): Boolean = notifyWrite(template, leaseTime) { actor ! template }

  def snapshot(template: AnyRef): AnyRef = fly.snapshot(template)

  class Notifier(f: ⇒ Unit) extends NotifyHandler {
    def templateMatched() = f
  }
}