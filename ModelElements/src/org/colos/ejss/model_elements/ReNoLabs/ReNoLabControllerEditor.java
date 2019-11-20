/**
 * 
 */
package org.colos.ejss.model_elements.ReNoLabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.colos.ejs.osejs.edition.CodeEditor;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.SearchResult;
import org.colos.ejss.xml.SimulationXML;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * @author arale
 *
 */
public class ReNoLabControllerEditor implements Editor {

  static private final int CONNECTION_TIMEOUT_SECONDS = 10;

  static private Border LABEL_BORDER = BorderFactory.createEmptyBorder(0,4,0,2);

  private org.colos.ejs.osejs.Osejs mEjs;

  protected JPanel mainPanel = null;
  protected ReNoLabModelEditor modelEditor = null;
  protected JTextField mAddressField = new JTextField();  // needs to be created to avoid null references
  protected JTextField mPortField = new JTextField();  // needs to be created to avoid null references
  protected ProgrammableTabbedPane mCodeEditor = null;  // The editor for the code

  private JComponent loginPanel = null;
  protected JTextField mUserField = new JTextField();  // needs to be created to avoid null references
  protected JPasswordField mPassField = new JPasswordField();  // needs to be created to avoid null references
  ButtonGroup versionGroup = new ButtonGroup();
  protected JRadioButton rbtnGeneral = new JRadioButton("Main",true);
  protected JRadioButton rbtnPersonal = new JRadioButton("Private",false);

  private Socket socket;

  private String name="";
  private boolean internal  = true;
  private boolean active  = true;
  private boolean changed = false;

  private boolean disconnected = false;

  /**
   * 
   */
  public ReNoLabControllerEditor(org.colos.ejs.osejs.Osejs _ejs) {
    mEjs = _ejs;
    
    // Initialize plugin elements
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

    JLabel userLabel = new JLabel("User:",SwingConstants.RIGHT);
    userLabel.setBorder(LABEL_BORDER);
    JLabel passLabel = new JLabel("Password:",SwingConstants.RIGHT);
    passLabel.setBorder(LABEL_BORDER);

    // Make both labels the same dimension
    maxWidth  = passLabel.getPreferredSize().width;
    maxHeight = passLabel.getPreferredSize().height;
    maxWidth  = Math.max(maxWidth,  userLabel.getPreferredSize().width);
    maxHeight = Math.max(maxHeight, userLabel.getPreferredSize().height);
    dim = new Dimension (maxWidth,maxHeight);
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
    // Controller version
    // ------------------------------
    versionGroup.add(rbtnGeneral);
    versionGroup.add(rbtnPersonal);

    // ------------------------------
    // Login configuration panel
    // ------------------------------
    loginPanel = new JPanel(new GridLayout(0,1));
    loginPanel.setBorder(new TitledBorder(null, "Login", TitledBorder.LEADING, TitledBorder.TOP));
    loginPanel.add(userPanel);
    loginPanel.add(passPanel);

    // ------------------------------
    // Version panel
    // ------------------------------
    JPanel versionPanel = new JPanel(new GridLayout(0,1));
    versionPanel.setBorder(new TitledBorder(null, "Version", TitledBorder.LEADING, TitledBorder.TOP));
    versionPanel.add(rbtnGeneral);
    versionPanel.add(rbtnPersonal);

    // ------------------------------
    // Controller panel
    // ------------------------------
    JPanel controllerPanel = new JPanel(new BorderLayout());
    controllerPanel.add(loginPanel, BorderLayout.CENTER);
    controllerPanel.add(versionPanel, BorderLayout.EAST);

    // ------------------------------
    // Server configuration panel
    // ------------------------------
    loginPanel = new JPanel(new GridLayout(0,1));
    loginPanel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP));
    loginPanel.add(controllerPanel);
    maxWidth  = loginPanel.getPreferredSize().width;
    maxHeight = loginPanel.getPreferredSize().height;
    maxWidth  = Math.max(maxWidth,  400);
    loginPanel.setPreferredSize(new Dimension (maxWidth,maxHeight));

    mCodeEditor = new ProgrammableTabbedPane(_ejs, "FileTab", true);  // The editor for the code
    mCodeEditor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

    // -------------------------------------------------------------------------------
    // Button to send the controller code to the server
    // -------------------------------------------------------------------------------
    JButton sendCodeButton = new JButton("Send Code");
    AbstractAction sendCode = new AbstractAction("Send Code"){
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {

        try {
          int option = JOptionPane.showOptionDialog(null, loginPanel, "Set User Parameters", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);

          if (option == JOptionPane.YES_OPTION) {
            disconnected = false;
            
            mEjs.getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
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

            socket.on("controller_upload_complete", new Emitter.Listener() {

              @Override
              public void call(Object... args) {
                mEjs.getOutputArea().println("Controller upload complete");
                mEjs.getOutputArea().textArea().paintAll(mEjs.getOutputArea().textArea().getGraphics());
              }

            });

            socket.on("controller_upload_rejected", new Emitter.Listener() {

              @Override
              public void call(Object... args) {
                if (args != null) {
                  for (Object obj : args) {
                    try {
                      JSONObject json = (JSONObject) obj;
                      String s = json.getString("text");
                      mEjs.getOutputArea().println("Controller upload rejected: " + s);
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

            socket.on("compilation_error", new Emitter.Listener() {

              @Override
              public void call(Object... args) {
                if (args != null) {
                  for (Object obj : args) {
                    try {
                      JSONObject json = (JSONObject) obj;
                      String s = UnEscapeStrig(json.getString("error"));
                      mEjs.getOutputArea().println(s);
                      mEjs.getOutputArea().textArea().paintAll(mEjs.getOutputArea().textArea().getGraphics());
                    } catch (Exception ex) {
                      ex.printStackTrace();
                    }
                  }
                }
              }

            });
            
            socket.on("compilation_result", new Emitter.Listener() {

              @Override
              public void call(Object... args) {
                socket.disconnect();
                mEjs.getOutputArea().println("Compilation result:");
                mEjs.getOutputArea().textArea().paintAll(mEjs.getOutputArea().textArea().getGraphics());
                if (args != null) {
                  for (Object obj : args) {
                    JSONObject json = (JSONObject) obj;
                    int result;
                    try {
                      result = json.getInt("code");
                      if (result == 0) {
                        mEjs.getOutputArea().println("Compilation completed successfully");
                        mEjs.getOutputArea().textArea().paintAll(mEjs.getOutputArea().textArea().getGraphics());
                      }
                      else {
                        mEjs.getOutputArea().println("Compilation returned an error: " + result);
                        mEjs.getOutputArea().textArea().paintAll(mEjs.getOutputArea().textArea().getGraphics());
                      }
                    } catch (JSONException e) {
                      mEjs.getOutputArea().println("Compilation returned an error: " + json);
                      mEjs.getOutputArea().textArea().paintAll(mEjs.getOutputArea().textArea().getGraphics());
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
              try {
                JSONObject json = new JSONObject();
                json.put("name", mUserField.getText());
                json.put("languaje", "C");
                if (rbtnPersonal.isSelected())
                  json.put("version", "private");
                else
                  json.put("version", "main");
                JSONArray filesArray = new JSONArray();
                for (CodeEditor editor : mCodeEditor.getCodePages()) {
                  JSONObject fileInfo = new JSONObject();
                  fileInfo.put("fileName", editor.getName());
                  fileInfo.put("code", editor.getTextComponent().getText());
                  filesArray.put(fileInfo);
                }
                json.put("files", filesArray);

                socket.emit("upload_controller", json);
              } catch (JSONException ex) {
                ex.printStackTrace();
              }

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
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
        finally {
          mEjs.getMainFrame().setCursor(Cursor.getDefaultCursor());
        }
      }

    };
    sendCodeButton.setAction(sendCode);
    
    // -------------------------------------------------------------------------------
    // Button to get the controller files from the server
    // -------------------------------------------------------------------------------
    JButton getCodeButton = new JButton("Get Code");
    AbstractAction getCode = new AbstractAction("Get Code"){
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
        try {
          int option = JOptionPane.showOptionDialog(null, loginPanel, "Set User Parameters", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);

          if (option == JOptionPane.YES_OPTION) {
            disconnected = false;
            
            mEjs.getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
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

            socket.on("controller_code", new Emitter.Listener() {

              @Override
              public void call(Object... args) {
                if (args != null && args.length > 0) {
                  try {
                    JSONArray filesArray = (JSONArray) args[0];
                    for (int i = 0; i < filesArray.length(); i++) {
                      JSONObject json = filesArray.getJSONObject(i);
                      String fileName = json.getString("fileName");
                      String code = json.getString("code");
                      mCodeEditor.AddPage(fileName, code, "");
                    }

                    //                  JSONObject json = (JSONObject) args[0];
                    //                  String fileName = json.getString("fileName");
                    //                  String code = json.getString("code");
                    //                  mCodeEditor.AddPage(fileName, code, "");
                  } catch (JSONException e) {
                    e.printStackTrace();
                  } catch (Exception ex) {
                    ex.printStackTrace();
                  }
                }
                socket.disconnect();
                mEjs.getOutputArea().println("Download complete");
                mEjs.getOutputArea().textArea().paintAll(mEjs.getOutputArea().textArea().getGraphics());
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
              try {
                JSONObject obj = new JSONObject();
                obj.put("name", mUserField.getText());
                obj.put("languaje", "C");
                if (rbtnPersonal.isSelected())
                  obj.put("version", "private");
                else
                  obj.put("version", "main");

                socket.emit("download_controller", obj);

              } catch (JSONException ex) {
                ex.printStackTrace();
              }

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
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
        finally {
          mEjs.getMainFrame().setCursor(Cursor.getDefaultCursor());
        }
      }
        
    };
    getCodeButton.setAction(getCode);

    // ------------------------------
    // Server configuration panel
    // ------------------------------
    JPanel topPanel = new JPanel(new GridLayout(0,1));
    topPanel.setBorder(new TitledBorder(null, "Lab server", TitledBorder.LEADING, TitledBorder.TOP));
    topPanel.add(addressPanel);
    topPanel.add(portPanel);

    // ------------------------------
    // Action panel
    // ------------------------------
    JPanel actionPanel = new JPanel();
    actionPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.LINE_AXIS));
    actionPanel.add(getCodeButton);
    actionPanel.add(sendCodeButton);
    actionPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    
    // ------------------------------
    // Main panel
    // ------------------------------
    mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    mainPanel.add(topPanel,BorderLayout.NORTH);
    mainPanel.add(mCodeEditor.getComponent(),BorderLayout.CENTER);
    mainPanel.add(actionPanel,BorderLayout.SOUTH);
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

  }

  /* (non-Javadoc)
   * @see org.colos.ejs.osejs.edition.Editor#generateCode(int, java.lang.String)
   */
  @Override
  public StringBuffer generateCode(int _type, String _info) {
    return null;
  }

  /* (non-Javadoc)
   * @see org.colos.ejs.osejs.edition.Editor#saveStringBuffer()
   */
  @Override
  public StringBuffer saveStringBuffer() {
    return null;
  }

  /* (non-Javadoc)
   * @see org.colos.ejs.osejs.edition.Editor#readString(java.lang.String)
   */
  @Override
  public void readString(String _input) {

  }

  /* (non-Javadoc)
   * @see org.colos.ejs.osejs.edition.Editor#search(java.lang.String, java.lang.String, int)
   */
  @Override
  public List<SearchResult> search(String _info, String _searchString,
      int _mode) {
    return null;
  }

  private String UnEscapeStrig(String str) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < str.length(); i++){
      char c = str.charAt(i);

      //Process char
      if (c == '\\') {
        // Escaped character
        if (str.length() > i + 1) {
          i = i + 1;
          if (str.charAt(i) == 'u') {
            // Unicode character, we need to read the 4 characters that define the character
            if (str.length() >= i + 4) {
              String hexCode = str.substring(i + 1, i + 5);
              i = i + 4;
              boolean isHex = hexCode.matches("[0-9A-Fa-f]+");
              if (isHex) {
                int value = Integer.parseInt(hexCode, 16);
                sb.append((char)value);
              }
              else {
                sb.append("\\u").append(hexCode);
              }
            }
            else {
              // String is too short
              sb.append('\\').append(str.substring(i, str.length() - 1));
              i = str.length() - 1;
            }
          }
          else if (str.charAt(i) == 't') {
            sb.append('\t');
          }
          else if (str.charAt(i) == 'b') {
            sb.append('\b');
          }
          else if (str.charAt(i) == 'n') {
            sb.append('\n');
          }
          else if (str.charAt(i) == 'r') {
            sb.append('\r');
          }
          else if (str.charAt(i) == 'f') {
            sb.append('\f');
          }
          else if (str.charAt(i) == '\'') {
            sb.append('\'');
          }
          else if (str.charAt(i) == '"') {
            sb.append('\"');
          }
          else if (str.charAt(i) == '\\') {
            sb.append('\\');
          }
          else
          {
            sb.append(c).append(str.charAt(i));
          }
        }
        else {
          sb.append(c);
        }
      }
      else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

}
