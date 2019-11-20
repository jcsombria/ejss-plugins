package org.colos.ejs.osejs.plugins;

/**
 * This class stores the information for the right click options of buttons added by plugins into the
 * Ejs button bar.
 * 
 * @author Iñigo Aizpuru Rueda
 * @version 1.0 February 2018
 *
 */
public class PluginRightClickOptionInfo {

  private String optionTitle;
  private Runnable optionAction;
  
  /**
   * Creates a PluginRightClickOptionInfo instance with a title and an action to be executed
   * when the option is selected.
   * 
   * @param A String containing the text of the popup menu item
   * @param A Runnable that will be executed when the option is selected
   */
  public PluginRightClickOptionInfo(String _title, Runnable _optionAction) {
    optionTitle = _title;
    optionAction = _optionAction;
  }
  
  /**
   * Returns the title for the option.
   * 
   * @return A String containing the title
   */
  public String getTitle() {
    return optionTitle;
  }
  
  /**
   * Returns the Runnable to be executed when the option is selected.
   * 
   * @return The Runnable that will be executed
   */
  public Runnable getAction() {
    return optionAction;
  }
  
  /**
   * Sets the String that will be displayed in the popup menu item.
   * 
   * @param The String that will be displayed
   */
  protected void setTitle(String _title) {
    optionTitle = _title;
  }

  /**
   * Sets the String that will be displayed in the popup menu item.
   * 
   * @param The Runnable that will be executed
   */
  protected void setAction(Runnable _optionAction) {
    optionAction = _optionAction;
  }

}
