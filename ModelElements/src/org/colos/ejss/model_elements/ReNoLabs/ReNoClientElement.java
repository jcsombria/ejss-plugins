/**
 * 
 */
package org.colos.ejss.model_elements.ReNoLabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.ModelElementEditor;
import org.colos.ejs.model_elements.ModelElementSearch;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.osejs.GenerateJS;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.html_view.ElementEditor;
import org.colos.ejs.osejs.edition.html_view.ElementsTree;
import org.colos.ejs.osejs.edition.html_view.OneView;
import org.colos.ejs.osejs.utils.FileUtils;
import org.colos.ejs.osejs.utils.ResourceUtil;
import org.colos.ejss.model_elements.input_output.WebSocketElement;
import org.colos.ejss.xml.JSObfuscator;
import org.colos.ejss.xml.XMLTransformerJava;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensourcephysics.tools.JarTool;

import io.socket.client.IO;
import io.socket.client.IO.Options;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * @author Iñigo Aizpuru Rueda
 *
 */
public class ReNoClientElement extends AbstractModelElement {
  static private ResourceUtil res    = new ResourceUtil ("Resources");
  
  static private ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejss/model_elements/ReNoLabs/ReNoLabs.png"); // This icon is included in this jar
  static private Border LABEL_BORDER = BorderFactory.createEmptyBorder(0,4,0,2);

  static private final String BEGIN_ADDRESS_HEADER = "<Address><![CDATA[";
  static private final String END_ADDRESS_HEADER = "]]></Address>"; 
  static private final String BEGIN_PORT_HEADER = "<Port><![CDATA[";
  static private final String END_PORT_HEADER = "]]></Port>"; 
  static private final String DEFAULT_CODE = "";
  static private final String BEGIN_PANEL_NAME = "<Panel><![CDATA[";
  static private final String END_PANEL_NAME = "]]></Panel>"; 
  
  static private final String UNSELECTED_PANEL_NAME = "UNDEFINED";
  
  protected JPanel mainPanel = null;
  
  protected String     sClientName = null;
  protected JTextField mAddressField = new JTextField();  // needs to be created to avoid null references
  protected JTextField mPortField = new JTextField();  // needs to be created to avoid null references
  protected ModelElementEditor mCodeEditor = new ModelElementEditor(this,null);  // The editor for the code
  {
    mCodeEditor.setName("ReNoClientElement");
    mCodeEditor.readPlainCode(DEFAULT_CODE);  
  }
  
  protected Osejs _ejs = null;
  protected DefaultComboBoxModel<String> panelComboBoxModel = new DefaultComboBoxModel<String>();
  protected String sSelectedPanelName = UNSELECTED_PANEL_NAME;
  
  private DefaultTableModel  simVariablesTableModel;
  private DefaultTableModel  labVariablesTableModel;
  private DefaultTableModel  linkedVariablesTableModel;
  private JTable simVariablesTable;
  private JTable labVariablesTable;
  private JTable linkedVariablesTable;

  private Socket socket;

  /**
   * 
   */
  public ReNoClientElement() {
    // Initialize class elements
    // Lab Variables Table
    labVariablesTableModel = new DefaultTableModel();
    Vector<String> labNamesVector = new Vector<String>();
    labNamesVector.add("I/O");
    labNamesVector.add("Name");
    labNamesVector.add("Type");
    Vector<Vector<String>> labVector = new Vector<Vector<String>>();      
    labVariablesTableModel.setDataVector(labVector, labNamesVector);

    // Simulation Variables Table
    simVariablesTableModel = new DefaultTableModel();
    Vector<String> simNamesVector = new Vector<String>();
    simNamesVector.add("Name");
    simNamesVector.add("Type");
    Vector<Vector<String>> simVector = new Vector<Vector<String>>();      
    simVariablesTableModel.setDataVector(simVector, simNamesVector);

    // Simulation Variables Table
    linkedVariablesTableModel = new DefaultTableModel();
    Vector<String> linkedNamesVector = new Vector<String>();
    linkedNamesVector.add("Simulation Variable");
    linkedNamesVector.add("Lag Signal");
    Vector<Vector<String>> linkedVector = new Vector<Vector<String>>();      
    linkedVariablesTableModel.setDataVector(linkedVector, linkedNamesVector);
  }

  /* (non-Javadoc)
   * @see org.colos.ejs.model_elements.AbstractModelElement#getImageIcon()
   */
  @Override
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }

  /* (non-Javadoc)
   * @see org.colos.ejs.model_elements.AbstractModelElement#getGenericName()
   */
  @Override
  public String getGenericName() { return "ReNoClient"; }

  /* (non-Javadoc)
   * @see org.colos.ejs.model_elements.AbstractModelElement#getConstructorName()
   */
  @Override
  public String getConstructorName() { return "ReNoClient"; }

  @Override
  public String getSourceCode(String name) {
    String address = mAddressField.getText().trim();
    String port = mPortField.getText().trim();
    return "var EJSS_RENOLABS = EJSS_RENOLABS || {};\n" +
           "EJSS_RENOLABS."+name+" = {};\n" +
           "EJSS_RENOLABS."+name+".Address = \""+address+"\";\n" + 
           "EJSS_RENOLABS."+name+".Port = \""+port+"\";\n" +
           "\n" +
           "_model.addToInitialization(function() {"+"\n" +
           "});"+"\n" +
           "\n" +
           "_model.addToReset(function() {"+"\n" +
           "  if (_view && _view."+name+")"+"\n" +
           "    delete _view."+name+";"+"\n" +
           "});"+"\n" +
           "\n" +
           "_model.addToEvolution(function() {"+"\n" +
           "});"+"\n" +
           "\n" +
           "_model.addToFixedRelations(function() {"+"\n" +
           "  if (_view && _view."+sSelectedPanelName+" && !_view."+name+") {"+"\n" +
           "    _view._addElement(EJSS_INTERFACE.html,\""+name+"\", _view."+sSelectedPanelName+")"+"\n" +
           "      .setProperty(\"Height\",\"100%\")"+"\n" +
           "      .setProperty(\"Width\",\"100%\")"+"\n" +
           "      .setProperty(\"Url\",\"http://"+address+":"+port+"\")"+"\n" +
           "      ;"+"\n" +
           "  }"+"\n" +
           "});"+"\n";
//           mCodeEditor.getCode()+"\n";
  } // Code that goes into the body of the model

  @Override
  public String getResourcesRequired() {
    return null;
  }

  @Override
  public String getImportStatements() {
    return "ReNoLabs/io.js";
  }

  @Override
  public String getDisplayInfo() {
    String connection = mAddressField.getText().trim() + ":" + mPortField.getText().trim();
    int l = connection.length();
    if (l>0) {
      if (l>28) connection = connection.substring(0,25)+"..."; 
      return "("+connection+")";
    }
    return null;
  }

  @Override
  public String savetoXML() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(BEGIN_ADDRESS_HEADER+mAddressField.getText()+END_ADDRESS_HEADER + "\n");
    buffer.append(BEGIN_PORT_HEADER+mPortField.getText()+END_PORT_HEADER + "\n");
    buffer.append(BEGIN_PANEL_NAME+sSelectedPanelName+END_PANEL_NAME + "\n");
    buffer.append(mCodeEditor.saveStringBuffer());
    return buffer.toString();
  }

  @Override
  public void readfromXML(String _inputXML) {
    mCodeEditor.readXmlString(_inputXML);
    String serviceStr = OsejsCommon.getPiece(_inputXML,BEGIN_ADDRESS_HEADER,END_ADDRESS_HEADER,false);
    if (serviceStr!=null) mAddressField.setText(serviceStr);
    String portStr = OsejsCommon.getPiece(_inputXML,BEGIN_PORT_HEADER,END_PORT_HEADER,false);
    if (portStr!=null) mPortField.setText(portStr);
    String panelStr = OsejsCommon.getPiece(_inputXML,BEGIN_PANEL_NAME,END_PANEL_NAME,false);
    if (panelStr!=null) sSelectedPanelName = panelStr;
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

  /* (non-Javadoc)
   * @see org.colos.ejs.model_elements.AbstractModelElement#getHtmlPage()
   */
  @Override
  protected String getHtmlPage() {
    return "org/colos/ejss/model_elements/ReNoLabs/ReNoClient.html";
  }

  @Override
  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    sClientName = name;
    
    JLabel serviceLabel = new JLabel("Server/IP:",SwingConstants.RIGHT);
    serviceLabel.setBorder(LABEL_BORDER);

    JLabel portLabel = new JLabel("Port:",SwingConstants.RIGHT);
    portLabel.setBorder(LABEL_BORDER);

    // Make both labels the same dimension
    int maxWidth  = serviceLabel.getPreferredSize().width;
    int maxHeight = serviceLabel.getPreferredSize().height;
    maxWidth  = Math.max(maxWidth,  portLabel.getPreferredSize().width);
    maxHeight = Math.max(maxHeight, portLabel.getPreferredSize().height);
    Dimension dim = new Dimension (maxWidth,maxHeight);
    serviceLabel.setPreferredSize(dim);
    portLabel.setPreferredSize(dim);

    mAddressField.getDocument().addDocumentListener (new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { collection.reportChange(ReNoClientElement.this); }
      public void insertUpdate(DocumentEvent e)  { collection.reportChange(ReNoClientElement.this); }
      public void removeUpdate(DocumentEvent e)  { collection.reportChange(ReNoClientElement.this); }
    });

    mPortField.getDocument().addDocumentListener (new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { collection.reportChange(ReNoClientElement.this); }
      public void insertUpdate(DocumentEvent e)  { collection.reportChange(ReNoClientElement.this); }
      public void removeUpdate(DocumentEvent e)  { collection.reportChange(ReNoClientElement.this); }
    });

    JPanel addressPanel = new JPanel(new BorderLayout());
    addressPanel.add(serviceLabel, BorderLayout.WEST);
    addressPanel.add(mAddressField, BorderLayout.CENTER);

    JPanel portPanel = new JPanel(new BorderLayout());
    portPanel.add(portLabel, BorderLayout.WEST);
    portPanel.add(mPortField, BorderLayout.CENTER);

    // ---------------------------------------------------------------------------------------------
    // Find available panels for the lab
    // ---------------------------------------------------------------------------------------------
    _ejs = collection.getEJS();
    JComboBox<String> panelComboBox = new JComboBox<String>(panelComboBoxModel);
    AbstractAction selectPanel = new AbstractAction("Select Panel"){
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
        try {
          JComboBox cb = (JComboBox)e.getSource();
          if (cb.getSelectedIndex() > 0)
            sSelectedPanelName = (String)cb.getSelectedItem();
          else
            sSelectedPanelName = UNSELECTED_PANEL_NAME;
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    };
    panelComboBox.addActionListener(selectPanel);
    
    // ------------------------------
    // Button panel
    // ------------------------------
    JButton getPanelsButton = new JButton("GetPanels");
    AbstractAction getPanels = new AbstractAction("Get Panels"){
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
        try {
          simVariablesTableModel.setRowCount(0);
//          String variable = collection.chooseVariable(mainPanel, "double|int", "");
//          System.out.println(variable);
//          String txt = collection.getEJS().getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_LIST_VARIABLES,"").toString();
          List<String> txtList = collection.getEJS().getModelEditor().getAllVariables();
          for (String s : txtList) {
            StringTokenizer tkn = new StringTokenizer (s,":");
            String varName = tkn.nextToken().trim();
            String varType = tkn.nextToken().trim();
            System.out.println(varName);
            Vector<String> row = new Vector<String>();
            row.add(varName);
            row.add(varType);
            simVariablesTableModel.addRow(row);
          }
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    };
    getPanelsButton.setAction(getPanels);

    // ------------------------------
    // Server connection panel
    // ------------------------------
    JPanel topPanel = new JPanel(new GridLayout(0,1));
    topPanel.setBorder(new TitledBorder(null, "Lab server connection", TitledBorder.LEADING, TitledBorder.TOP));
    topPanel.add(addressPanel);
    topPanel.add(portPanel);

    // ------------------------------
    // Link panel
    // ------------------------------
    simVariablesTable = new JTable(simVariablesTableModel) {
      private static final long serialVersionUID = 1L;
      public boolean isCellEditable(int row, int col) { return false; }
    };
    simVariablesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    simVariablesTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
      @Override
      public Component getTableCellRendererComponent(JTable table,
          Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        
        String dataType = (String)table.getModel().getValueAt(row, 1);
        if (isSelected) {
          setBackground(Color.RED);
          setForeground(Color.WHITE);
        }
        else if (dataType.equals("double")) {
          setBackground(Color.WHITE);
          setForeground(Color.BLACK);
        } else if (dataType.equals("int")) {
          setBackground(Color.WHITE);
          setForeground(Color.BLACK);
        }
        else {
          setBackground(Color.WHITE);
          setForeground(Color.LIGHT_GRAY);
        }
        return this;
      }   
    });
    JScrollPane simVariablesPane = new JScrollPane(simVariablesTable);
    simVariablesPane.setBorder(new TitledBorder(null, "Simulation Variables", TitledBorder.LEADING, TitledBorder.TOP));
    
    labVariablesTable = new JTable(labVariablesTableModel) {
      private static final long serialVersionUID = 1L;
      public boolean isCellEditable(int row, int col) { return false; }
    };
    labVariablesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane labVariablesPane = new JScrollPane(labVariablesTable);
    labVariablesPane.setBorder(new TitledBorder(null, "Laboratory Variables", TitledBorder.LEADING, TitledBorder.TOP));
    
    JPanel variablesPanel = new JPanel();
    variablesPanel.setLayout(new BoxLayout(variablesPanel, BoxLayout.X_AXIS));
    variablesPanel.add(simVariablesPane);
    variablesPanel.add(labVariablesPane);

    linkedVariablesTable = new JTable(linkedVariablesTableModel) {
      private static final long serialVersionUID = 1L;
      public boolean isCellEditable(int row, int col) { return false; }
    };
    linkedVariablesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane linkedVariablesPane = new JScrollPane(linkedVariablesTable);
    linkedVariablesPane.setBorder(new TitledBorder(null, "Linked Variables", TitledBorder.LEADING, TitledBorder.TOP));
    
    JPanel linkConfigPanel = new JPanel();
    linkConfigPanel.setLayout(new BoxLayout(linkConfigPanel, BoxLayout.Y_AXIS));
    linkConfigPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
//    linkConfigPanel.setPreferredSize(new Dimension(430, 600));
    linkConfigPanel.add(variablesPanel);
    linkConfigPanel.add(linkedVariablesPane);
    
    JButton getSignalsButton = new JButton("Get Signals");
    AbstractAction getSignals = new AbstractAction("Get Signals"){
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
        try {
          // Connect to server
          System.out.println("Connecting to http://"+mAddressField.getText()+":"+mPortField.getText()+"...");
          IO.Options opts = new IO.Options();
          opts.forceNew = true;
          opts.query = "mode=client";
          opts.reconnection = false;
          
          socket = IO.socket("http://"+mAddressField.getText()+":"+mPortField.getText(), opts);

          socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
              System.out.println("Connected");
            }

          });

          socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
              System.out.println("Disconnected");
            }

          });
          
          // Read signals when receiving the response from the server
          socket.on("SignalInfoToClient", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
              // Display signals in table
              for (Object o : args) {
System.out.println(o);
                try {
                  JSONObject jsonObj = (JSONObject) o;
                  labVariablesTableModel.setRowCount(0);
                  if (jsonObj != null) {
                    JSONArray input = jsonObj.getJSONArray("i");
                    JSONArray output = jsonObj.getJSONArray("o");
                    if (input != null) {
                      for (int i = 0 ; i < input.length(); i++) {
                        JSONObject jsonSignal = (JSONObject) input.get(i);
                        if (jsonSignal != null) {
                          Vector<String> row = new Vector<String>();
                          row.add("in");
                          row.add(jsonSignal.getString("name"));
                          row.add(jsonSignal.getString("type"));
                          labVariablesTableModel.addRow(row);
                        }
                      }
                    }
                    else
                    {
                      System.out.println("No input signals found!");
                    }
                    if (output != null) {
                      for (int i = 0 ; i < output.length(); i++) {
                        JSONObject jsonSignal = (JSONObject) output.get(i);
                        if (jsonSignal != null) {
                          Vector<String> row = new Vector<String>();
                          row.add("out");
                          row.add(jsonSignal.getString("name"));
                          row.add(jsonSignal.getString("type"));
                          labVariablesTableModel.addRow(row);
                        }
                      }
                    }
                    else
                    {
                      System.out.println("No output signals found!");
                    }
                  }
                  else
                  {
                    System.out.println("jsonObj not a JSONObject!");
                  }
                } catch (JSONException e) {
                  labVariablesTableModel.setRowCount(0);
                  e.printStackTrace();
                } catch (Exception e) {
                  labVariablesTableModel.setRowCount(0);
                  e.printStackTrace();
                }
              }
              socket.disconnect();
            }

          });

          socket = socket.connect();

          // Wait for connection
          {
            int t = 0;
            while (!socket.connected() && t <= 10) {
              Thread.sleep(100);
              t = t + 1;
            }
          }
          
          // Request signals
          if (socket.connected()) {
            try {
              JSONObject obj = new JSONObject();
              
              socket.emit("SignalRequest", obj);

            } catch (Exception ex) {
              ex.printStackTrace();
            }

            // Wait for disconnection
            {
              int t = 0;
              while (socket.connected() && t <= 10) {
                Thread.sleep(100);
                t = t + 1;
              }
              if (socket.connected()) {
                // Force disconnection
                socket.disconnect();
                System.out.println("ERROR: Timeout!");
              }
            }
          }
          else
            System.out.println("ERROR: Connection failed!");
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    };
    getSignalsButton.setAction(getSignals);
    
    JPanel linkActionPanel = new JPanel();
    linkActionPanel.setLayout(new BoxLayout(linkActionPanel,BoxLayout.Y_AXIS));
    linkActionPanel.setBorder(new EmptyBorder(5, 0, 5, 0));
    linkActionPanel.add(getSignalsButton);

    JPanel linkPanel = new JPanel(new BorderLayout());
    linkPanel.add(linkConfigPanel, BorderLayout.CENTER);
    linkPanel.add(linkActionPanel, BorderLayout.EAST);
    
    // ------------------------------
    // Lab configuration panel
    // ------------------------------
    JPanel cfgPanel = new JPanel(new BorderLayout());
    cfgPanel.setBorder(new TitledBorder(null, "Lab configuration", TitledBorder.LEADING, TitledBorder.TOP));
    cfgPanel.add(panelComboBox, BorderLayout.NORTH);
    cfgPanel.add(linkPanel, BorderLayout.CENTER);
//    cfgPanel.add(mCodeEditor.getComponent(collection), BorderLayout.SOUTH);

    // ------------------------------
    // Action panel
    // ------------------------------
    JPanel actionPanel = new JPanel(new BorderLayout());
    actionPanel.add(getPanelsButton, BorderLayout.EAST);
    
    // ------------------------------
    // Lab panel
    // ------------------------------
    JPanel labPanel = new JPanel(new GridLayout(0,1));
    labPanel.add(cfgPanel);

    // ------------------------------
    // Main panel
    // ------------------------------
    mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(topPanel,BorderLayout.NORTH);
//    mainPanel.add(mCodeEditor.getComponent(collection),BorderLayout.CENTER);
    mainPanel.add(labPanel,BorderLayout.CENTER);
    mainPanel.add(actionPanel,BorderLayout.SOUTH);
    mainPanel.setPreferredSize(new Dimension(800,600));
    mainPanel.addAncestorListener ( new AncestorListener ()
    {
      public void ancestorAdded ( AncestorEvent event )
      {
        rebuildPanelComboBox();
        refreshSimVariables(collection);
      }

      public void ancestorRemoved ( AncestorEvent event )
      {
      }

      public void ancestorMoved ( AncestorEvent event )
      {
      }
    });
    return mainPanel;
  }

  @Override
  public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, String name, ModelElementsCollection collection) {
    java.util.List<ModelElementSearch> list = new ArrayList<ModelElementSearch>();
    addToSearch(list,mAddressField,info,searchString,mode,this,name,collection);
    addToSearch(list,mPortField,info,searchString,mode,this,name,collection);
    list.addAll(mCodeEditor.search(info, searchString, mode, name, collection));
    return list;
  }

  private void rebuildPanelComboBox() {
    // Backup selected panel name as it gets lost when the combo box model y updated
    String sSelectedItem = sSelectedPanelName;
    
    // Clear the combo box model
    panelComboBoxModel.removeAllElements();
    
    // Find the current panel items
    List<ElementEditor> simElements = new ArrayList<ElementEditor>();
    for (Editor page : _ejs.getHtmlViewEditor().getPages()) {
      OneView oneViewPage = ((OneView) page);
      ElementsTree treeElements = oneViewPage.getTree();
      DefaultMutableTreeNode rootNode = treeElements.findNode(res.getString("Tree.Main"));
      simElements.add(treeElements.viewOf(rootNode));
      Enumeration cursor = rootNode.preorderEnumeration();
      while (cursor.hasMoreElements()) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) cursor.nextElement();
        if (treeElements.viewOf(node).getClassname().equals("Elements.Panel"))
          simElements.add(treeElements.viewOf(node));
      }
    }
    
    // Populate the combo box model
    int i = 0;
    for (ElementEditor e : simElements) {
      String sElementName = e.getName();
      if (i == 0)
        sElementName = "<Choose a panel>";

      panelComboBoxModel.addElement(sElementName);

      // Restore the selected panel if still present
      if (sElementName.equals(sSelectedItem))
        panelComboBoxModel.setSelectedItem(sElementName);
      i++;
    }
  }

  private void refreshSimVariables(final ModelElementsCollection collection) {
    simVariablesTableModel.setRowCount(0);
    // Populate simulation variables
    List<String> txtList = collection.getEJS().getModelEditor().getAllVariables();
    for (String s : txtList) {
      StringTokenizer tkn = new StringTokenizer (s,":");
      String varName = tkn.nextToken().trim();
      String varType = tkn.nextToken().trim();
      Vector<String> row = new Vector<String>();
      row.add(varName);
      row.add(varType);
      simVariablesTableModel.addRow(row);
    }
  }
}
