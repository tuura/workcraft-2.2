package org.workcraft.plugins.cpog.scala

import org.workcraft.dom.visual.DrawRequest
import java.awt.Color
import org.workcraft.dom.visual.ColorisableGraphicalContent
import java.awt.BasicStroke
import java.awt.geom.PathIterator
import java.awt.geom.Point2D
import java.awt.geom.Line2D
import java.awt.geom.Path2D
import org.workcraft.dom.visual.Touchable

object Path {
  def colorisableGraphicalContent(p: Path2D, stroke: BasicStroke, color: Color) = new ColorisableGraphicalContent {
    override def draw(r: DrawRequest) = {
      val g = r.getGraphics();
      val colorisation = r.getColorisation().getColorisation();

      g.setStroke(stroke)
      g.setColor(color)

      g.draw(p)
    }
  }
  
  def visualBounds (p: Path2D) = p.getBounds2D

  def touchable(p: Path2D, threshold: Double) = new Touchable {
    val pathError = 0.01
    val segments = getSegments(p.getPathIterator(null, pathError))

    private def testSegments(segments: List[Line2D], point: Point2D, threshold: Double): Boolean = {
      val tSq = threshold * threshold
      for (s <- segments) {
        if (s.ptSegDistSq(point) < tSq)
          return true
      }
      false
    }

    private def getSegments(i: PathIterator): List[Line2D] = {
      val coords = new Array[Double](6)

      var curX = 0.0
      var curY = 0.0

      var startX = 0.0
      var startY = 0.0

      var broken = true

      var segments: List[Line2D] = Nil

      while (!i.isDone) {
        val t = i.currentSegment(coords)
        if (t == PathIterator.SEG_MOVETO) {
          curX = coords(0)
          curY = coords(1)
          broken = true
        } else if (t == PathIterator.SEG_LINETO) {
          segments = new Line2D.Double(curX, curY, coords(0), coords(1)) :: segments
          if (broken) {
            startX = curX
            startY = curY
            broken = false
          }
          curX = coords(0)
          curY = coords(1)
        } else if (t == PathIterator.SEG_CLOSE) {
          segments = new Line2D.Double(curX, curY, startX, startY) :: segments
          curX = startX
          curY = startY
          broken = true
        }
        
        i.next()
      }

      segments
    }

    override def hitTest(point: Point2D) = {
      testSegments(segments, point, threshold)
    }
    override def getBoundingBox = p.getBounds2D
    override def getCenter = new Point2D.Double(0, 0)
  }

  def richGraphicalContent(p: Path2D, stroke: BasicStroke, color: Color, touchThreshold: Double) =
    new RichGraphicalContent(colorisableGraphicalContent(p, stroke, color), visualBounds(p), touchable(p, touchThreshold))

}