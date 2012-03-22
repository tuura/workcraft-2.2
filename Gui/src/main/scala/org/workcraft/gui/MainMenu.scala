package org.workcraft.gui

import javax.swing.JMenuBar
import javax.swing.JMenu
import javax.swing.JMenuItem
import org.workcraft.gui.docking.DockableWindow
import org.workcraft.services.GlobalServiceManager
import org.workcraft.services.NewModelImpl
import javax.swing.JComponent

import org.workcraft.scala.effects.IO

class MainMenu(
    mainWindow: MainWindow, utilityWindows: List[DockableWindow[_ <: JComponent]], 
    services: () => GlobalServiceManager,
    newModel: ((NewModelImpl, Boolean)) => IO[Unit],
    reconfigure: IO[Unit]) extends JMenuBar {
  val fileMenu = new FileMenu(services, mainWindow, newModel)
  val editMenu = new EditMenu(mainWindow)
  val windowsMenu = new UtilityWindowsMenu(utilityWindows)
  val toolsMenu = new ToolsMenu(services, mainWindow)  
  val utilityMenu = new JMenu("Utility")
  utilityMenu.setMnemonic('U')
  utilityMenu.add(GUI.menuItem("Reconfigure", Some('R'), None, reconfigure))
  
  add(fileMenu)
  add(editMenu)
  add(windowsMenu)
  add(toolsMenu)
  add(utilityMenu)
}