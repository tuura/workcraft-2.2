package org.workcraft.plugins.stg21
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionBase
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import org.workcraft.dependencymanager.advanced.core.EvaluationContext
import java.awt.geom.Point2D

object modifiable {
  import org.workcraft.plugins.stg21.fields._
  implicit def decorateModifiableExpression[W](we : ModifiableExpression[W]) = new {
    def modifiableField[P]
      (getter : W => P)
      (setter : P => W => W)
      : ModifiableExpression[P] = {
      new ModifiableExpressionBase[P] {
        override def setValue(v : P) {
          we.setValue(setter(v)(org.workcraft.dependencymanager.advanced.core.GlobalCache.eval(we)))
        }
        override def evaluate(context : EvaluationContext) = {
          getter(context.resolve(we))
        }
      }
    }
    def modifiableField[P] (field : Field[W,P]) : ModifiableExpression[P] = modifiableField[P](field.get)(field.set)
  }
  import types._
  implicit def decorateModifiableVisualStg (stg : ModifiableExpression[VisualStg]) = new {
    val visual = stg.modifiableField (VisualStg.visual)
    val math = stg.modifiableField (VisualStg.math)
  }
  implicit def decorateModifiableMathStg (stg : ModifiableExpression[MathStg]) = new {
    val signals : ModifiableExpression[Col[Signal]] = stg.modifiableField ((_ : MathStg).signals) (x => _.copy(signals=x))
    val transitions : ModifiableExpression[Col[Transition]] = stg.modifiableField ((_ : MathStg).transitions) (x => _.copy(transitions=x))
    val places : ModifiableExpression[Col[Place]] = stg.modifiableField ((_ : MathStg).places) (x => _.copy(places=x))
    val arcs : ModifiableExpression[Col[Arc]] = stg.modifiableField ((_ : MathStg).arcs) (x => _.copy(arcs=x))
  }
  implicit def decorateModifiableVisualModel[N,A] (visual : ModifiableExpression[VisualModel[N,A]]) = new {
    val groups : ModifiableExpression[Col[Group]] = visual.modifiableField ((_ : VisualModel[N,A]).groups) (x => _.copy(groups=x))
    val arcs : ModifiableExpression[Map[A, VisualArc]] = visual.modifiableField ((_ : VisualModel[N,A]).arcs) (x => _.copy(arcs=x))
    val nodesInfo : ModifiableExpression[Map[N, VisualInfo]] = visual.modifiableField ((_ : VisualModel[N,A]).nodesInfo) (x => _.copy(nodesInfo=x))
  }
  implicit def decorateModifiableMap[K,V] (map : ModifiableExpression[Map[K,V]]) = {
    class Q {
    def lookup(key : K) : ModifiableExpression[Option[V]] = 
      map.modifiableField ((_ : Map[K,V]).get(key)) ((x : Option[V]) => x match {
        case None => _ - key
        case Some(value) => _ + ((key, value))
      }
      )
    }
    new Q
  }
  
  implicit def decorateModifiableOption[V] (o : ModifiableExpression[Option[V]]) = {
    class Q{
    def orElse(v : V) : ModifiableExpression[V] = o.modifiableField ((_ : Option[V]).getOrElse(v)) (x => _ => Some(x))
    }
    new Q
  }
  implicit def decorateVisualInfo (v : ModifiableExpression[VisualInfo]) = new {
    def position : ModifiableExpression[Point2D] = v.modifiableField(VisualInfoFields.position)
    def parent : ModifiableExpression[Option[Id[Group]]] = v.modifiableField ((_ : VisualInfo).parent) (x => _.copy(parent = x))
  }
  implicit def decorateCol[T] (v : ModifiableExpression[Col[T]]) = new {
    def lookup(k : Id[T]) : ModifiableExpression[Option[T]] = v.modifiableField (_.lookup(k)) (x => m => x match {
      case Some(t) => m.insert(k)(t)
      case None => m
    })
  }
}
