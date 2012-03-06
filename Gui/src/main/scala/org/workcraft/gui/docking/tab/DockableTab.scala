package org.workcraft.gui.docking.tab
import org.workcraft.gui.docking.DockableWindow
import javax.swing.JPanel
import java.awt.BorderLayout
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.Box
import java.awt.Dimension
import javax.swing.JComponent

class DockableTab[A <: JComponent](window: DockableWindow[A]) extends JPanel {
  setOpaque(false)

  setLayout(new BorderLayout())
  setFocusable(false)

  val buttonsPanel = new JPanel()
  buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS))
  buttonsPanel.setOpaque(false)
  buttonsPanel.setFocusable(false)

  val trimmedTitle = if (window.title.length > 64)
    window.title.substring(0, 31) + "..." + window.title.substring(window.title.length() - 32, window.title.length());
  else
    window.title

  val label = new JLabel(trimmedTitle)
  label.setFocusable(false)

  if (window.configuration.maximiseButton) {
    val max = new TabButton("\u2191", "Maximize window", () => window.configuration.onMaximiseClicked(window))
    buttonsPanel.add(max)
    buttonsPanel.add(Box.createRigidArea(new Dimension(2, 0)))
  }

  val xs = label.getPreferredSize()

  val ys = if (window.configuration.closeButton) {
    val close = new TabButton("\u00d7", "Close window", () => window.configuration.onCloseClicked(window))
    buttonsPanel.add(close)
    close.getPreferredSize()
  } else xs

  label.setOpaque(false)

  this.add(label, BorderLayout.CENTER)
  this.add(buttonsPanel, BorderLayout.EAST)

  setPreferredSize(new Dimension(xs.width + ys.width + 30, Math.max(ys.height, xs.height) + 4));
}