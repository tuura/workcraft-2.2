
package org.workcraft.plugins

package stg21 {

import org.workcraft.util.Maybe
import org.workcraft.dependencymanager.advanced.core.EvaluationContext
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionBase
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import java.awt.geom.Point2D
import org.workcraft.dom.visual.connections.RelativePoint
import scala.collection.mutable.WeakHashMap
import scalaz.State
import org.workcraft.interop.ModelService
import org.workcraft.dependencymanager.advanced.core.Expression
import scalaz.Lens
import org.workcraft.plugins.stg21.fields.GroupLenses

object types {
  
  sealed trait SignalType
  object SignalType {
    case object Input extends SignalType
    case object Output extends SignalType
    case object Internal extends SignalType
  }
  
  sealed trait TransitionDirection
  
  object TransitionDirection {
    case object Rise extends TransitionDirection
    case object Fall extends TransitionDirection
    case object Toggle extends TransitionDirection
  }

  type InstanceNumber = Int
  type Transition = (TransitionLabel, InstanceNumber)
  
  sealed trait TransitionLabel
  case class DummyLabel(name : String) extends TransitionLabel
  case class SignalLabel(signal : Id[Signal], direction : TransitionDirection) extends TransitionLabel
  
  
  case class Signal(name : String, direction : SignalType)
  case class Id[T] (id : Int) {
    def upCast[B >: T] : Id[B] = Id(id)
    def downCast[B <: T] : Id[B] = Id(id)
  }

  case class ExplicitPlace(initialMarking : Int, name : String)
  
  sealed trait Arc
  case class ConsumingArc(from : Id[ExplicitPlace], to : Id[Transition]) extends Arc
  case class ProducingArc(from : Id[Transition], to : Id[ExplicitPlace]) extends Arc
  case class ImplicitPlaceArc(from : Id[Transition], to : Id[Transition], initialMarking : Int) extends Arc

  sealed trait StgConnectable
  case class NodeConnectable(n : StgNode) extends StgConnectable 
  case class ArcConnectable(a : Id[Arc]) extends StgConnectable
  
  case class Col[T] (map : Map[Id[T], T], nextFreeId : Id[T]) {
	  def remove(id : Id[T]) : Col[T] = Col[T](map - id, nextFreeId)
	  def lookup(key : Id[T]) : Option[T] = map.get(key)
	  def insert(key : Id[T])(value : T) = copy(map = map + ((key, value)))
	  def keys : List[Id[T]] = map.keys.toList
  }

  import StateExtensions._
  
  object Col {
    def empty[T] = Col[T](Map.empty, Id[T](0))
    def uncheckedLook[T] = (id : Id[T]) => Lens[Col[T], T](_.lookup(id).get, (col,v) => update(id)(x => v) ~> col)
    def add[T](t : T) : State[Col[T], Id[T]] = state (col => {
      (Col[T](col.map + ((col.nextFreeId, t)), Id[T](col.nextFreeId.id + 1)), col.nextFreeId)
    })
    def remove[T](t : Id[T]) : State[Col[T], Boolean] = state (col => {
      (Col[T](col.map - t, col.nextFreeId), col.map.contains(t))
    })
    def update[T](t : Id[T])(f : T => T) : State[Col[T], Boolean] = state (col => {
      col.map.get(t) match {
        case None => (col, false)
        case Some(x) => (Col[T](col.map + (t -> f(x)), col.nextFreeId), true)
      }
    })
  }
  
  case class MathStg (
    signals : Col[Signal],
    transitions : Col[Transition],
    places : Col[ExplicitPlace],
    arcs : Col[Arc]
  )
  
  sealed trait StgNode
  
  case class ExplicitPlaceNode (p : Id[ExplicitPlace]) extends StgNode
  case class TransitionNode (t : Id[Transition]) extends StgNode
  
  class VisualArc

  implicit def decorateVisualArc(arc : Arc) = new {
    def firstAndSecond : (StgNode, StgNode) = arc match {
      case ProducingArc(t, p) => (TransitionNode(t), ExplicitPlaceNode(p))
      case ConsumingArc(p, t) => (ExplicitPlaceNode(p), TransitionNode(t))
      case ImplicitPlaceArc(t1, t2, _) => (TransitionNode(t1), TransitionNode(t2))
    }
    def first = firstAndSecond._1
    def second = firstAndSecond._2
  }
  
  case class Bezier(cp1: RelativePoint, cp2: RelativePoint) extends VisualArc
  case class Polyline(cp: List[Point2D.Double]) extends VisualArc
  
  case class Group(info : VisualInfo)

  object Group extends GroupLenses
  
  case class VisualInfo(position : Point2D.Double, parent : Option[Id[Group]])
  object VisualInfo extends fields.VisualInfoLenses 
  
  sealed trait VisualNode
  case class StgVisualNode(n : StgNode) extends VisualNode
  case class GroupVisualNode(g : Id[Group]) extends VisualNode
  
  sealed trait VisualEntity
  case class NodeVisualEntity(n : VisualNode) extends VisualEntity
  case class ArcVisualEntity(a : Id[Arc]) extends VisualEntity
  
  case class VisualModel[N,A] (
    groups : Col[Group],
    arcs : Map[A, VisualArc],
    nodesInfo : Map[N, VisualInfo]
  )
  
  case class VisualStg (
    math : MathStg,
    visual : VisualModel[StgNode, Id[Arc]]
  )

  object VisualStg extends fields.VisualStgLenses {
	val empty = VisualStg(MathStg.empty, VisualModel.empty)
  }
  
  object VisualModel extends fields.VisualModelLenses {
    def empty[N,A] = VisualModel[N,A](Col.empty, Map.empty, Map.empty)
    def addNode[N,A](node : N, where : Point2D.Double) : State[VisualModel[N,A], Unit] = state ((m : Map[N, VisualInfo]) => (m + ((node, VisualInfo(where, None))), ())) .on(nodesInfo)
    def removeNode[N,A](node : N) : State[VisualModel[N,A], Boolean] = state ((m : Map[N, VisualInfo]) => (m - node, m.contains(node))) .on(nodesInfo)
  }
  
  val MATH_STG_SERVICE_HANDLE : ModelService[Expression[MathStg]] = ModelService.createNewService(classOf[Expression[MathStg]], "STG representation of the underlying model");
  object MathStg extends org.workcraft.plugins.stg21.fields.MathStgLenses {
    val empty = MathStg(Col.empty, Col.empty, Col.empty, Col.empty)
  }
}
}
