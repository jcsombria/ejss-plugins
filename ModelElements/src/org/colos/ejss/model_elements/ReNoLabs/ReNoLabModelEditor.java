/**
 * 
 */
package org.colos.ejss.model_elements.ReNoLabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.ModelElement;
import org.colos.ejs.model_elements.ModelElementEditor;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.SearchResult;
import org.colos.ejs.osejs.edition.html_view.ElementEditor;
import org.colos.ejs.osejs.edition.html_view.ElementsTree;
import org.colos.ejs.osejs.edition.html_view.OneView;
import org.colos.ejs.osejs.edition.variables.ModelElementInformation;
import org.colos.ejs.osejs.utils.ResourceUtil;
import org.colos.ejss.xml.SimulationXML;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * @author Iñigo Aizpuru Rueda
 *
 */
public class ReNoLabModelEditor implements Editor {
  
  static private final String LAB_NAME = "lab";

  private JComponent mainPanel;
  private JList labListControl;
  private JPanel detailPanel;

  private org.colos.ejs.osejs.Osejs mEjs;

  private String name="";
  private boolean internal  = true;
  private boolean active  = true;
  private boolean changed = false;
  
  // Lab List Model
  private DefaultListModel<ReNoLabElement> labListModel = new DefaultListModel<ReNoLabElement>();

  /**
   * 
   */
  public ReNoLabModelEditor(org.colos.ejs.osejs.Osejs _ejs) {
    mEjs = _ejs;
    
    // ------------------------------
    // Detail panel
    // ------------------------------
    detailPanel = new JPanel(new BorderLayout());
    detailPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Connection Details", TitledBorder.LEADING, TitledBorder.BELOW_TOP));
    
    // ------------------------------
    // Connections panel
    // ------------------------------

    // Lab List
    labListControl = new JList<ReNoLabElement>(labListModel);
    labListControl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    labListControl.setLayoutOrientation(JList.VERTICAL);
    labListControl.setVisibleRowCount(-1);
    labListControl.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() == false) {
          // Remove previous panel if any
          BorderLayout layout = (BorderLayout) detailPanel.getLayout();
          if (layout.getLayoutComponent(BorderLayout.CENTER) != null)
            detailPanel.remove(layout.getLayoutComponent(BorderLayout.CENTER));

          // Add the panel of the selected item if any
          if (labListControl.getSelectedIndex() >= 0) {
            Component selectedComponent = labListModel.get(labListControl.getSelectedIndex()).getEditor();
            detailPanel.add(selectedComponent, BorderLayout.CENTER);
          }
          //detailPanel.paint(detailPanel.getGraphics());
          detailPanel.paintAll(detailPanel.getGraphics());
        }
      }
    });
    JScrollPane listScroller = new JScrollPane(labListControl);
//    listScroller.setPreferredSize(new Dimension(250, 80));
    
    // Add Lab
    JButton addLabButton = new JButton("AddLab");
    AbstractAction addLab = new AbstractAction("+"){
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
        ReNoLabElement lab = new ReNoLabElement(getUniqueName(LAB_NAME));
        AddLabToModel(lab);
        setChanged(true);
      }
    };
    addLabButton.setAction(addLab);
    
    // Remove Lab
    JButton removeLabButton = new JButton("RemoveLab");
    AbstractAction removeLab = new AbstractAction("-"){
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
        // Remove the selected item
        if (labListControl.getSelectedIndex() >= 0) {
          labListModel.remove(labListControl.getSelectedIndex());
          setChanged(true);
        }
        //detailPanel.paint(detailPanel.getGraphics());
        detailPanel.paintAll(detailPanel.getGraphics());
      }
    };
    removeLabButton.setAction(removeLab);

    JPanel buttonPanel = new JPanel(new GridLayout(0,1));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder());
    buttonPanel.add(addLabButton);
    buttonPanel.add(removeLabButton);

    JPanel actionPanel = new JPanel(new BorderLayout());
    actionPanel.setBorder(BorderFactory.createEmptyBorder());
    actionPanel.add(buttonPanel,BorderLayout.NORTH);

    JPanel connectionPanel = new JPanel(new BorderLayout());
    connectionPanel.add(listScroller,BorderLayout.CENTER);
    connectionPanel.add(actionPanel,BorderLayout.EAST);
    
    // ------------------------------
    // Server configuration main panel
    // ------------------------------
    mainPanel = new JPanel(new GridLayout(1,2));
    mainPanel.setBorder(new TitledBorder(null, "Lab Connections", TitledBorder.LEADING, TitledBorder.TOP));
    mainPanel.add(connectionPanel);
    mainPanel.add(detailPanel);
    mainPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    mainPanel.setPreferredSize(new Dimension(800,600));
  }

  /* (non-Javadoc)
   * @see org.colos.ejs.osejs.edition.Editor#setName(java.lang.String)
   */
  @Override
  public void setName(String _name) {
    name = _name;
  }

  /* (non-Javadoc)
   * @see org.colos.ejs.osejs.edition.Editor#getName()
   */
  @Override
  public String getName() {
    return name;
  }

  /* (non-Javadoc)
   * @see org.colos.ejs.osejs.edition.Editor#clear()
   */
  @Override
  public void clear() {
    labListModel.clear();
  }

  /* (non-Javadoc)
   * @see org.colos.ejs.osejs.edition.Editor#getComponent()
   */
  @Override
  public Component getComponent() {
    return mainPanel;
  }

  /* (non-Javadoc)
   * @see org.colos.ejs.osejs.edition.Editor#setColor(java.awt.Color)
   */
  @Override
  public void setColor(Color _color) {

  }

  /* (non-Javadoc)
   * @see org.colos.ejs.osejs.edition.Editor#setFont(java.awt.Font)
   */
  @Override
  public void setFont(Font _font) {

  }

  /* (non-Javadoc)
   * @see org.colos.ejs.osejs.edition.Editor#setZoomLevel(int)
   */
  @Override
  public void setZoomLevel(int level) {

  }

  /* (non-Javadoc)
   * @see org.colos.ejs.osejs.edition.Editor#isChanged()
   */
  @Override
  public boolean isChanged() {
    return changed;
  }

  /* (non-Javadoc)
   * @see org.colos.ejs.osejs.edition.Editor#setChanged(boolean)
   */
  @Override
  public void setChanged(boolean _changed) {
    changed = _changed;
  }

  /* (non-Javadoc)
   * @see org.colos.ejs.osejs.edition.Editor#isActive()
   */
  @Override
  public boolean isActive() {
    return active;
  }

  /* (non-Javadoc)
   * @see org.colos.ejs.osejs.edition.Editor#setActive(boolean)
   */
  @Override
  public void setActive(boolean _active) {
    active = _active;
  }

  /* (non-Javadoc)
   * @see org.colos.ejs.osejs.edition.Editor#isInternal()
   */
  @Override
  public boolean isInternal() {
    return internal;
  }

  /* (non-Javadoc)
   * @see org.colos.ejs.osejs.edition.Editor#setInternal(boolean)
   */
  @Override
  public void setInternal(boolean _internal) {
    internal = _internal;
  }

  /* (non-Javadoc)
   * @see org.colos.ejs.osejs.edition.Editor#fillSimulationXML(org.colos.ejss.xml.SimulationXML)
   */
  @Override
  public void fillSimulationXML(SimulationXML _simXML) {
    String initCode = "" +
      "function getLabKey() {\n" +
      "  if (typeof(LAB_KEY)==='undefined')\n" +
      "    return undefined;\n" +
      "\n" +
      "  return LAB_KEY;\n" +
      "}\n";
/*    String logoutCode = "" +
        "//<-- Add LOGOUT DIV -->\n"+
        "var logoutDiv = document.getElementById(\"logout-block\");\n" +
        "if(!logoutDiv) {\n" +
        "  logoutDiv = document.createElement(\"div\");\n" +
        "  logoutDiv.id = \"logout-block\";\n" +
        "  logoutDiv.className = \"logout-block\";\n" +
        "  logoutDiv.innerHTML = \"<a href='/select'><img src='/images/user_icon.png' alt='REAL' height='35' width='35' /></a><span> <%= user.username %> </span><a href='/logout'><button>Logout</button></a>\";\n" +
        "  var bodyTag = document.getElementsByTagName(\"body\")[0];\n" +
        "  var child = document.getElementById(\"_topFrame\");\n" +
        "  bodyTag.insertBefore(logoutDiv, child);\n" +
        "}\n"+
        "//<-- END Add LOGOUT DIV -->\n\n" +
        "var EJSS_RENOLABS = EJSS_RENOLABS || {};\n";*/
    _simXML.addModelElement("", "", initCode);
    for (int i = 0; i < labListModel.size(); i++) {
      ReNoLabElement element = labListModel.get(i);
        String sourceCode = element.getSourceCode();
        _simXML.addModelElement("","", sourceCode);
    }
  }

  /* (non-Javadoc)
   * @see org.colos.ejs.osejs.edition.Editor#generateCode(int, java.lang.String)
   */
  @Override
  public StringBuffer generateCode(int _type, String _info) {
    StringBuffer sb = new StringBuffer();
    return sb;
  }

  /* (non-Javadoc)
   * @see org.colos.ejs.osejs.edition.Editor#saveStringBuffer()
   */
  @Override
  public StringBuffer saveStringBuffer() {
    StringBuffer save = new StringBuffer();
    String renolabPrefix = "ReNoLabs.Model";
    String labPrefix = "Lab.Element";

    save.append("<"+renolabPrefix+">\n");
    for (int i = 0; i < labListModel.size(); i++) {
      ReNoLabElement element = labListModel.get(i);
      save.append("<"+labPrefix+">\n");
      save.append("<"+labPrefix+".Name>"+element.getName()+"</"+labPrefix+".Name>\n");
      String configuration = element.savetoXML();
      if (configuration!=null) {
        save.append("<"+labPrefix+".Configuration>\n");
        save.append(configuration);
        save.append("</"+labPrefix+".Configuration>\n");
      }
      save.append("</"+labPrefix+">\n");
    }
    save.append("</"+renolabPrefix+">\n");
    return save;
  }

  /* (non-Javadoc)
   * @see org.colos.ejs.osejs.edition.Editor#readString(java.lang.String)
   */
  @Override
  public void readString(String _input) {
    String renolabPrefix = "ReNoLabs.Model";
    String labPrefix = "Lab.Element";

    if (_input.indexOf("<"+renolabPrefix+">\n") >= 0) {
      int labPrefixLength = labPrefix.length()+3;
      int begin = _input.indexOf("<"+labPrefix+">\n");
      while (begin>=0) {
        int end = _input.indexOf("</"+labPrefix+">\n");
        String piece = _input.substring(begin+labPrefixLength,end);
        String elementName = OsejsCommon.getPiece(piece,"<"+labPrefix+".Name>","</"+labPrefix+".Name>\n",false);
        String elementXML = OsejsCommon.getPiece(piece,"<"+labPrefix+".Configuration>\n","</"+labPrefix+".Configuration>\n",false);
        if (elementXML!=null) {
          ReNoLabElement element = new ReNoLabElement(elementName);
          element.readfromXML(elementXML);
          AddLabToModel(element);
        }
        _input = _input.substring(end+labPrefixLength+1);
        begin = _input.indexOf("<"+labPrefix+">\n");
      }
    }
    setChanged(false);
  }

  /* (non-Javadoc)
   * @see org.colos.ejs.osejs.edition.Editor#search(java.lang.String, java.lang.String, int)
   */
  @Override
  public List<SearchResult> search(String _info, String _searchString, int _mode) {
    return null;
  }
  
  public boolean nameExists(String _name) {
    for (int i = 0, n=labListModel.size(); i<n; i++) {
      ReNoLabElement element  = labListModel.getElementAt(i);
      if (_name.equals(element.getName())) return true;
    }
    return false;
  }

  private String getUniqueName (String name) {
    name = OsejsCommon.getValidIdentifier(name.trim());
    int i=1;
    String newname = name + i;
    while (mEjs.getModelEditor().getVariablesEditor().nameExists(newname) ||
           mEjs.getModelEditor().getElementsEditor().nameExists(newname) ||
           nameExists(newname)) newname = name + (++i);
    return newname;
  }
  
  private void AddLabToModel(ReNoLabElement lab) {
    lab.addListDataListener(new ChangeListener() {
      private static final long serialVersionUID = 1L;
      @Override
      public void stateChanged(ChangeEvent e) {
        labListModel.setElementAt((ReNoLabElement) e.getSource(), labListModel.indexOf(e.getSource()));
        setChanged(true);
      }
    });
    labListModel.addElement(lab);
  }
}
