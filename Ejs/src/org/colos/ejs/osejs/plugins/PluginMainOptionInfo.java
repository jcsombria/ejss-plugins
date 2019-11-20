package org.colos.ejs.osejs.plugins;

import org.colos.ejs.osejs.edition.Editor;

/**
 * This class stores the information for the main options added by plugins into Ejs.
 * 
 * @author Iñigo Aizpuru Rueda
 * @version 1.0 February 2018
 *
 */
public class PluginMainOptionInfo {

  private String mainOptionId;
  @Deprecated
  private java.awt.Component mainOptionComponent;
  private Editor optionEditor;
  
  /**
   * Creates a PluginMainOptionInfo instance with a title and a component.
   * 
   * @param A String containing the id of the main option
   * @param A java.awt.Component that will be displayed when the main option is selected
   */
/*  @Deprecated
  public PluginMainOptionInfo(String _id, java.awt.Component _component) {
    mainOptionId = _id;
    mainOptionComponent = _component;
  }*/
  
  /**
   * Creates a PluginMainOptionInfo instance with a title and a component.
   * 
   * @param A String containing the id of the main option
   * @param An Editor that handles the edition when option is selected
   */
  public PluginMainOptionInfo(String _id, Editor _editor) {
    mainOptionId = _id;
    optionEditor = _editor;
  }

  /**
   * Returns the id of the option.
   * 
   * @return A String containing the id
   */
  public String getId() {
    return mainOptionId;
  }

  /**
   * Returns the java.awt.Component to be displayed when the option is selected.
   * 
   * @return The java.awt.Component that will be displayed
   */
/*  @Deprecated
  public java.awt.Component getComponent() {
    return mainOptionComponent;
  }*/
  
  /**
   * Sets the java.awt.Component that will be displayed when the option is selected.
   * 
   * @param The java.awt.Component that will be displayed
   */
/*  @Deprecated
  protected void setComponent(java.awt.Component _component) {
    mainOptionComponent = _component;
  }*/

  /**
   * Returns the Editor for the option.
   * 
   * @return The Editor
   */
  public Editor getEditor() {
    return optionEditor;
  }
  
}
