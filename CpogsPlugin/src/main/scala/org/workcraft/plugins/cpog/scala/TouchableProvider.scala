package org.workcraft.plugins.cpog.scala

import java.awt.geom.AffineTransform
import org.workcraft.dom.visual.TransformHelper
import org.workcraft.dependencymanager.advanced.core.Expression
import java.awt.geom.Rectangle2D
import java.awt.geom.Point2D
import org.workcraft.dom.visual.Touchable
import org.workcraft.plugins.cpog.scala.nodes._
import org.workcraft.plugins.cpog.scala.Expressions.monadicSyntax


object TouchableProvider {
  
  val vertexTouchable = new Touchable {
    val size = VisualVertex.size
    val bb = new Rectangle2D.Double(-size/2, -size/2, size, size)
    val center = new Point2D.Double(0,0)
    
    def hitTest (point: Point2D) = point.distance(0,0) < size/2
    def getBoundingBox = bb
    def getCenter = center
  }
   
  def localTouchable (c : Component) : Expression[Touchable] =
    c match {
      case _ : Vertex => Expressions.constant(vertexTouchable)
      case v : Variable =>
        for ( image <- VisualVariable.image(v) ) 
        yield org.workcraft.plugins.cpog.gui.TouchableProvider.bbToTouchable(image.boundingBox);
      case r : RhoClause =>
        for ( image <- VisualRhoClause.image(r) ) 
        yield org.workcraft.plugins.cpog.gui.TouchableProvider.bbToTouchable(image.boundingBox);
    }
  
  def touchable (transform: Component => Expression[AffineTransform])(node : Node) : Expression[Touchable] = node match {
    case c : Component => for ( lt <- localTouchable(c); t <- transform(c) ) yield TransformHelper.transform(lt,t)
    case a : Arc => for (gui <- VisualArc.gui(touchable(transform))(a)) yield gui.shape
  }
}