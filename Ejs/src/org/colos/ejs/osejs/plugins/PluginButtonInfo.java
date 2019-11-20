package org.colos.ejs.osejs.plugins;

import java.util.Vector;

import javax.swing.ImageIcon;

/**
 * This class stores the information for the buttons added by plugins into Ejs.
 * 
 * @author Iñigo Aizpuru Rueda
 * @version 1.0 February 2018
 */
public class PluginButtonInfo {
  
  // Default icon size constants
  static private final int ICON_WIDTH = 21;
  static private final int ICON_HEIGHT = 20;

  private ImageIcon icon = null;
  private String toolTipText = "";
  private Runnable leftClickAction;
  private Vector<PluginRightClickOptionInfo> rightClicActions;
  
  /**
   * Creates a PluginButtonInfo instance with the specified Icon, tooltip and the action to be executed when
   * left clicking the button.
   * 
   * @param The ImageIcon that will be displayed on the button
   * @param The String that will be displayed as the tooltip
   * @param The Runnable that will be executed when left-clicking the button
   * @param A Vector of PluginRightClickOptionInfo that will be displayed when right-clicking the button
   * 
   */
  public PluginButtonInfo(ImageIcon _icon, String _toolTipText, Runnable _leftClickAction, Vector<PluginRightClickOptionInfo> _rightClicActions) {
    setIcon(_icon);
    toolTipText = _toolTipText;
    leftClickAction = _leftClickAction;
    rightClicActions = _rightClicActions;
  }
  
  /**
   * Returns the Icon for the button.
   * 
   * @return An Icon to be displayed on the button.
   */
  public ImageIcon getIcon() {
    return icon;
  }
  
  /**
   * Returns the tooltip for the button.
   * 
   * @return A String with the tooltip for the button.
   */
  public String getToolTipText() {
    return toolTipText;
  }
  
  /**
   * Returns the Runnable to be executed when performing a left click in the button.
   * 
   * @return A Runnable to be executed when left clicking the button
   */
  public Runnable getLeftClickAction() {
    return leftClickAction;
  }
  
  /**
   * Returns the Vector of PluginRightClickOptionInfo to be displayed when performing a right click in the button.
   * 
   * @return A Vector of PluginRightClickOptionInfo to be displayed
   */
  public Vector<PluginRightClickOptionInfo> getRightClickActions() {
    return rightClicActions;
  }

  /**
   * Sets the Icon to be displayed on the button.
   * 
   * @param The ImageIcon that will be displayed on the button
   */
  protected void setIcon(ImageIcon _icon) {
    if (_icon.getIconWidth() != ICON_WIDTH || _icon.getIconHeight() != ICON_HEIGHT)
      icon = new ImageIcon(_icon.getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, java.awt.Image.SCALE_SMOOTH));
    else
      icon = _icon;
  }
  
  /**
   * Sets the String that will be displayed as the tooltip of the button.
   * 
   * @param The String that will be displayed
   */
  protected void setToolTipText(String _toolTipText) {
    toolTipText = _toolTipText;
  }
  
  /**
   * Sets the Runnable to be executed when the button is left-clicked.
   * 
   * @param The Runnable that will be executed
   */
  protected void setLeftClickAction(Runnable _leftClickAction) {
    leftClickAction = _leftClickAction;
  }
  
  /**
   * Sets the Vector of PluginRightClickOptionInfo to be displayed when the button is right-clicked.
   * 
   * @param The Vector of PluginRightClickOptionInfo that will be displayed
   */
  protected void setRightClickAction(Vector<PluginRightClickOptionInfo> _rightClickActions) {
    rightClicActions = _rightClickActions;
  }
  
}
