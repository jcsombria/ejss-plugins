package org.colos.ejs.osejs.plugins;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;

import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.utils.TwoStrings;

/**
 * This interface declares the API for a class that can be used to extend EJS features like:
 * 1.- Adding new main options
 * 2.- Adding custom buttons to the icon bar
 * 
 * EJS will automatically add the functionality of the plugin classes that implement this interface
 * contained in the JAR files located under the 'config/CustomPlugins' folder of the workspace.
 * 
 * @author Iñigo Aizpuru
 * @version 2.0 March 2018
 */
public interface Plugin {
  
  public void Initialize(Osejs _ejs);
  
  // -------------------------------
  // Main options extension
  // -------------------------------

  /**
   * Returns the Vector containing the PluginMainOptionInfo of all the main options that this specific
   * Plugin adds to the main option bar.
   * 
   * @return A Vector of PluginMainOptionInfo
   */
  public Vector<PluginMainOptionInfo> getMainOptions();
  
  /**
   * Returns the Vector containing the PluginMainOptionInfo of all the model options that this specific
   * Plugin adds to the model option bar.
   * 
   * @return A Vector of PluginMainOptionInfo
   */
  public Vector<PluginMainOptionInfo> getModelOptions();
  
  // -------------------------------
  // Icon bar extension
  // -------------------------------

  /**
   * Returns the Vector containing the PluginButtonInfo of all the buttons that this specific
   * Plugin adds to the icon bar.
   * 
   * @return A Vector of PluginButtonInfo
   */
  Vector<PluginButtonInfo> getBarButtons();

  // -------------------------------
  // Html control extension
  // -------------------------------

  /**
   * Returns the Vector containing the PluginHtmlControlSection of all the buttons that this specific
   * Plugin adds to the Html control elements.
   * 
   * @return A Vector of PluginHtmlControlSection
   */
//  Vector<PluginHtmlControlSection> getHtmlControlSections();
  
  List<String> getHtmlViewElements();
  
  List<TwoStrings> getResources();
  List<TwoStrings> getSystemResources();
  List<TwoStrings> getHtmlViewResources();
  List<TwoStrings> getElementTips();
  
  HashMap<String, ImageIcon> getIconResources();

  String getHtmlViewElementInfo();

  String getJSScripts();
}
