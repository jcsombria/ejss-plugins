package org.colos.ejss.model_elements.ReNoLabs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.colos.ejs.model_elements.ModelElement;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.osejs.GenerateJS;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.html_view.ElementsTree;
import org.colos.ejs.osejs.edition.html_view.OneView;
import org.colos.ejs.osejs.utils.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * @author Iñigo Aizpuru Rueda
 *
 */
public class ReNoLabElement {

  static private Border LABEL_BORDER = BorderFactory.createEmptyBorder(0,4,0,2);

  static private final String BEGIN_ADDRESS_HEADER = "<Address><![CDATA[";
  static private final String END_ADDRESS_HEADER = "]]></Address>"; 
  static private final String BEGIN_PORT_HEADER = "<Port><![CDATA[";
  static private final String END_PORT_HEADER = "]]></Port>"; 

  protected JTextField mNameField = new JTextField();    // needs to be created to avoid null references
  protected JTextField mAddressField = new JTextField(); // needs to be created to avoid null references
  protected JTextField mPortField = new JTextField();    // needs to be created to avoid null references
  
  protected Component component = null;
  
  protected EventListenerList listenerList = new EventListenerList();

  /**
   * 
   */
  public ReNoLabElement(String name) {
    // Initialize class elements
    if (name == null)
      mNameField.setText("");
    else
      mNameField.setText(name.trim());
  }
  
  public String getName() { return mNameField.getText().trim(); }
  
  public String getAddress() { return mAddressField.getText().trim(); }
  
  public String getPort() { return mPortField.getText().trim(); }
  
  public String getDisplayInfo() {
    return getName() + " (" + getAddress() + ":" + getPort() + ")";
  }
  
  public Component getEditor() {
    if (component == null)
      component = createEditor();
    
    return component;
  }

  /**
   * Adds a listener to the list that's notified each time a change
   * to the data model occurs.
   *
   * @param l the <code>ChangeListener</code> to be added
   */
  public void addListDataListener(ChangeListener l) {
      listenerList.add(ChangeListener.class, l);
  }


  /**
   * Removes a listener from the list that's notified each time a
   * change to the data model occurs.
   *
   * @param l the <code>ChangeListener</code> to be removed
   */
  public void removeListDataListener(ChangeListener l) {
      listenerList.remove(ChangeListener.class, l);
  }

  public String savetoXML() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(BEGIN_ADDRESS_HEADER+mAddressField.getText()+END_ADDRESS_HEADER + "\n");
    buffer.append(BEGIN_PORT_HEADER+mPortField.getText()+END_PORT_HEADER + "\n");
    return buffer.toString();
  }

  public void readfromXML(String _inputXML) {
    String serviceStr = OsejsCommon.getPiece(_inputXML,BEGIN_ADDRESS_HEADER,END_ADDRESS_HEADER,false);
    if (serviceStr!=null) mAddressField.setText(serviceStr);
    String portStr = OsejsCommon.getPiece(_inputXML,BEGIN_PORT_HEADER,END_PORT_HEADER,false);
    if (portStr!=null) mPortField.setText(portStr);
  }

  public String getSourceCode() {
    String name = mNameField.getText().trim();
    String address = mAddressField.getText().trim();
    String port = mPortField.getText().trim();
    return "var " + name + " = {\n" +
           "  address : \""+address+"\",\n" + 
           "  port    : \""+port+"\",\n" +
           "  key_ejs : getLabKey(),\n" +
           "  user    : undefined,\n" +
           "  password: undefined,\n" +
           "  configNo: 0,\n" +
           "  config  : undefined,\n" +
           "  state_EJS : {\n" +
           "    inputs  : {},\n" +
           "    outputs : {}\n" +
           "  },\n" +
           "  state_REAL : {\n" +
           "    config     : 0,                  //[0: disconnected, 1: ready, 2: play, 3: pause, 4: reset]\n" +
           "    evolution  : [],                 //[]\n" +
           "    inputs  : {},\n" +
           "    outputs : {}\n" +
           "  },\n" +
           "  socket : undefined,\n" +
           "  getQueryString : function() {\n" +
           "    if (this.key_ejs)\n" +
           "      return 'key=' + this.key_ejs;\n" +
           "    return 'user='+(this.user || '')+'&'+'password='+(this.password || '');\n" +
           "  },\n" +
           "  connect : function() {\n" +
           "    if (!this.key_ejs && !this.user) return;\n" +
           "    if (!this.socket) {\n" +
           "      this.socket = io.connect('http://' + this.address + ':' + this.port, { query: this.getQueryString() });\n" +
           "      if (this.socket) {\n" +
           "        this.socket.on('serverOut_clientIn', function(data) {\n" +
           "          " + name + ".state_REAL[data.variable] = data.value;\n" +
           "          " + name + ".state_REAL.inputs[data.variable] = data.value;\n" +
           "          _update()\n" +
           "        });\n" +
           "        this.socket.on('disconnect_timeout', function(data) {\n" +
           "          alert(data.text);\n" +
           "        });\n" +
           "        this.socket.on('connect', function() {\n" +
           "          " + name + ".socket.emit('clientOut_request', {request: 'config'});\n" +
           "          _update()\n" +
           "        });\n" +
           "        this.socket.on('disconnect', function() {\n" +
           "          " + name + ".config = undefined;\n" +
           "          _update();\n" +
           "          if (" + name + ".key_ejs)\n" +
           "            window.location = \"./select\"\n" +
           "        });\n" +
           "        this.socket.on('serverOut_response', function(data) {\n" +
           "          if (data.request == 'config') {\n" +
           "            " + name + ".config = data.response;\n" +
           "            " + name + ".configNo++;\n" +
           "          }\n" +
           "          _update()\n" +
           "        })\n" +
           "      }\n" +
           "    }\n" +
           "    else {\n" +
           "      if (this.socket.disconnected) {\n" +
           "        this.socket.query = this.socket.io.opts.query = this.getQueryString();\n" +
           "        this.socket.connect();\n" +
           "      }\n" +
           "    }\n" +
           "  },\n" +
           "  disconnect : function(variable) {\n" +
           "    if (this.socket.connected) {\n" +
           "      this.socket.disconnect();\n" +
           "    }\n" +
           "  },\n" +
           "  send_connect : function(variable, controller) {\n" +
           "    var temp = [0];\n" +
           "    temp[0] = variable;\n" +
           "    if (variable == 1) {\n" +
           "      this.socket.emit('clientOut_serverIn', {variable: 'config', value: temp, version: controller})\n" +
           "    }\n" +
           "    else {\n" +
           "      this.socket.emit('clientOut_serverIn', {variable: 'config', value: temp})\n" +
           "    }\n" +
           "  },\n" +
           "  update : function() {\n" +
           "    for (key in this.state_EJS) {\n" +
           "      if (this.state_REAL[key] != this.state_EJS[key]) {\n" +
           "        this.socket.emit('clientOut_serverIn', {variable: key, value: this.state_EJS[key]})\n" +
           "      }\n" +
           "    }\n" +
           "    for (key in this.state_EJS.inputs) {\n" +
           "      if (this.state_REAL.inputs[key] != this.state_EJS.inputs[key]) {\n" +
           "        this.socket.emit('clientOut_serverIn', {variable: key, value: this.state_EJS.inputs[key]})\n" +
           "      }\n" +
           "    }\n" +
           "  }\n" +
           "};\n" +
           name + ".connect();\n\n";
    
  } // Code that goes into the body of the model

  protected Component createEditor() {
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
      public void changedUpdate(DocumentEvent e) { fireStateChanged(); }
      public void insertUpdate(DocumentEvent e)  { fireStateChanged(); }
      public void removeUpdate(DocumentEvent e)  { fireStateChanged(); }
    });

    mPortField.getDocument().addDocumentListener (new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { fireStateChanged(); }
      public void insertUpdate(DocumentEvent e)  { fireStateChanged(); }
      public void removeUpdate(DocumentEvent e)  { fireStateChanged(); }
    });

    JPanel addressPanel = new JPanel(new BorderLayout());
    addressPanel.add(serviceLabel, BorderLayout.WEST);
    addressPanel.add(mAddressField, BorderLayout.CENTER);

    JPanel portPanel = new JPanel(new BorderLayout());
    portPanel.add(portLabel, BorderLayout.WEST);
    portPanel.add(mPortField, BorderLayout.CENTER);

    // ------------------------------
    // Server configuration panel
    // ------------------------------
    JPanel topPanel = new JPanel(new GridLayout(0,1));
    topPanel.setBorder(new TitledBorder(null, "Lab server configuration", TitledBorder.LEADING, TitledBorder.TOP));
    topPanel.add(addressPanel);
    topPanel.add(portPanel);

    // ------------------------------
    // Main panel
    // ------------------------------
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(topPanel,BorderLayout.NORTH);
    return mainPanel;
  }
  
  protected void fireStateChanged() {
    for (ChangeListener l : listenerList.getListeners(ChangeListener.class)) {
      l.stateChanged(new ChangeEvent(ReNoLabElement.this));
    }
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getDisplayInfo();
  }
}
