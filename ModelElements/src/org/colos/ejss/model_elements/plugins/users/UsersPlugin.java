/**
 * 
 */
package org.colos.ejss.model_elements.plugins.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;

import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.plugins.Plugin;
import org.colos.ejs.osejs.plugins.PluginButtonInfo;
import org.colos.ejs.osejs.plugins.PluginMainOptionInfo;
import org.colos.ejs.osejs.utils.TwoStrings;

/**
 * @author Jesús Chacón Sombría
 *
 */
public class UsersPlugin implements Plugin {
  static private List<TwoStrings> resources = new ArrayList<TwoStrings>();
  static private List<TwoStrings> systemResources = new ArrayList<TwoStrings>();
  static private List<TwoStrings> elementTips = new ArrayList<TwoStrings>();
  static private HashMap<String, ImageIcon> iconResources = new HashMap<String, ImageIcon>();
  
  private Osejs ejs;
  
  protected Vector<PluginButtonInfo> buttons = new Vector<PluginButtonInfo>();
  private Vector<PluginMainOptionInfo> mainOptions = new Vector<PluginMainOptionInfo>();
  private Vector<PluginMainOptionInfo> modelOptions = new Vector<PluginMainOptionInfo>();
  private UsersEditor usersEditor;
  
  {
    resources.add(new TwoStrings("Osejs.Main.Users", "Users"));
    resources.add(new TwoStrings("Osejs.Main.Users.ToolTip", "Manage Lab Users"));
    resources.add(new TwoStrings("Users.Color", "0,0,255"));
  }

  public UsersPlugin() {}

  @Override
  public void Initialize(Osejs _ejs) {
    ejs = _ejs;
    usersEditor = new UsersEditor(_ejs);
    mainOptions.add(new PluginMainOptionInfo("Users", usersEditor));
  }

  @Override public Vector<PluginMainOptionInfo> getMainOptions() { return mainOptions; }
  @Override public Vector<PluginMainOptionInfo> getModelOptions() { return modelOptions; }
  @Override public Vector<PluginButtonInfo> getBarButtons() { return buttons; }
  @Override public List<String> getHtmlViewElements() { return null; }
  @Override public List<TwoStrings> getResources() { return resources; }
  @Override public List<TwoStrings> getSystemResources() { return systemResources; }
  @Override public List<TwoStrings> getHtmlViewResources() { return null; }
  @Override public List<TwoStrings> getElementTips() { return elementTips; }
  @Override public HashMap<String, ImageIcon> getIconResources() { return iconResources; }
  @Override public String getHtmlViewElementInfo() { return null; }
  @Override public String getJSScripts() { return ""; }
}