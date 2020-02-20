///**
// * 
// */
//package org.colos.ejss.model_elements.plugins.installer;
//
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Component;
//import java.awt.Dimension;
//import java.awt.FlowLayout;
//import java.awt.Font;
//import java.awt.GridLayout;
//import java.awt.event.ActionEvent;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.io.File;
//import java.util.List;
//import java.util.Map;
//import java.util.Vector;
//
//import javax.swing.AbstractAction;
//import javax.swing.BorderFactory;
//import javax.swing.BoxLayout;
//import javax.swing.DefaultListModel;
//import javax.swing.ImageIcon;
//import javax.swing.JButton;
//import javax.swing.JComponent;
//import javax.swing.JFileChooser;
//import javax.swing.JLabel;
//import javax.swing.JList;
//import javax.swing.JOptionPane;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.JSeparator;
//import javax.swing.JSplitPane;
//import javax.swing.JTable;
//import javax.swing.JTextArea;
//import javax.swing.ListCellRenderer;
//import javax.swing.ListSelectionModel;
//import javax.swing.border.EtchedBorder;
//import javax.swing.border.TitledBorder;
//import javax.swing.event.TableModelEvent;
//import javax.swing.event.TableModelListener;
//import javax.swing.filechooser.FileNameExtensionFilter;
//import javax.swing.table.DefaultTableModel;
//
//import org.colos.ejs.osejs.edition.Editor;
//import org.colos.ejs.osejs.edition.SearchResult;
//import org.colos.ejss.xml.SimulationXML;
//
///**
// * @author Jesús Chacón Sombría
// */
//public class PluginInstallerModelEditor implements Editor {
//  
//private static final String PLUGIN_RESTART_EJS = "Plugin %s %s. Please restart EjsS.";
//private static final String PLUGIN_SUCCESFULLY_INSTALLED = "Plugin %s succesfully installed";
//
////  static private final String LAB_NAME = "lab";
//  class PluginTableModel extends DefaultTableModel {
//    private final String[] COLUMNS = {"Enabled", "Jar File"};
//
//      public void setDataVector(Object[][] dataVector) {
//        setDataVector(dataVector, COLUMNS);
//      }
//  
//      public Class<?> getColumnClass(int c) {
//        return getValueAt(0, c).getClass();
//      }
//
//      public boolean isCellEditable(int row, int column) {
//        return (column == 0);
//      }
//  }
//
//  private JComponent mainPanel;
//  private JTable pluginTable;
//  private JPanel detailPanel;
//
//  private org.colos.ejs.osejs.Osejs mEjs;
//
//  private String name="";
//  private boolean internal  = true;
//  private boolean active  = true;
//  private boolean changed = false;
//  
//  // Lab List Model
//  private PluginTableModel pluginTableModel = new PluginTableModel();
//  private PluginUpdater pluginChecker;
//  private JList pluginDetail;
//  private DefaultListModel<Map<String, Object>> pluginDetailModel = new DefaultListModel<>();
//  /**
//   * 
//   */
//  public PluginInstallerModelEditor(org.colos.ejs.osejs.Osejs _ejs) {
//    mEjs = _ejs;
//
//    // ------------------------------
//    // Detail panel
//    // ------------------------------
//    detailPanel = new JPanel(new BorderLayout());
//    
//    detailPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Plugin Info", TitledBorder.LEADING, TitledBorder.BELOW_TOP));
//    pluginDetail = new JList(pluginDetailModel);
//    pluginDetail.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
//    pluginDetail.setLayoutOrientation(JList.VERTICAL_WRAP);
//    pluginDetail.setVisibleRowCount(-1);
//    pluginDetail.setCellRenderer(new PluginInfoRenderer());
//    detailPanel.add(pluginDetail);
//    // ------------------------------
//    // Connections panel
//    // ------------------------------
//
//    // Plugin List
//    pluginTableModel.setDataVector(new Object[][] {{new Boolean(true), ""}});
//    pluginTable = new JTable(pluginTableModel);
//    pluginTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//    pluginTable.addMouseListener(new MouseAdapter() {
//      public void mousePressed (MouseEvent _evt) {
//          updatePluginInfo();
//      }
//    });
//    JScrollPane listScroller = new JScrollPane(pluginTable);
//    listScroller.setPreferredSize(new Dimension(250, 80));
//
////    // Add Lab
//    JButton addLabButton = new JButton("AddLab");
//    AbstractAction addLab = new AbstractAction("+"){
//      private static final long serialVersionUID = 1L;
//      public void actionPerformed(ActionEvent e) {
//        updatePluginList();
//        JFileChooser chooser = new JFileChooser();
//        FileNameExtensionFilter filter = new FileNameExtensionFilter("jar", "zip");
//        chooser.setFileFilter(filter);
//        int returnVal = chooser.showOpenDialog(mainPanel);
//        if(returnVal == JFileChooser.APPROVE_OPTION) {
//          File f = chooser.getSelectedFile();
//          pluginChecker.install(f);
//          String message = String.format(PLUGIN_SUCCESFULLY_INSTALLED, f.getName());
//          JOptionPane.showMessageDialog(null, message);
//        }
//      }
//    };
//    addLabButton.setAction(addLab);
//
//    JPanel buttonPanel = new JPanel(new GridLayout(0,1));
//    buttonPanel.setBorder(BorderFactory.createEmptyBorder());
//    buttonPanel.add(addLabButton);
//
//    JPanel actionPanel = new JPanel(new BorderLayout());
//    actionPanel.setBorder(BorderFactory.createEmptyBorder());
//    actionPanel.add(buttonPanel,BorderLayout.NORTH);
//
//    JPanel connectionPanel = new JPanel(new BorderLayout());
//    connectionPanel.add(listScroller,BorderLayout.CENTER);
//    connectionPanel.add(actionPanel,BorderLayout.EAST);
//    
////Create a split pane with the two scroll panes in it.
////    splitPane.setOneTouchExpandable(true);
////
////    //Provide minimum sizes for the two components in the split pane
////    Dimension minimumSize = new Dimension(100, 50);
////    listScrollPane.setMinimumSize(minimumSize);
////    pictureScrollPane.setMinimumSize(minimumSize);
//    
//    // ------------------------------
//    // Server configuration main panel
//    // ------------------------------
//    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, connectionPanel, detailPanel);
//    splitPane.setDividerLocation(300);
//    mainPanel = new JPanel(new GridLayout(1,1));
//    mainPanel.setBorder(new TitledBorder(null, "Lab Connections", TitledBorder.LEADING, TitledBorder.TOP));
//    mainPanel.add(splitPane);
//    mainPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
//    mainPanel.setPreferredSize(new Dimension(800, 600));
//    
//    // TO DO: Encapsulate better
//    String path = mEjs.getConfigDirectory() + "/CustomPlugins";
//    pluginChecker = new PluginUpdater(new File(path));
//    updatePluginList();
//  }
//
//  /* (non-Javadoc)
//   * @see org.colos.ejs.osejs.edition.Editor#setName(java.lang.String)
//   */
//  @Override
//  public void setName(String _name) {
//    name = _name;
//  }
//
//  /* (non-Javadoc)
//   * @see org.colos.ejs.osejs.edition.Editor#getName()
//   */
//  @Override
//  public String getName() {
//    return name;
//  }
//
//  /* (non-Javadoc)
//   * @see org.colos.ejs.osejs.edition.Editor#clear()
//   */
//  @Override
//  public void clear() {
//    pluginTableModel.getDataVector().clear();
//  }
//
//  /* (non-Javadoc)
//   * @see org.colos.ejs.osejs.edition.Editor#getComponent()
//   */
//  @Override
//  public Component getComponent() {
//    return mainPanel;
//  }
//
//  /* (non-Javadoc)
//   * @see org.colos.ejs.osejs.edition.Editor#setColor(java.awt.Color)
//   */
//  @Override
//  public void setColor(Color _color) {
//
//  }
//
//  /* (non-Javadoc)
//   * @see org.colos.ejs.osejs.edition.Editor#setFont(java.awt.Font)
//   */
//  @Override
//  public void setFont(Font _font) {
//
//  }
//
//  /* (non-Javadoc)
//   * @see org.colos.ejs.osejs.edition.Editor#setZoomLevel(int)
//   */
//  @Override
//  public void setZoomLevel(int level) {
//
//  }
//
//  /* (non-Javadoc)
//   * @see org.colos.ejs.osejs.edition.Editor#isChanged()
//   */
//  @Override
//  public boolean isChanged() {
//    return changed;
//  }
//
//  /* (non-Javadoc)
//   * @see org.colos.ejs.osejs.edition.Editor#setChanged(boolean)
//   */
//  @Override
//  public void setChanged(boolean _changed) {
//    changed = _changed;
//  }
//
//  /* (non-Javadoc)
//   * @see org.colos.ejs.osejs.edition.Editor#isActive()
//   */
//  @Override
//  public boolean isActive() {
//    return active;
//  }
//
//  /* (non-Javadoc)
//   * @see org.colos.ejs.osejs.edition.Editor#setActive(boolean)
//   */
//  @Override
//  public void setActive(boolean _active) {
//    active = _active;
//  }
//
//  /* (non-Javadoc)
//   * @see org.colos.ejs.osejs.edition.Editor#isInternal()
//   */
//  @Override
//  public boolean isInternal() {
//    return internal;
//  }
//
//  /* (non-Javadoc)
//   * @see org.colos.ejs.osejs.edition.Editor#setInternal(boolean)
//   */
//  @Override
//  public void setInternal(boolean _internal) {
//    internal = _internal;
//  }
//
//  /* (non-Javadoc)
//   * @see org.colos.ejs.osejs.edition.Editor#fillSimulationXML(org.colos.ejss.xml.SimulationXML)
//   */
//  @Override
//  public void fillSimulationXML(SimulationXML _simXML) {
//  }
//
//  /* (non-Javadoc)
//   * @see org.colos.ejs.osejs.edition.Editor#generateCode(int, java.lang.String)
//   */
//  @Override
//  public StringBuffer generateCode(int _type, String _info) {
//    StringBuffer sb = new StringBuffer();
//    return sb;
//  }
//
//  /* (non-Javadoc)
//   * @see org.colos.ejs.osejs.edition.Editor#saveStringBuffer()
//   */
//  @Override
//  public StringBuffer saveStringBuffer() {
//    StringBuffer save = new StringBuffer();
//    return save;
//  }
//
//  /* (non-Javadoc)
//   * @see org.colos.ejs.osejs.edition.Editor#readString(java.lang.String)
//   */
//  @Override
//  public void readString(String _input) {
//  }
//
//  /* (non-Javadoc)
//   * @see org.colos.ejs.osejs.edition.Editor#search(java.lang.String, java.lang.String, int)
//   */
//  @Override
//  public List<SearchResult> search(String _info, String _searchString, int _mode) {
//    return null;
//  }
//  
//  public boolean nameExists(String _name) {
////    for (int i = 0, n=labListModel.size(); i<n; i++) {
////      ReNoLabElement element  = labListModel.getElementAt(i);
////      if (_name.equals(element.getName())) return true;
////    }
//    return false;
//  }
//
////  private String getUniqueName (String name) {
////    name = OsejsCommon.getValidIdentifier(name.trim());
////    int i=1;
////    String newname = name + i;
////    while (mEjs.getModelEditor().getVariablesEditor().nameExists(newname) ||
////           mEjs.getModelEditor().getElementsEditor().nameExists(newname) ||
////           nameExists(newname)) newname = name + (++i);
////    return newname;
////  }
//  
//  private void updatePluginList() {
//    clear();
//    for(PluginInfo p : pluginChecker.getPluginList()) {
//      Object[] data = {p.enabled, p.name};
//      pluginTableModel.addRow(data);
//    }
//    pluginTable.getColumnModel().getColumn(0).setPreferredWidth(200);
//    pluginTable.getColumnModel().getColumn(1).setPreferredWidth(800);
//
//    pluginTableModel.addTableModelListener(new TableModelListener() {
//      @Override
//      public void tableChanged(TableModelEvent e) {
//          if(e.getColumn() == 0) {
//            PluginInfo p = pluginChecker.getPluginList().get(e.getFirstRow());
//            Vector row = (Vector)pluginTableModel.getDataVector().get(e.getFirstRow());
//            boolean enabled = (boolean)row.get(0);
//            if(enabled) {
//              if(p.enable()) {
//                String message = String.format(PLUGIN_RESTART_EJS, p.name, (enabled ? "enabled" : "disabled"));
//                JOptionPane.showMessageDialog(null, message);
//              }
//            } else {
//              if(p.disable()) {
//                String message = String.format(PLUGIN_RESTART_EJS, p.name, (enabled ? "enabled" : "disabled"));
//                JOptionPane.showMessageDialog(null, message);
//              } else {
//                pluginTableModel.setValueAt(true, e.getFirstRow(), 0);
//                String message = String.format("Cannot disable plugin %s", p.name);
//                JOptionPane.showMessageDialog(null, message);
//              }
//            }
//          }
//      }
//    });
//  } 
//
//  private void updatePluginInfo() {
//    int selected = pluginTable.getSelectedRow(); 
//    if(selected != -1) {
//      pluginDetailModel.clear();
//      List<Map<String, Object>> info = pluginChecker.getPluginList().get(selected).info;
//      for(Map<String, Object> i : info) {
//        pluginDetailModel.addElement(i);
//      }
//    }
//  }
//  
//}
//
