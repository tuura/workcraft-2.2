package org.workcraft.gui
import java.awt.Color
import scalaz._
import Scalaz._
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import javax.swing.SwingUtilities
import javax.swing.ImageIcon
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.Image
import org.workcraft.logging.Logger
import java.lang.reflect.InvocationTargetException
import Logger._

object MainWindowIconManager {
  private def applyListener(window: MainWindow, active: Image, inactive: Image): IO[Unit] =
    SwingUtilities.invokeAndWait(new Runnable() {
      def run = {
        window.setIconImage(if (window.isActive()) active else inactive)
        window.addWindowListener(new WindowAdapter() {
          override def windowDeactivated(e: WindowEvent) = {window.setIconImage(inactive)}
          override def windowActivated(e: WindowEvent) = window.setIconImage(active);
        })
      }
    }).pure

  def apply(implicit window: MainWindow, logger:() => Logger[IO]) =
    new Thread(new Runnable() {
      def run = {
        try {
          (GUI.createIconFromSVG("images/icons/svg/place.svg", 32, 32, Color.WHITE) |@|
            GUI.createIconFromSVG("images/icons/svg/place_empty.svg", 32, 32, Color.WHITE))({
              case (Right(active), Right(inactive)) => applyListener(window, active.getImage(), inactive.getImage())
              case (Left(e), _) => warning(e)
              case (_, Left(e)) => warning(e)
            }).join.unsafePerformIO
        } catch {
          case e: InterruptedException => warning(e.getMessage())
          case e: InvocationTargetException => warning(e.getCause().getMessage())
          case e: RuntimeException => warning(e.getMessage())
        }
      }
    }).start()
}