/**
 * 
 */
package org.colos.ejss.model_elements.ReNoLabs;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.edition.html_view.OneView;
import org.colos.ejs.osejs.plugins.PluginButtonInfo;
import org.colos.ejs.osejs.plugins.PluginRightClickOptionInfo;
import org.colos.ejs.osejs.utils.FileUtils;
import org.colos.ejss.xml.JSObfuscator;
import org.colos.ejss.xml.SimulationXML;
import org.colos.ejss.xml.XMLTransformerJava;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensourcephysics.tools.JarTool;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * @author Iñigo Aizpuru Rueda
 *
 */
public class ReNoLabDeploymentButton {

  static private ImageIcon BUTTON_ICON = AbstractModelElement.createImageIcon("org/colos/ejss/model_elements/ReNoLabs/ReNoLabs.png");
  static private final int CONNECTION_TIMEOUT_SECONDS = 10;
  
  static private Border LABEL_BORDER = BorderFactory.createEmptyBorder(0,4,0,2);

  private org.colos.ejs.osejs.Osejs mEjs;
  private Socket socket;
  
  private JComponent mainPanel;
  protected JTextField mAddressField = new JTextField();  // needs to be created to avoid null references
  protected JTextField mPortField = new JTextField();  // needs to be created to avoid null references
  protected JTextField mUserField = new JTextField();  // needs to be created to avoid null references
  protected JPasswordField mPassField = new JPasswordField();  // needs to be created to avoid null references

  private PluginButtonInfo pbi;
  
  private boolean chunk_received = false;
  private boolean disconnected = false;
  
  /**
   * 
   */
  public ReNoLabDeploymentButton(org.colos.ejs.osejs.Osejs _ejs) {
    
    mEjs = _ejs;

    JLabel serviceLabel = new JLabel("Server/IP:",SwingConstants.RIGHT);
    serviceLabel.setBorder(LABEL_BORDER);
    JLabel portLabel = new JLabel("Port:",SwingConstants.RIGHT);
    portLabel.setBorder(LABEL_BORDER);

    JLabel userLabel = new JLabel("User:",SwingConstants.RIGHT);
    userLabel.setBorder(LABEL_BORDER);
    JLabel passLabel = new JLabel("Password:",SwingConstants.RIGHT);
    passLabel.setBorder(LABEL_BORDER);

    // Make all labels the same dimension
    int maxWidth  = serviceLabel.getPreferredSize().width;
    int maxHeight = serviceLabel.getPreferredSize().height;
    maxWidth  = Math.max(maxWidth,  portLabel.getPreferredSize().width);
    maxHeight = Math.max(maxHeight, portLabel.getPreferredSize().height);
    maxWidth  = Math.max(maxWidth,  userLabel.getPreferredSize().width);
    maxHeight = Math.max(maxHeight, userLabel.getPreferredSize().height);
    maxWidth  = Math.max(maxWidth,  passLabel.getPreferredSize().width);
    maxHeight = Math.max(maxHeight, passLabel.getPreferredSize().height);
    Dimension dim = new Dimension (maxWidth,maxHeight);
    serviceLabel.setPreferredSize(dim);
    portLabel.setPreferredSize(dim);
    userLabel.setPreferredSize(dim);
    passLabel.setPreferredSize(dim);
    
    JPanel addressPanel = new JPanel(new BorderLayout());
    addressPanel.add(serviceLabel, BorderLayout.WEST);
    addressPanel.add(mAddressField, BorderLayout.CENTER);

    JPanel portPanel = new JPanel(new BorderLayout());
    portPanel.add(portLabel, BorderLayout.WEST);
    portPanel.add(mPortField, BorderLayout.CENTER);

    JPanel userPanel = new JPanel(new BorderLayout());
    userPanel.add(userLabel, BorderLayout.WEST);
    userPanel.add(mUserField, BorderLayout.CENTER);

    JPanel passPanel = new JPanel(new BorderLayout());
    passPanel.add(passLabel, BorderLayout.WEST);
    passPanel.add(mPassField, BorderLayout.CENTER);

    // ------------------------------
    // Server configuration panel
    // ------------------------------
    JPanel serverPanel = new JPanel(new GridLayout(0,1));
    serverPanel.setBorder(new TitledBorder(null, "Lab server", TitledBorder.LEADING, TitledBorder.TOP));
    serverPanel.add(addressPanel);
    serverPanel.add(portPanel);

    // ------------------------------
    // Login configuration panel
    // ------------------------------
    JPanel loginPanel = new JPanel(new GridLayout(0,1));
    loginPanel.setBorder(new TitledBorder(null, "Login", TitledBorder.LEADING, TitledBorder.TOP));
    loginPanel.add(userPanel);
    loginPanel.add(passPanel);

    // ------------------------------
    // Server configuration panel
    // ------------------------------
    mainPanel = new JPanel(new GridLayout(0,1));
    mainPanel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP));
    mainPanel.add(serverPanel);
    mainPanel.add(loginPanel);
    maxWidth  = mainPanel.getPreferredSize().width;
    maxHeight = mainPanel.getPreferredSize().height;
    maxWidth  = Math.max(maxWidth,  400);
    mainPanel.setPreferredSize(new Dimension (maxWidth,maxHeight));

    // -------------------------------------------------------------------------------
    // Button to connect with the laboratory server
    // -------------------------------------------------------------------------------

    Runnable leftClickAction = new Runnable() {
      
      @Override
      public void run() {
        int option = JOptionPane.showOptionDialog(null, mainPanel, "Set Deployment Parameters", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
        
        if (option == JOptionPane.YES_OPTION) {
          disconnected = false;
          
          String name = "";
          String content = "";
          try {
            mEjs.getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            Osejs _ejs = mEjs;

            File javascriptDir = new File(_ejs.getBinDirectory(),"javascript/lib");
            String libPath = FileUtils.getPath(javascriptDir);

            // Create a working temporary directory
            File zipFolder;
            try {
              zipFolder = File.createTempFile("LabCode", ".tmp", _ejs.getExportDirectory()); // Get a unique name for our temporary directory
              zipFolder.delete();        // remove the created file
              zipFolder.mkdirs();
            } catch (Exception exc) { 
              exc.printStackTrace();
              JOptionPane.showMessageDialog(_ejs.getMainPanel(),"Error generating laboratory code. File not created.",
                  "Error creating temp folder", JOptionPane.INFORMATION_MESSAGE);
              return;
            }
            
            String filename = _ejs.getSimInfoEditor().getSimulationName();
            if (filename==null) filename = _ejs.getCurrentXMLFilename();
            SimulationXML simulation = _ejs.getSimulationXML(filename);

            String simFilename = simulation.getName()+ (JSObfuscator.isGenerateXHTML() ? "_Simulation.xhtml" : "_Simulation.html");
            File htmlFile = new File (zipFolder,simFilename);          

            OneView oneViewPage = ((OneView) _ejs.getHtmlViewEditor().getCurrentPage());
            String viewDesired = null;
            if (oneViewPage!=null)
              viewDesired = oneViewPage.getName();

            boolean ok = XMLTransformerJava.saveHTMLFile(_ejs,libPath,htmlFile, // output info
                simulation, viewDesired, null, "_ejs_library/css/ejss.css","_ejs_library", null, true, true); // separate JS and use full library
            if (!ok) {
              JOptionPane.showMessageDialog(_ejs.getMainPanel(),"Error generating laboratory code. File not created.",
                  "Error saving files", JOptionPane.INFORMATION_MESSAGE);
              return;
            }

            File [] filesInFolder = zipFolder.listFiles();
            for (int i = 0; i < filesInFolder.length; i++) {
              if (filesInFolder[i].getName().endsWith(".js")) {
                name = filesInFolder[i].getName();
                content = FileUtils.readTextFile(filesInFolder[i],null);
                System.out.println(filesInFolder[i].getName());
  /*              System.out.println(content);*/
              }
            }
  /*          GenerateJS.prepackageXMLSimulation(_ejs, null, filename, zipFolder, true, null);
            File [] filesInFolder = zipFolder.listFiles();
            for (int i = 0; i < filesInFolder.length; i++) {
              if (filesInFolder[i].getName().startsWith(filename + "_Simulation")) {
                name = filesInFolder[i].getName();
                content = FileUtils.readTextFile(filesInFolder[i],null);
                System.out.println(content);
              }
            }*/
            JarTool.remove(zipFolder);
          }
          catch (Exception ex) {
            ex.printStackTrace();
          }
          try {
            mEjs.getOutputArea().println("Connecting to http://"+mAddressField.getText()+":"+mPortField.getText()+"...");
            mEjs.getOutputArea().textArea().paintAll(mEjs.getOutputArea().textArea().getGraphics());
            IO.Options opts = new IO.Options();
            opts.forceNew = true;
            opts.query = "mode=maintenance&user=" + mUserField.getText() + "&password=" + new String(mPassField.getPassword());
            opts.reconnection = false;

            socket = IO.socket("http://"+mAddressField.getText()+":"+mPortField.getText(), opts);

            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

              @Override
              public void call(Object... args) {
                mEjs.getOutputArea().println("Connected");
                mEjs.getOutputArea().textArea().paintAll(mEjs.getOutputArea().textArea().getGraphics());
              }

            });
            
            socket.on("chunkCompleted", new Emitter.Listener() {

              @Override
              public void call(Object... args) {
                chunk_received = true;
                mEjs.getOutputArea().println("Chunk complete...");
                mEjs.getOutputArea().textArea().paintAll(mEjs.getOutputArea().textArea().getGraphics());
              }

            });

            socket.on("codeCompleted", new Emitter.Listener() {

              @Override
              public void call(Object... args) {
                socket.disconnect();
                mEjs.getOutputArea().println("Upload completed");
                mEjs.getOutputArea().textArea().paintAll(mEjs.getOutputArea().textArea().getGraphics());
              }

            });

            socket.on("upload_rejected", new Emitter.Listener() {

              @Override
              public void call(Object... args) {
                if (args != null) {
                  for (Object obj : args) {
                    try {
                      JSONObject json = (JSONObject) obj;
                      String s = json.getString("text");
                      mEjs.getOutputArea().println("Upload rejected: " + s);
                      mEjs.getOutputArea().textArea().paintAll(mEjs.getOutputArea().textArea().getGraphics());
                      socket.disconnect();
                    } catch (Exception ex) {
                      ex.printStackTrace();
                    }
                  }
                }
              }

            });

            socket.on("login_error", new Emitter.Listener() {

              @Override
              public void call(Object... args) {
                if (args != null) {
                  for (Object obj : args) {
                    try {
                      JSONObject json = (JSONObject) obj;
                      String s = json.getString("text");
                      mEjs.getOutputArea().println("Login ERROR: " + s);
                      mEjs.getOutputArea().textArea().paintAll(mEjs.getOutputArea().textArea().getGraphics());
                    } catch (Exception ex) {
                      ex.printStackTrace();
                    }
                  }
                }
              }

            });

            socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

              @Override
              public void call(Object... args) {
                disconnected = true;
                mEjs.getOutputArea().println("Disconnected");
                mEjs.getOutputArea().textArea().paintAll(mEjs.getOutputArea().textArea().getGraphics());
              }

            });
            
            socket = socket.connect();
            
            // Wait for connection
            {
              int t = 0;
              while (!disconnected && !socket.connected() && t <= (10 * CONNECTION_TIMEOUT_SECONDS)) {
                Thread.sleep(100);
                t = t + 1;
              }
            }
            
            if (socket.connected()) {
              int index = 0;
              while (index < content.length()) {
                try {
                  JSONObject obj = new JSONObject();
                  obj.put("name", name);
                  obj.put("code", content.substring(index, Math.min(index+10000, content.length())));
                  
                  chunk_received = false;
                  socket.emit("upload_chunk", obj);
                  {
                    int t = 0;
                    while (!chunk_received && t <= (10 * CONNECTION_TIMEOUT_SECONDS)) {
                      Thread.sleep(100);
                      t = t + 1;
                    }
                    if (!chunk_received) {
                      // Force disconnection
                      socket.disconnect();
                      mEjs.getOutputArea().println("ERROR: Chunk lost!");
                      mEjs.getOutputArea().textArea().paintAll(mEjs.getOutputArea().textArea().getGraphics());
                      return;
                    }
                  }

                  index += 10000;
                } catch (JSONException ex) {
                  ex.printStackTrace();
                }
              }

              socket.emit("finish_upload", new JSONObject());
              
              // Wait for disconnection
              {
                int t = 0;
                while (socket.connected() && t <= (10 * CONNECTION_TIMEOUT_SECONDS)) {
                  Thread.sleep(100);
                  t = t + 1;
                }
                if (socket.connected()) {
                  // Force disconnection
                  socket.disconnect();
                  mEjs.getOutputArea().println("ERROR: Timeout!");
                  mEjs.getOutputArea().textArea().paintAll(mEjs.getOutputArea().textArea().getGraphics());
                }
              }
            }
            else {
              mEjs.getOutputArea().println("ERROR: Connection failed!");
              mEjs.getOutputArea().textArea().paintAll(mEjs.getOutputArea().textArea().getGraphics());
            }
          }
          catch (Exception ex) {
            ex.printStackTrace();
          }
          finally {
            mEjs.getMainFrame().setCursor(Cursor.getDefaultCursor());
          }
        }
      }
    };
    
    Vector<PluginRightClickOptionInfo> rightClickActions = new Vector<>();
    
    pbi = new PluginButtonInfo(BUTTON_ICON, "Deploy Lab", leftClickAction, rightClickActions);
  }

  public PluginButtonInfo getButtonInfo()
  {
    return pbi;
  }
}
