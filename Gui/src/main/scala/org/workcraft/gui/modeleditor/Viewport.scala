package org.workcraft.gui.modeleditor

import org.workcraft.scala.Expressions._
import scalaz._
import Scalaz._
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._

import org.workcraft.graphics.GraphicalContent
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D
import java.awt.geom.Point2D
import org.workcraft.dependencymanager.advanced.user.Variable
import org.workcraft.dependencymanager.advanced.core.GlobalCache

class Viewport(val dimensions: Expression[(Int, Int, Int, Int)]) {
  val translationX = Variable.create(0.0)
  val translationY = Variable.create(0.0)
  val scale = Variable.create(0.0625)
  val origin = new Point2D.Double(0.0, 0.0)

  def transform: Expression[AffineTransform] = for {
    view <- viewTransform;
    proj <- projection
  } yield {
    val result = new AffineTransform(proj)
    result.concatenate(view)
    result
  }

  def inverseTransform: Expression[AffineTransform] = transform.map(_.createInverse)
  
  def pixelSizeInUserSpace = for {
    uts <- userToScreen;
    stu <- screenToUser
  } yield {
    val p = uts(origin)
    val q = new Point2D.Double (p.getX+1, p.getY+1)
    stu(q)
  }

  def projection = dimensions.map {
    case (x, y, w, h) => {
      val result = new AffineTransform()
      result.translate(w / 2 + x, h / 2 + y)
      if (h != 0)
        result.scale(h / 2, h / 2)
      result
    }
  }

  def viewTransform = for {
    tx <- translationX;
    ty <- translationY;
    scale <- scale
  } yield {
    val result = new AffineTransform()
    result.scale(scale, scale)
    result.translate(tx, ty)
    result
  }

  def screenToUser: Expression[Point2D.Double => Point2D.Double] = for {
    inverseTransform <- inverseTransform
  } yield p => {
    val result = new Point2D.Double
    inverseTransform.transform(p, result)
    result
  }

  def screenToUserX: Expression[Int => Double] = for {
    screenToUser <- screenToUser
  } yield x => screenToUser(new Point2D.Double(x, 0)).getX

  def screenToUserY: Expression[Int => Double] = for {
    screenToUser <- screenToUser
  } yield y => screenToUser(new Point2D.Double(0, y)).getY

  def userToScreen: Expression[Point2D.Double => Point2D.Double] = for {
    transform <- transform
  } yield p => {
    val result = new Point2D.Double
    transform.transform(p, result)
    result
  }

  def userToScreenX: Expression[Double => Int] = for {
    userToScreen <- userToScreen
  } yield x => userToScreen(new Point2D.Double(x, 0)).getX.toInt

  def userToScreenY: Expression[Double => Int] = for {
    userToScreen <- userToScreen
  } yield y => userToScreen(new Point2D.Double(0, y)).getY.toInt

  def visibleArea: Expression[Rectangle2D] = for {
    inverseTransform <- inverseTransform;
    viewportDimensions <- dimensions
  } yield {
    val (viewX, viewY, viewWidth, viewHeight) = viewportDimensions

    val viewLL = new Point2D.Double(viewX, viewHeight + viewY)
    val viewUR = new Point2D.Double(viewWidth + viewX, viewY)

    val visibleUL = new Point2D.Double
    val visibleLR = new Point2D.Double

    inverseTransform.transform(viewLL, visibleUL)
    inverseTransform.transform(viewUR, visibleLR)

    new Rectangle2D.Double(visibleUL.getX, visibleLR.getY, visibleLR.getX - visibleUL.getX, visibleUL.getY - visibleLR.getY)
  }

  def pan(dx: Int, dy: Int): IO[Unit] = (for {
    userToScreen <- userToScreen.eval;
    screenToUser <- screenToUser.eval
  } yield {
    val originInScreenSpace = userToScreen(origin)

    val panInScreenSpace = new Point2D.Double(originInScreenSpace.getX + dx, originInScreenSpace.getY + dy)
    val panInUserSpace = screenToUser(panInScreenSpace)

    translationX.update(_ + panInUserSpace.getX) >>=| translationY.update(_ + panInUserSpace.getY)
  }).join

  def zoom(levels: Int): IO[Unit] =
    scale.update(s => Math.min(Math.max(s * Math.pow(Viewport.scaleFactor, levels), 0.01), 1.0))

  def zoomTo(levels: Int, anchor: Point2D.Double): IO[Unit] = (for {
    screenToUser1 <- screenToUser.eval;
    val anchorInOldSpace = screenToUser1(anchor);
    _ <- zoom(levels);
    screenToUser2 <- screenToUser.eval;
    val anchorInNewSpace = screenToUser2(anchor);
    tx <- translationX.eval;
    ty <- translationY.eval
  } yield {
   translationX.set(tx + anchorInNewSpace.getX - anchorInOldSpace.getX) >>=| translationY.set(ty + anchorInNewSpace.getY - anchorInOldSpace.getY)
  }).join
}

object Viewport {
  val scaleFactor = Math.pow(2, 0.125)
}