/**
 * 
 */
package org.colos.ejss.model_elements.plugins.installer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.osejs.plugins.PluginButtonInfo;
import org.colos.ejs.osejs.plugins.PluginRightClickOptionInfo;

/**
 * @author Jesús Chacón Sombría
 *
 */
public class PluginManagerButton {

  class PluginTableModel extends DefaultTableModel {
    private final String[] COLUMNS = {"Enabled", "Jar File"};

    public void setDataVector(Object[][] dataVector) {
      setDataVector(dataVector, COLUMNS);
    }
  
    public Class<?> getColumnClass(int c) {
      return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int column) {
      return (column == 0);
    }
  }

  private static final String PLUGIN_RESTART_EJS = "Plugin %s %s. Please restart EjsS.";
  private static final String PLUGIN_SUCCESFULLY_INSTALLED = "Plugin %s succesfully installed.";
  private static final String PLUGIN_NOT_INSTALLED = "Plugin %s is already installed.";
  private static final String PLUGIN_SUCCESFULLY_UNINSTALLED = "Plugin %s succesfully uninstalled.";
  private static final String PLUGIN_NOT_UNINSTALLED = "Cannot uninstall plugin %s.";
  private static final String BUTTON_ICON_NAME = "org/colos/ejss/model_elements/plugins/installer/resources/PluginInstaller.png";
  static private ImageIcon BUTTON_ICON = AbstractModelElement.createImageIcon(BUTTON_ICON_NAME);
  private org.colos.ejs.osejs.Osejs mEjs;
  private JComponent mainPanel;
  private PluginButtonInfo pbi;
  private JPanel detailPanel;
  private JList pluginDetail;
  private PluginTableModel pluginTableModel = new PluginTableModel();
  private DefaultListModel<Map<String, Object>> pluginDetailModel = new DefaultListModel<>();
  private JTable pluginTable;
  private PluginInstaller pluginManager;
  
  /**
   * 
   */
   PluginManagerButton(org.colos.ejs.osejs.Osejs _ejs) {
    mEjs = _ejs;

    createEditor();
    // -------------------------------------------------------------------------------
    // Button to open plugins configuration dialog
    // -------------------------------------------------------------------------------
    Runnable leftClickAction = new Runnable() {
      @Override
      public void run() {
        JOptionPane.showOptionDialog(null, mainPanel, "Configure Plugins", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
      }
    };
    pbi = new PluginButtonInfo(BUTTON_ICON, "Manage Plugins", leftClickAction, null);
  }

  public PluginButtonInfo getButtonInfo() {
    return pbi;
  }

  private void createEditor() {
    // ------------------------------
    // Detail panel
    // ------------------------------
    detailPanel = new JPanel(new BorderLayout());

    detailPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Plugin Info", TitledBorder.LEADING, TitledBorder.BELOW_TOP));
    pluginDetail = new JList(pluginDetailModel);
    pluginDetail.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    pluginDetail.setLayoutOrientation(JList.VERTICAL_WRAP);
    pluginDetail.setVisibleRowCount(-1);
    pluginDetail.setCellRenderer(new PluginInfoRenderer());
    detailPanel.add(pluginDetail);
    // ------------------------------
    // Connections panel
    // ------------------------------

    // Plugin List
    pluginTableModel.setDataVector(new Object[][] {{new Boolean(true), ""}});
    pluginTable = new JTable(pluginTableModel);
    pluginTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    pluginTable.addMouseListener(new MouseAdapter() {
      public void mousePressed (MouseEvent _evt) {
          updatePluginInfo();
      }
    });
    JScrollPane listScroller = new JScrollPane(pluginTable);
    listScroller.setPreferredSize(new Dimension(250, 80));

    pluginTableModel.addTableModelListener(new TableModelListener() {
      @Override
      public void tableChanged(TableModelEvent e) {
          if(e.getColumn() == 0) {
            PluginInfo p = pluginManager.getPluginList().get(e.getFirstRow());
            Vector row = (Vector)pluginTableModel.getDataVector().get(e.getFirstRow());
            boolean enabled = (boolean)row.get(0);
            if(enabled) {
              if(p.enable()) {
                String message = String.format(PLUGIN_RESTART_EJS, p.name, (enabled ? "enabled" : "disabled"));
                JOptionPane.showMessageDialog(null, message);
              }
            } else {
              if(p.disable()) {
                String message = String.format(PLUGIN_RESTART_EJS, p.name, (enabled ? "enabled" : "disabled"));
                JOptionPane.showMessageDialog(null, message);
              } else {
                String message = String.format("Cannot disable plugin %s", p.name);
                JOptionPane.showMessageDialog(null, message);
              }
            }
          }
      }
    });

    // Add Lab
    AbstractAction installPlugin = new AbstractAction("+"){
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Plugin File", "jar");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
          File f = chooser.getSelectedFile();
          if(pluginManager.install(f)) {
            String message = String.format(PLUGIN_SUCCESFULLY_INSTALLED, f.getName());
            JOptionPane.showMessageDialog(null, message);
          } else {
            String message = String.format(PLUGIN_NOT_INSTALLED, f.getName());
            JOptionPane.showMessageDialog(null, message);
          }
        }
        updatePluginList();
      }
    };
    JButton installButton = new JButton(installPlugin);

    AbstractAction uninstallPlugin = new AbstractAction("-") {
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
        int i = pluginTable.getSelectedRow();
        if(i < 0 || i >= pluginTableModel.getRowCount()) {
          String message = "Please, select the plugin you want to uninstall.";
          JOptionPane.showMessageDialog(null, message, "", JOptionPane.ERROR_MESSAGE);
        } else {
          String plugin = (String)pluginTableModel.getValueAt(i, 1);
          System.out.println(plugin);
          if(pluginManager.uninstall(plugin)) {
            String message = String.format(PLUGIN_SUCCESFULLY_UNINSTALLED, plugin);
            JOptionPane.showMessageDialog(null, message);
          } else {
            String message = String.format(PLUGIN_NOT_UNINSTALLED, plugin);
            JOptionPane.showMessageDialog(null, message);
          }
        }
        updatePluginList();
      }
    };
    JButton uninstallButton = new JButton(uninstallPlugin);

    JPanel buttonPanel = new JPanel(new GridLayout(0,1));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder());
    buttonPanel.add(installButton);
    buttonPanel.add(uninstallButton);

    JPanel actionPanel = new JPanel(new BorderLayout());
    actionPanel.setBorder(BorderFactory.createEmptyBorder());
    actionPanel.add(buttonPanel,BorderLayout.NORTH);

    JPanel connectionPanel = new JPanel(new BorderLayout());
    connectionPanel.add(listScroller,BorderLayout.CENTER);
    connectionPanel.add(actionPanel,BorderLayout.EAST);
 
    // ------------------------------
    // Server configuration main panel
    // ------------------------------
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, connectionPanel, detailPanel);
    splitPane.setDividerLocation(300);
    mainPanel = new JPanel(new GridLayout(1,1));
    mainPanel.setBorder(new TitledBorder(null, "Lab Connections", TitledBorder.LEADING, TitledBorder.TOP));
    mainPanel.add(splitPane);
    mainPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    mainPanel.setPreferredSize(new Dimension(800, 600));
    
    // TO DO: Encapsulate better
    String path = mEjs.getConfigDirectory() + "/CustomPlugins";
    pluginManager = new PluginInstaller(new File(path));
    updatePluginList();
  }

  private void updatePluginList() {
    pluginTableModel.getDataVector().clear();
    for(PluginInfo p : pluginManager.getPluginList()) {
      Object[] data = {p.enabled, p.name};
      pluginTableModel.addRow(data);
    }
    pluginTable.getColumnModel().getColumn(0).setPreferredWidth(200);
    pluginTable.getColumnModel().getColumn(1).setPreferredWidth(800);
  } 

  private void updatePluginInfo() {
    int selected = pluginTable.getSelectedRow(); 
    if(selected != -1) {
      pluginDetailModel.clear();
      List<Map<String, Object>> info = pluginManager.getPluginList().get(selected).info;
      for(Map<String, Object> i : info) {
        pluginDetailModel.addElement(i);
      }
    }
  }

}

class PluginInfoRenderer extends JPanel implements ListCellRenderer<Map<String, Object>> {

  @Override
  public Component getListCellRendererComponent(
      JList<? extends Map<String, Object>> list,
          Map<String, Object> info, int index, boolean isSelected,
          boolean cellHasFocus) {

    String name = (String)info.get("name");
    String desc = (String)info.get("description");
    ImageIcon icon = (ImageIcon)info.get("icon");

    setLayout(new BorderLayout());

    JLabel jname = new JLabel(name, icon, JLabel.LEFT);
    JTextArea jdesc = new JTextArea(desc, 5, 40);
    jdesc.setLineWrap(true);
    jdesc.setWrapStyleWord(true);
    this.removeAll();
    this.add(jname, BorderLayout.NORTH);
    this.add(jdesc, BorderLayout.CENTER);
    this.add(new JSeparator(), BorderLayout.SOUTH);
    this.setBackground(Color.WHITE);
    return this;
  }
}