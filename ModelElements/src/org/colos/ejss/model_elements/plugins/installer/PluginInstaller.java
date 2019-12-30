/**
 * 
 */
package org.colos.ejss.model_elements.plugins.installer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JTextField;

import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.plugins.Plugin;
import org.colos.ejs.osejs.plugins.PluginButtonInfo;
import org.colos.ejs.osejs.plugins.PluginMainOptionInfo;
import org.colos.ejs.osejs.utils.TwoStrings;

/**
 * @author Jesús Chacón Sombría
 *
 */
public class PluginInstaller implements Plugin {
  static private List<TwoStrings> resources = new ArrayList<TwoStrings>();
  static private List<TwoStrings> systemResources = new ArrayList<TwoStrings>();
  static private List<TwoStrings> elementTips = new ArrayList<TwoStrings>();
  static private HashMap<String, ImageIcon> iconResources = new HashMap<String, ImageIcon>();
  
  private Osejs ejs;
  
  protected Vector<PluginButtonInfo> buttons = new Vector<PluginButtonInfo>();
  protected PluginInstallerButton installerButton = null;
  private Vector<PluginMainOptionInfo> mainOptions = new Vector<PluginMainOptionInfo>();
  private Vector<PluginMainOptionInfo> modelOptions = new Vector<PluginMainOptionInfo>();
  
  public PluginInstaller() {
    // Initialize class elements
  }

  @Override
  public void Initialize(Osejs _ejs) {
    ejs = _ejs;
    installerButton = new PluginInstallerButton(ejs);
    buttons.add(installerButton.getButtonInfo());
  }

  @Override
  public Vector<PluginMainOptionInfo> getMainOptions() {
    return mainOptions;
  }

  @Override
  public Vector<PluginMainOptionInfo> getModelOptions() {
    return modelOptions;
  }

  @Override
  public Vector<PluginButtonInfo> getBarButtons() {
    return buttons;
  }

  @Override
  public List<String> getHtmlViewElements() {
    return null;
  }

  @Override
  public List<TwoStrings> getResources() {
    return resources;
  }
  
  @Override
  public List<TwoStrings> getSystemResources() {
    return systemResources;
  }
  
  @Override
  public List<TwoStrings> getHtmlViewResources() {
    return null;
  }
  
  @Override
  public List<TwoStrings> getElementTips() {
    return elementTips;
  }
  
  @Override
  public HashMap<String, ImageIcon> getIconResources() {
    return iconResources;
  }

  @Override
  public String getHtmlViewElementInfo() {
    return null;
  }
  
  @Override
  public String getJSScripts() {
    return "";
  }

}