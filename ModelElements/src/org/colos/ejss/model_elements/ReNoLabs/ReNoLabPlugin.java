/**
 * 
 */
package org.colos.ejss.model_elements.ReNoLabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.colos.ejs.library.resources.ejs_res;
import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.ModelElementEditor;
import org.colos.ejs.model_elements.ModelElementSearch;
import org.colos.ejs.model_elements.ModelElementTabbedEditor;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.osejs.GenerateJS;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.plugins.Plugin;
import org.colos.ejs.osejs.edition.CodeEditor;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.html_view.ElementsTree;
import org.colos.ejs.osejs.edition.html_view.OneView;
import org.colos.ejs.osejs.utils.FileUtils;
import org.colos.ejs.osejs.utils.ResourceUtil;
import org.colos.ejs.osejs.utils.TwoStrings;
import org.colos.ejs.osejs.plugins.PluginButtonInfo;
import org.colos.ejs.osejs.plugins.PluginMainOptionInfo;
import org.colos.ejs.osejs.plugins.PluginRightClickOptionInfo;
import org.colos.ejss.model_elements.input_output.WebSocketElement;
import org.colos.ejss.xml.JSObfuscator;
import org.colos.ejss.xml.XMLTransformerJava;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensourcephysics.tools.JarTool;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

import io.socket.client.IO;
import io.socket.client.IO.Options;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * @author Iñigo Aizpuru Rueda
 *
 */
public class ReNoLabPlugin implements Plugin {
  static private ImageIcon LABCONTROL_ICON = AbstractModelElement.createImageIcon("org/colos/ejss/model_elements/ReNoLabs/ReNoLabControl.png"); // This icon is included in this jar
  static private ImageIcon LABLOGIN_ICON = AbstractModelElement.createImageIcon("org/colos/ejss/model_elements/ReNoLabs/ReNoLabLogin.png"); // This icon is included in this jar
  static private ImageIcon LABFUNCTION_ICON = AbstractModelElement.createImageIcon("org/colos/ejss/model_elements/ReNoLabs/ReNoLabFunction.png"); // This icon is included in this jar
  static private ImageIcon BUTTON_ICON = AbstractModelElement.createImageIcon("org/colos/ejss/model_elements/ReNoLabs/ReNoLabs.png");

  static private List<TwoStrings> resources = new ArrayList<TwoStrings>();
  static private List<TwoStrings> systemResources = new ArrayList<TwoStrings>();
  static private List<TwoStrings> htmlViewResources = new ArrayList<TwoStrings>();
  static private List<TwoStrings> elementTips = new ArrayList<TwoStrings>();

  static private HashMap<String, ImageIcon> iconResources = new HashMap<String, ImageIcon>();

  static private List<String> htmlViewElements = new ArrayList<String>();
  static private StringBuilder htmlViewElementInfo = new StringBuilder();
  static private String reNoLabsJSScript = "";
  
  private Osejs ejs;
  
  protected ReNoLabDeploymentButton deployButton = null;
  protected Vector<PluginButtonInfo> buttons = new Vector<PluginButtonInfo>();
  protected ReNoLabControllerEditor controllerEditor = null;
  protected ReNoLabModelEditor modelEditor = null;
  protected JTextField mAddressField = new JTextField();  // needs to be created to avoid null references
  protected JTextField mPortField = new JTextField();  // needs to be created to avoid null references
  protected ProgrammableTabbedPane mCodeEditor = null;  // The editor for the code

  private Vector<PluginMainOptionInfo> mainOptions = new Vector<PluginMainOptionInfo>();
  private Vector<PluginMainOptionInfo> modelOptions = new Vector<PluginMainOptionInfo>();
  
  public static final int CONNECTION_TIMEOUT_SECONDS = 10;

  static {
    iconResources.put("Reno15x15", new ImageIcon(BUTTON_ICON.getImage().getScaledInstance(15, 15, java.awt.Image.SCALE_SMOOTH)));
    
    iconResources.put("Control24x24", new ImageIcon(LABCONTROL_ICON.getImage().getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH)));
    iconResources.put("Login24x24", new ImageIcon(LABLOGIN_ICON.getImage().getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH)));
    iconResources.put("Function24x24", new ImageIcon(LABFUNCTION_ICON.getImage().getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH)));

    resources.add(new TwoStrings("View.Elements.Groups.ReNoLabs", "ReNoLabs"));
    resources.add(new TwoStrings("View.Elements.Groups.ReNoLabs.ToolTip", "ReNoLabs Controls"));
    resources.add(new TwoStrings("View.Elements.ReNoLabs", "ReNoLabs Controls"));
    systemResources.add(new TwoStrings("View.Elements.Groups.ReNoLabs.Icon", "Reno15x15"));

    htmlViewElementInfo.append("class:EJSS_INTERFACE.RENOLABS.labControl\n");
    htmlViewElementInfo.append("parent:EJSS_INTERFACE.element\n");
    htmlViewElementInfo.append("name:'Lab',                   info: 'String',            def: '<none>'\n");
    htmlViewElementInfo.append("name:'ButtonWidth',           info: 'int|String',        def: '120px'\n");
    htmlViewElementInfo.append("name:'ButtonHeight',          info: 'int|String',        def: '50px'\n");
    htmlViewElementInfo.append("defaults:ButtonWidth=\"120px\";ButtonHeight=\"50px\"\n");
    htmlViewElementInfo.append("edition:<COLUMN>; <LABELConfiguration>; Lab;\n");
    htmlViewElementInfo.append("edition:<COLUMN>; <LABELAspect>; ButtonWidth; ButtonHeight;\n");
    
    htmlViewElementInfo.append("class:EJSS_INTERFACE.RENOLABS.labFunctionParameter\n");
    htmlViewElementInfo.append("parent:EJSS_INTERFACE.element\n");
    htmlViewElementInfo.append("name:'Lab',                   info: 'String',            def: '<none>'\n");
    htmlViewElementInfo.append("name:'Config',                info: 'String',            def: '<none>'\n");
    htmlViewElementInfo.append("name:'Width',                 info: 'int|String',        def: '350px'\n");
    htmlViewElementInfo.append("name:'Height',                info: 'int|String',        def: '<none>'\n");
    htmlViewElementInfo.append("name:'ColumnWidth',           info: 'int|String',        def: '120px'\n");
    htmlViewElementInfo.append("name:'RowHeight',             info: 'int|String',        def: '50px'\n");
    htmlViewElementInfo.append("name:'Font',                  info: 'String|Font',       def: 'inherit'\n");
    htmlViewElementInfo.append("defaults:ColumnWidth=\"120px\";RowHeight=\"50px\"\n");
    htmlViewElementInfo.append("edition:<COLUMN>; <LABELConfiguration>; Lab; Config;\n");
    htmlViewElementInfo.append("edition:<COLUMN>; <LABELAspect>; Width; Height; <SEP>; ColumnWidth; RowHeight; Font;\n");

/*
    htmlViewElementInfo.append("class:EJSS_INTERFACE.RENOLABS.labWaveFunctions\n");
    htmlViewElementInfo.append("parent:EJSS_INTERFACE.element\n");
    htmlViewElementInfo.append("name:'Lab',                   info: 'String',            def: '<none>'\n");
    htmlViewElementInfo.append("name:'Width',                 info: 'int|String',        def: '350px'\n");
    htmlViewElementInfo.append("name:'Height',                info: 'int|String',        def: '<none>'\n");
    htmlViewElementInfo.append("name:'ColumnWidth',           info: 'int|String',        def: '120px'\n");
    htmlViewElementInfo.append("name:'RowHeight',             info: 'int|String',        def: '50px'\n");
    htmlViewElementInfo.append("name:'Font',                  info: 'String|Font',       def: 'inherit'\n");
    htmlViewElementInfo.append("defaults:ColumnWidth=\"120px\";RowHeight=\"50px\"\n");
    htmlViewElementInfo.append("edition:<COLUMN>; <LABELConfiguration>; Lab;\n");
    htmlViewElementInfo.append("edition:<COLUMN>; <LABELAspect>; Width; Height; <SEP>; ColumnWidth; RowHeight; Font;\n");

    htmlViewElementInfo.append("class:EJSS_INTERFACE.RENOLABS.labControllers\n");
    htmlViewElementInfo.append("parent:EJSS_INTERFACE.element\n");
    htmlViewElementInfo.append("name:'Lab',                   info: 'String',            def: '<none>'\n");
    htmlViewElementInfo.append("name:'Width',                 info: 'int|String',        def: '350px'\n");
    htmlViewElementInfo.append("name:'Height',                info: 'int|String',        def: '<none>'\n");
    htmlViewElementInfo.append("name:'ColumnWidth',           info: 'int|String',        def: '120px'\n");
    htmlViewElementInfo.append("name:'RowHeight',             info: 'int|String',        def: '50px'\n");
    htmlViewElementInfo.append("name:'Font',                  info: 'String|Font',       def: 'inherit'\n");
    htmlViewElementInfo.append("defaults:ColumnWidth=\"120px\";RowHeight=\"50px\"\n");
    htmlViewElementInfo.append("edition:<COLUMN>; <LABELConfiguration>; Lab;\n");
    htmlViewElementInfo.append("edition:<COLUMN>; <LABELAspect>; Width; Height; <SEP>; ColumnWidth; RowHeight; Font;\n");
*/

    htmlViewElementInfo.append("class:EJSS_INTERFACE.RENOLABS.labLogin\n");
    htmlViewElementInfo.append("parent:EJSS_INTERFACE.element\n");
    htmlViewElementInfo.append("name:'Labs',                  info: 'Object|Object[]',   def: '<none>'\n");
    htmlViewElementInfo.append("name:'Font',                  info: 'String|Font',       def: 'inherit'\n");
    htmlViewElementInfo.append("edition:<COLUMN>; <LABELConfiguration>; Labs;\n");
    htmlViewElementInfo.append("edition:<COLUMN>; <LABELAspect>; Font;\n");

    htmlViewElements.add("ReNoLabs");
    
    htmlViewResources.add(new TwoStrings("ReNoLabs", "LabControl LabFunctionParameter LabLogin"));//, "ReNoLabs.group"));
    //htmlViewResources.add(new TwoStrings("ReNoLabs", "LabControl LabWaveFunctions LabControllers LabFunctionParameter LabLogin"));//, "ReNoLabs.group"));
    //htmlViewResources.add(new TwoStrings("ReNoLabs.group", "LabControl LabWaveFunctions LabControllers"));
    htmlViewResources.add(new TwoStrings("Elements.LabControl", "EJSS_INTERFACE.RENOLABS.labControl"));
//    htmlViewResources.add(new TwoStrings("Elements.LabWaveFunctions", "EJSS_INTERFACE.RENOLABS.labWaveFunctions"));
//    htmlViewResources.add(new TwoStrings("Elements.LabControllers", "EJSS_INTERFACE.RENOLABS.labControllers"));
    htmlViewResources.add(new TwoStrings("Elements.LabFunctionParameter", "EJSS_INTERFACE.RENOLABS.labFunctionParameter"));
    htmlViewResources.add(new TwoStrings("Elements.LabLogin", "EJSS_INTERFACE.RENOLABS.labLogin"));

    elementTips.add(new TwoStrings("LabControl.Name", "LabControl"));
    elementTips.add(new TwoStrings("LabControl.ToolTip", "Button bar for controlling the execution of the remote laboratory"));
    elementTips.add(new TwoStrings("LabControl.Icon", "Control24x24"));

/*
    elementTips.add(new TwoStrings("LabWaveFunctions.Name", "LabWaveFunctions"));
    elementTips.add(new TwoStrings("LabWaveFunctions.ToolTip", "Wave function selection control"));
    elementTips.add(new TwoStrings("LabWaveFunctions.Icon", "Reno24x24"));

    elementTips.add(new TwoStrings("LabControllers.Name", "LabControllers"));
    elementTips.add(new TwoStrings("LabControllers.ToolTip", "Controller selection control"));
    elementTips.add(new TwoStrings("LabControllers.Icon", "Reno24x24"));
*/

    elementTips.add(new TwoStrings("LabFunctionParameter.Name", "LabFunctionParameter"));
    elementTips.add(new TwoStrings("LabFunctionParameter.ToolTip", "Autoconfigurable function selection control"));
    elementTips.add(new TwoStrings("LabFunctionParameter.Icon", "Function24x24"));

    elementTips.add(new TwoStrings("LabLogin.Name", "LabLogin"));
    elementTips.add(new TwoStrings("LabLogin.ToolTip", "Lab login control"));
    elementTips.add(new TwoStrings("LabLogin.Icon", "Login24x24"));

    resources.add(new TwoStrings("FileTab.Page", "FileName.c"));
    
    // Main Option - Deployment
    resources.add(new TwoStrings("Osejs.Main.Deployment", "Deployment"));
    resources.add(new TwoStrings("Osejs.Main.Deployment.ToolTip", "Remote Lab Deployment"));
    resources.add(new TwoStrings("Deployment.Color", "0,0,0"));
    
    // Main Option - Controller
    resources.add(new TwoStrings("Osejs.Main.Controller", "Controller"));
    resources.add(new TwoStrings("Osejs.Main.Controller.ToolTip", "Remote Controller"));
    resources.add(new TwoStrings("Controller.Color", "0,0,0"));
    resources.add(new TwoStrings("Model.ReNoLabModelEditor", "Remote Lab"));
    resources.add(new TwoStrings("Model.ReNoLabModelEditor.ToolTip", "Remote laboratory model editor"));

    StringBuffer buffer = new StringBuffer();
    Resource res = ResourceLoader.getResource("org/colos/ejss/model_elements/ReNoLabs/socket.io.js");
    if (res==null) {
      System.err.println ("Error: ReNoLabs script file is missing : "+"org/colos/ejss/model_elements/ReNoLabs/ReNoLabsJSScript.js");
    }
    else {
      BufferedReader in = new BufferedReader(res.openReader());
      try {
        for (String line; (line = in.readLine()) != null;) {
          buffer.append(line).append("\n");
        }
      } catch (IOException e) {
      }
    }
    
    res = ResourceLoader.getResource("org/colos/ejss/model_elements/ReNoLabs/ReNoLabsJSScript.js");
    if (res==null) {
      System.err.println ("Error: ReNoLabs script file is missing : "+"org/colos/ejss/model_elements/ReNoLabs/ReNoLabsJSScript.js");
    }
    else {
      BufferedReader in = new BufferedReader(res.openReader());
      try {
        for (String line; (line = in.readLine()) != null;) {
          buffer.append(line).append("\n");
        }
      } catch (IOException e) {
      }
    }
    reNoLabsJSScript = buffer.toString();
  }

  /**
   * 
   */
  public ReNoLabPlugin() {
    // Initialize class elements
  }

  @Override
  public void Initialize(Osejs _ejs) {
    ejs = _ejs;

    // ------------------------------
    // Buttons
    // ------------------------------
    deployButton = new ReNoLabDeploymentButton(ejs);
    buttons.add(deployButton.getButtonInfo());
    
    // ------------------------------
    // Model editor panel
    // ------------------------------
    modelEditor = new ReNoLabModelEditor(_ejs);
    modelOptions.add(new PluginMainOptionInfo("ReNoLabModelEditor", modelEditor));
    
    // ------------------------------
    // Controller panel
    // ------------------------------
    controllerEditor = new ReNoLabControllerEditor(_ejs);
    mainOptions.add(new PluginMainOptionInfo("Controller", controllerEditor));
  }

//  @Override
//  public String getMainOptionId() {
//    return "ReNoLab";
//  }
//
//  @Override
//  public Component getMainOptionComponent(String _optionId) {
//    return mainPanel;
//  }
  
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
/*
    Runnable leftClickAction = new Runnable() {
      
      @Override
      public void run() {
        JOptionPane.showMessageDialog(null, "Button left clicked", "Information", JOptionPane.INFORMATION_MESSAGE);
      }
    };
    
    Vector<PluginRightClickOptionInfo> rightClickActions = new Vector<>();
    PluginRightClickOptionInfo rcInfo = new PluginRightClickOptionInfo("Option 1", new Runnable() {
      
      @Override
      public void run() {
        JOptionPane.showMessageDialog(null, "Option 1 clicked", "Information", JOptionPane.INFORMATION_MESSAGE);
      }
    });
    rightClickActions.add(rcInfo);
    rightClickActions.add(null);
    rcInfo = new PluginRightClickOptionInfo("Option 2", new Runnable() {
      
      @Override
      public void run() {
        JOptionPane.showMessageDialog(null, "Option 2 clicked", "Information", JOptionPane.INFORMATION_MESSAGE);
      }
    });
    rightClickActions.add(rcInfo);
    
    Vector<PluginButtonInfo> buttons = new Vector<PluginButtonInfo>();
    PluginButtonInfo pbi = new PluginButtonInfo(BUTTON_ICON, "RenoLab Button 1", leftClickAction, rightClickActions);
    buttons.add(pbi);
    return buttons;
*/
  }

  @Override
  public List<String> getHtmlViewElements() {
    return htmlViewElements;
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
    return htmlViewResources;
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
    return htmlViewElementInfo.toString();
  }
  
  @Override
  public String getJSScripts() {
    return reNoLabsJSScript;
  }

}
