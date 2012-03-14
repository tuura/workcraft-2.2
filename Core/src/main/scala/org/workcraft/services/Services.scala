package org.workcraft.services

sealed trait Scope

trait GlobalScope extends Scope

trait ModelScope extends Scope

trait Service[S <: Scope, ImplT]

/*trait ServiceProvider[S <: Scope] {
  def implementation[T](service: Service[S, T]): Option[T]
}*/

trait GlobalServiceProvider {
  def implementations[T](service: Service[GlobalScope,T]): List[T]
}

object GlobalServiceProvider {
  val Empty = new GlobalServiceProvider {
    def implementations[T](service: Service[GlobalScope, T]) = Nil
  }
}

trait ModelServiceProvider {
  def implementation[T](service: Service[ModelScope,T]): Option[T]
}

object ModelServiceProvider {
  val Empty = new ModelServiceProvider {
    def implementation[T](service: Service[ModelScope, T]) = None
  }
}