package es.uned.dia.ejss.softwarelinks.model_elements;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.*;
import java.security.*;
import java.security.KeyStore;
import java.security.cert.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.net.ssl.*;
import javax.net.ssl.SSLContext;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SarlabElement extends AbstractModelElement {
  private static final String ICON_PATH = "es/uned/dia/ejss/softwarelinks/resources/EnlargeElement.jpg";
  private static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon(ICON_PATH);
  
  protected static final Object CONNECTION_OK = "Server OK";
  protected static final Object SERVER_KO = "Server is not working";
  private SarlabConfigurationModel config = new SarlabConfigurationModel();

  private DefaultTableModel htmlProxyTableModel = new DefaultTableModel(new Object[] {"Description", "URL", "Path", "Type", "Element"}, 1);
  private DefaultTableModel websocketProxyTableModel = new DefaultTableModel(new Object[] {"Description", "URL", "Path", "Type", "Element"}, 1);
  private JCheckBox securityCheckbox = new JCheckBox("Secure connection (Needs the app to be saved!)");
  private JTextField serverText = new JTextField("localhost", 14);
  private JTextField portText = new JTextField("80", 5);
  private JTextField pathText = new JTextField("SARLABV8.0", 15);
  private JTextField expIdText = new JTextField();
  private JTextField usernameText = new JTextField("", 15);
  private JTextField passwordText = new JPasswordField("", 15);
  private JTable htmlProxyTable;
  private JTable websocketProxyTable;

  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "ENLARGE"; }
  
  public String getConstructorName() { return "enlarge"; }
  
  public String getInitializationCode(String _name) { // Code for the LINT in JS
    return "";
  } 

  public String getSourceCode(String name) { // Code that goes into the body of the model
    String onConnect = "";
    Iterator iter = htmlProxyTableModel.getDataVector().iterator();
    while (iter.hasNext()) {
      Vector row = (Vector)iter.next();
      String id = (String)row.get(0);
      String path = (String)row.get(2);
      String type = (String)row.get(3);
      String element = (String)row.get(4);
      if (!element.isEmpty()) {
        switch (type) {
          case "RIP Server":
            onConnect += String.format("%s.transport.setHost(_model._sarlab.getHTTPUrlById('%s', '%s'));", element, id, path);
            //onConnect += String.format("%s.proxy = _model._sarlab.getHTTPProxy('%s', '%s');", element, id, path);
            break;
          case "Camera":
            onConnect += String.format("_model._userUnserialize({'%s':_model._sarlab.getCamUrlById('%s', '%s')});", element, id, path);
            break;
          }
      }
    }
    Iterator j = websocketProxyTableModel.getDataVector().iterator();
    while (j.hasNext()) {
      Vector row = (Vector)j.next();
      String id = (String)row.get(0);
      String path = (String)row.get(2);
      String type = (String)row.get(3);
      String element = (String)row.get(4);
      if(!element.isEmpty()) {
        switch(type) {
          case "RIP Server":
            onConnect += String.format("%s.transport.setHost(_model._sarlab.getWebsocketsUrlById('%s'));", element, id);
            break;
          case "Camera":
            onConnect += String.format("_model._userUnserialize({'%s':_model._sarlab.getCamUrlById('%s', '%s')});", element, id, path);
            break;
          default:
            onConnect += String.format("%s.transport.setHost(_model._sarlab.getWebsocketsUrlById('%s'));", element, id);
            break;
          }
      }
    }

    String credentialTemplate = "_model._sarlab.setSarlabCredentials({'username':'%s','password':'%s'});",
           username = config.getUsername(),
           password = config.getPassword();
    String credentials = username.isEmpty() ? "" : String.format(credentialTemplate, username, password);
    return String.format("var %s = new SarlabProxy('%s', '%s', '%s', '%s', '%s'); _model._sarlab = %s; " + credentials
        + "_model._sarlab.connect = function(callback) { " 
        + "this.connectExperience(this.experience, function() { %s if(callback != undefined) callback(); }.bind(_model)); }",
        name, config.getSecurity(), config.getHost(), config.getPath(), config.getPort(), config.getExperience(), name, onConnect);
  } 
  
  public String getImportStatements() { // Required for Lint
    return "SoftwareLinks/SarlabProxy.js"; 
  }

  public String getTooltip() {
	return "";
  }

  @Override
  protected String getHtmlPage() {
  	return "es/uned/dia/ejss/softwarelinks/resources/enlarge.html";
  }

  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    JPanel mainPanel = new JPanel();
    mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    mainPanel.setPreferredSize(new Dimension(600, 400));
    JPanel serverPanel = createServerPanel(mainPanel);
    JScrollPane websocketPanel = createWebsocketPanel();
    JScrollPane proxyPanel = createProxyPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.add("Server", serverPanel);
    mainPanel.add("WebSocket Connections", websocketPanel);
    mainPanel.add("HTTP Proxy", proxyPanel);

//    htmlProxyTable.addMouseListener (new MouseAdapter() {        
//      public void mousePressed (MouseEvent _evt) {
//        if (htmlProxyTable.isEnabled ()) {
//          int row = htmlProxyTable.rowAtPoint(_evt.getPoint()); 
//          int col = htmlProxyTable.columnAtPoint(_evt.getPoint()); 
//          if(row != -1 && col == 4) {
//            htmlProxyTable.setRowSelectionInterval(row, row);
//            String value = "";
//            String variable = collection.chooseVariable(htmlProxyTable, "", value);
////          String variable = collection.chooseViewElement(htmlProxyTable, Group.class, value);
//            htmlProxyTable.setValueAt(variable, row, col);
//          }
//        }
//      }
//    });    

//
//    JButton passwordLinkButton = new JButton(LINK_ICON);
//    passwordLinkButton.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        String value = mPasswordField.getText().trim();
//        if (!ModelElementsUtilities.isLinkedToVariable(value))
//          value = "";
//        else
//          value = ModelElementsUtilities.getPureValue(value);
//        String variable = collection.chooseVariable(mPasswordField, "String",
//            value);
//        if (variable != null)
//          mPasswordField.setText("%" + variable + "%");
//      }
//    });
//

    return mainPanel;
  }

  private JPanel createServerPanel(final Component parent) {
    JPanel serverPanel = new JPanel();
    serverPanel.setBorder(new TitledBorder(null, "ENLARGE configuration", TitledBorder.LEADING, TitledBorder.TOP));
    serverPanel.setMinimumSize(new Dimension(620, 180));
    serverPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
    serverPanel.setPreferredSize(new Dimension(620, 130));
    SpringLayout sl_topPanel = new SpringLayout();
    serverPanel.setLayout(sl_topPanel);

    JLabel serverLabel = new JLabel("IP:");
    sl_topPanel.putConstraint(SpringLayout.NORTH, serverLabel, 7, SpringLayout.NORTH, serverPanel);
    sl_topPanel.putConstraint(SpringLayout.WEST, serverLabel, 5, SpringLayout.WEST, serverPanel);
    serverPanel.add(serverLabel);

    sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, serverText, 0, SpringLayout.VERTICAL_CENTER, serverLabel);
    sl_topPanel.putConstraint(SpringLayout.WEST, serverText, 6, SpringLayout.EAST, serverLabel);
    serverText.setColumns(14);
    serverPanel.add(serverText);

    JLabel portLabel = new JLabel("Port:");
    sl_topPanel.putConstraint(SpringLayout.NORTH, portLabel, 0, SpringLayout.NORTH, serverLabel);
    sl_topPanel.putConstraint(SpringLayout.WEST, portLabel, 10, SpringLayout.EAST, serverText);
    serverPanel.add(portLabel);

    sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, portText, 0, SpringLayout.VERTICAL_CENTER, serverLabel);
    sl_topPanel.putConstraint(SpringLayout.WEST, portText, 6, SpringLayout.EAST, portLabel);
    portText.setColumns(5);
    serverPanel.add(portText);

    JLabel pathLabel = new JLabel("Path:");
    sl_topPanel.putConstraint(SpringLayout.NORTH, pathLabel, 0, SpringLayout.NORTH, portLabel);
    sl_topPanel.putConstraint(SpringLayout.WEST, pathLabel, 10, SpringLayout.EAST, portText);
    serverPanel.add(pathLabel);

    sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, pathText, 0, SpringLayout.VERTICAL_CENTER, portLabel);
    sl_topPanel.putConstraint(SpringLayout.WEST, pathText, 6, SpringLayout.EAST, pathLabel);
    sl_topPanel.putConstraint(SpringLayout.EAST, pathText, -6, SpringLayout.EAST, serverPanel);
    serverPanel.add(pathText);

    JLabel expIdLabel = new JLabel("Experience Id:");
    sl_topPanel.putConstraint(SpringLayout.NORTH, expIdLabel, 6, SpringLayout.SOUTH, serverText);
    sl_topPanel.putConstraint(SpringLayout.WEST, expIdLabel, 5, SpringLayout.WEST, serverPanel);
    serverPanel.add(expIdLabel);

    sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, expIdText, 0, SpringLayout.VERTICAL_CENTER, expIdLabel);
    sl_topPanel.putConstraint(SpringLayout.WEST, expIdText, 6, SpringLayout.EAST, expIdLabel);
    sl_topPanel.putConstraint(SpringLayout.EAST, expIdText, -6, SpringLayout.EAST, serverPanel);
    expIdText.setColumns(30);
    serverPanel.add(expIdText);

    sl_topPanel.putConstraint(SpringLayout.NORTH, securityCheckbox, 4, SpringLayout.SOUTH, expIdText);
    sl_topPanel.putConstraint(SpringLayout.WEST, securityCheckbox, 5, SpringLayout.WEST, serverPanel);
    serverPanel.add(securityCheckbox);

    JLabel usernameLabel = new JLabel("ENLARGE user:");
    sl_topPanel.putConstraint(SpringLayout.NORTH, usernameLabel, 25, SpringLayout.NORTH, securityCheckbox);
    sl_topPanel.putConstraint(SpringLayout.WEST, usernameLabel, 5, SpringLayout.WEST, serverPanel);
    serverPanel.add(usernameLabel);

    sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, usernameText, 0, SpringLayout.VERTICAL_CENTER, usernameLabel);
    sl_topPanel.putConstraint(SpringLayout.WEST, usernameText, 6, SpringLayout.EAST, usernameLabel);
    serverPanel.add(usernameText);

    JLabel passwordLabel = new JLabel("ENLARGE password:");
    sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, passwordLabel, 0, SpringLayout.VERTICAL_CENTER, usernameLabel);
    sl_topPanel.putConstraint(SpringLayout.WEST, passwordLabel, 5, SpringLayout.EAST, usernameText);
    serverPanel.add(passwordLabel);

    sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, passwordText, 0, SpringLayout.VERTICAL_CENTER, passwordLabel);
    sl_topPanel.putConstraint(SpringLayout.WEST, passwordText, 6, SpringLayout.EAST, passwordLabel);
    sl_topPanel.putConstraint(SpringLayout.EAST, passwordText, -5, SpringLayout.EAST, serverPanel);
    serverPanel.add(passwordText);

    JButton testButton = new JButton("Get Server Info");
    sl_topPanel.putConstraint(SpringLayout.NORTH, testButton, 10, SpringLayout.SOUTH, usernameLabel);
    //sl_topPanel.putConstraint(SpringLayout.SOUTH, testButton, -6, SpringLayout.SOUTH, serverPanel);
    sl_topPanel.putConstraint(SpringLayout.HORIZONTAL_CENTER, testButton, 0, SpringLayout.HORIZONTAL_CENTER, serverPanel);
    testButton.setMinimumSize(new Dimension(0, 50));

    AbstractAction testServer = new AbstractAction("Get Server Info"){
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent e) {
            boolean serverResponds = getServerInfo();
            if(serverResponds) {
                JOptionPane.showMessageDialog(parent, CONNECTION_OK);
            } else {
                JOptionPane.showMessageDialog(parent, SERVER_KO);
            }
        }
    };
    testButton.setAction(testServer);

    serverPanel.add(testButton);
    return serverPanel;
  }

  private boolean getServerInfo() {
    String response;
    try {
      config.setServer(securityCheckbox.isSelected(), serverText.getText(), portText.getText(), pathText.getText());
      config.setExperience(expIdText.getText());
      String urlformat;
      if (securityCheckbox.isSelected()) {
          urlformat = "https://%s:%s/%s/webresources/service?idExp=%s";
      } else {
          urlformat = "http://%s:%s/%s/webresources/service?idExp=%s";
      }
      String url = String.format(urlformat, config.getHost(), config.getPort(), config.getPath(), URLEncoder.encode(config.getExperience(), "UTF-8"));
      response = SarlabElement.httpget(url);
      config.load(response);
      List<Map<String,String>> htmlproxies = config.getHtmlProxies();
      Iterator<Map<String, String>> iter = htmlproxies.iterator();
      htmlProxyTableModel.getDataVector().removeAllElements();
      while (iter.hasNext()) {
        Map<String, String> proxy = iter.next();
        htmlProxyTableModel.addRow(new Object[] {proxy.get("description"), proxy.get("url"), "", "Camera", ""});
      }      
      List<Map<String, String>> wsproxies = config.getWebsocketProxies();
      Iterator<Map<String, String>> j = wsproxies.iterator();
      websocketProxyTableModel.getDataVector().removeAllElements();
      while (j.hasNext()) {
        Map<String, String> proxy = j.next();
        websocketProxyTableModel.addRow(new Object[] {proxy.get("description"), proxy.get("url"), "", "Camera", ""});
      }
      
      
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }
  
	private JScrollPane createWebsocketPanel() {
    websocketProxyTable = new JTable(websocketProxyTableModel);
    websocketProxyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane websocketScrollPane = new JScrollPane(websocketProxyTable);
    websocketScrollPane.setBorder(BorderFactory.createTitledBorder("Websocket Proxies"));
    return websocketScrollPane;
  }

  private JScrollPane createProxyPanel() {
    htmlProxyTable = new JTable(htmlProxyTableModel);
    htmlProxyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    TableColumn column = htmlProxyTable.getColumnModel().getColumn(3);
    JComboBox comboBox = new JComboBox();
    comboBox.addItem("RIP Server");
    comboBox.addItem("Camera");
    column.setCellEditor(new DefaultCellEditor(comboBox));
    JScrollPane htmlProxyScrollPane = new JScrollPane(htmlProxyTable);
    htmlProxyScrollPane.setBorder(BorderFactory.createTitledBorder("HTTP Proxies"));
    return htmlProxyScrollPane;
  }

  public String savetoXML() {
    Boolean security = securityCheckbox.isSelected();
    String server = serverText.getText().trim();
    String port = portText.getText().trim();
    String spath = pathText.getText().trim();
    String username = usernameText.getText().trim();
    String password = passwordText.getText().trim();
    config.setServer(security, server, port, spath);
    config.setExperience(expIdText.getText());
    config.setCredentials(username, password);
    config.addLinks(htmlProxyTableModel.getDataVector());
    config.addWebsocketsLinks(websocketProxyTableModel.getDataVector());
    return config.dump();
  }
  
  public void readfromXML(String inputXML) {
    config.restore(inputXML);
    securityCheckbox.setSelected(config.getSecurity());
    serverText.setText(config.getHost());
    portText.setText(config.getPort());
    pathText.setText(config.getPath());
    expIdText.setText(config.getExperience());
    usernameText.setText(config.getUsername());
    passwordText.setText(config.getPassword());
    htmlProxyTableModel.getDataVector().removeAllElements();
    for(Map<String, String> link : config.getHtmlLinks()) {
      String id = link.get("id"),
          ip =link.get("ip"),
          path = link.get("path"),
          type = link.get("type"),
          element = link.get("element");
      htmlProxyTableModel.addRow(new Object[] {id, ip, path, type, element});
    }
    websocketProxyTableModel.getDataVector().removeAllElements();
    for(Map<String, String> link : config.getWebsocketsLinks()) {
      String id = link.get("id"),
          ip =link.get("ip"),
          path = link.get("path"),
          type = link.get("type"),
          element = link.get("element");
      websocketProxyTableModel.addRow(new Object[] {id, ip, path, type, element});
    }
  }

  public static String httpget(String request) throws Exception {
    Object response = null;
    CloseableHttpClient httpclient = HttpClients.createDefault();
    try {
      response = getHttpResponse(request, httpclient);
    } catch (SSLHandshakeException e) {
      int index = request.indexOf(":", request.indexOf(":") + 1);
      String host = request.substring(8, index);
      int index2 = request.indexOf("/", request.indexOf("//") + 2);
      String port = request.substring(index + 1, index2);
      String passw = "changeit";
      char[] passphrase = passw.toCharArray();
      File file = keyStoreFile();
      InputStream in = new FileInputStream(file);
      KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
      ks.load(in, passphrase);
      in.close();
      InstallCert.main(host, Integer.parseInt(port), passphrase, ks);
      SSLContext sslcontext = SSLContexts.custom()
              .loadTrustMaterial(ks, new TrustSelfSignedStrategy())
              .build();
      SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
              sslcontext,
              new String[] { "TLSv1.2" },
              null,
              SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
      httpclient = HttpClients.custom()
              .setSSLSocketFactory(sslsf)
              .build();
      response = getHttpResponse(request, httpclient);
    } catch (NoHttpResponseException e) {
    } finally {
      httpclient.close();
    }
    return (String)response;
  }

  private static Object getHttpResponse(String request, CloseableHttpClient httpclient)
          throws IOException {
    Object response;
    HttpGet httpget = new HttpGet(request);
    ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
      public String handleResponse(final HttpResponse response) throws IOException {
        int status = response.getStatusLine().getStatusCode();
        if (status >= 200 && status < 300) {
          HttpEntity entity = response.getEntity();
          return entity != null ? EntityUtils.toString(entity) : null;
        } else {
          throw new ClientProtocolException("Unexpected response status: " + status);
        }
      }
    };
    String responseBody = httpclient.execute(httpget, responseHandler);
    response = responseBody;
    return response;
  }

  private static File keyStoreFile() {
    File file = new File("jssecacerts");
    if (!file.isFile()) {
      char SEP = File.separatorChar;
      File dir = new File(System.getProperty("java.home") + SEP
              + "lib" + SEP + "security");
      file = new File(dir, "jssecacerts");
      if (!file.isFile()) {
        file = new File(dir, "cacerts");
      }
    }
    System.out.println("Loading KeyStore " + file + "...");
    return file;
  }

  public SarlabConfigurationModel getConfig() {
    return config;
  }
}


class InstallCert {

  public static void main(String host, Integer port, char[] passphrase, KeyStore ks) throws Exception {
    SSLContext context = SSLContext.getInstance("TLS");
    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(ks);
    X509TrustManager defaultTrustManager = (X509TrustManager)tmf.getTrustManagers()[0];
    SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
    context.init(null, new TrustManager[] {tm}, null);
    SSLSocketFactory factory = context.getSocketFactory();

    System.out.println("Opening connection to " + host + ":" + port + "...");
    SSLSocket socket = (SSLSocket)factory.createSocket(host, port);
    socket.setSoTimeout(10000);
    try {
      System.out.println("Starting SSL handshake...");
      socket.startHandshake();
      socket.close();
      System.out.println();
      System.out.println("No errors, certificate is already trusted");
    } catch (SSLException e) {
      System.out.println();
      e.printStackTrace(System.out);

      X509Certificate[] chain = tm.chain;
      if (chain == null) {
        System.out.println("Could not obtain server certificate chain");
        return;
      }

      System.out.println();
      System.out.println("Server sent " + chain.length + " certificate(s):");
      System.out.println();
      MessageDigest sha1 = MessageDigest.getInstance("SHA1");
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      for (int i = 0; i < chain.length; i++) {
        X509Certificate cert = chain[i];
        System.out.println
                (" " + (i + 1) + " Subject " + cert.getSubjectDN());
        System.out.println("   Issuer  " + cert.getIssuerDN());
        sha1.update(cert.getEncoded());
        System.out.println("   sha1    " + toHexString(sha1.digest()));
        md5.update(cert.getEncoded());
        System.out.println("   md5     " + toHexString(md5.digest()));
        System.out.println();
      }

      X509Certificate cert = chain[0];
      String alias = host;
      ks.setCertificateEntry(alias, cert);

      OutputStream out = new FileOutputStream("jssecacerts");
      ks.store(out, passphrase);
      out.close();

      System.out.println();
      System.out.println(cert);
      System.out.println();
      System.out.println("Added certificate to keystore 'jssecacerts' using alias '" + alias + "'");
    }
  }

  private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

  private static String toHexString(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 3);
    for (int b : bytes) {
      b &= 0xff;
      sb.append(HEXDIGITS[b >> 4]);
      sb.append(HEXDIGITS[b & 15]);
      sb.append(' ');
    }
    return sb.toString();
  }

  private static class SavingTrustManager implements X509TrustManager {

    private final X509TrustManager tm;
    private X509Certificate[] chain;

    SavingTrustManager(X509TrustManager tm) {
      this.tm = tm;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
      // throw new UnsupportedOperationException();
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] chain,
                                   final String authType)
            throws CertificateException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] chain,
                                   final String authType)
            throws CertificateException {
      this.chain = chain;
      this.tm.checkServerTrusted(chain, authType);
    }
  }

}


class SarlabConfigurationModel {
  public static final String ID = "id";
  public static final String IP = "ip";
  public static final String PATH = "path";
  public static final String TYPE = "type";
  public static final String ELEMENT = "element";
  private static final String XNL_SARLAB = "sarlab";
  private static final String XNL_SECURITY = "secure";
  private static final String XNL_SERVER = "server";
  private static final String XNL_PORT = "port";
  private static final String XNL_SPATH = "path";
  private static final String XNL_USERNAME = "username";
  private static final String XNL_PASSWORD = "password";
  private static final String XNL_EXPERIENCE = "experience";
  private static final String XNL_WEBSOCKETS_LINKS = "wslinks";
  private static final String XNL_LINKS = "links";
  private static final String XNL_LABEL_LINK = "link";
  private static final String XNL_ID = "id";
  private static final String XNL_IP = "ip";
  private static final String XNL_PATH = "path";
  private static final String XNL_TYPE = "type";
  private static final String XNL_ELEMENT = "element";
  private Boolean security = false;
  private String host = "localhost";
  private String port = "80";
  private String spath = "SARLABV8.0";
  private String experience = "exp";
  private String username = "";
  private String password = "";
  
  private ArrayList<Map<String, String>> websocketProxies = new ArrayList<>();
  private ArrayList<Map<String, String>> websocketLinks = new ArrayList<>(); 
  private ArrayList<Map<String, String>> htmlProxies = new ArrayList<>();
  private ArrayList<Map<String, String>> htmlLinks = new ArrayList<>(); 
  
  public void setServer(Boolean secure, String host, String port, String spath) {
    this.security = secure;
    this.host = host;
    if (host.length() <= 0) {
      this.host = "localhost";
    }
    this.port = port;
    if(port.length() <= 0) {
      this.port = "80";
    }
    this.spath = spath;
    if(spath.length() <= 0) {
      this.spath = "SARLABV8.0";
    }
  }

  public void setExperience(String experience) {
       this.experience = experience;
  }

  public Boolean getSecurity() {
    return security;
  }

  public String getHost() {
    return host;
  }

  public String getPort() {
    return port;
  }

  public String getPath() {
    return spath;
  }

  public String getExperience() {
    return experience;
  }
  
  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public void setCredentials(String username, String password) {
    this.username = username;
    this.password = password;
  }
  
  public List<Map<String, String>> getWebsocketProxies() {
    return websocketProxies;
  }

  public List<Map<String, String>> getHtmlProxies() {
    return htmlProxies;
  }

  public void addWebsocketsLink(String id, String ip, String path, String type, String element) {
    Map<String, String> row = new HashMap<>();
    row.put(ID, id);
    row.put(IP, ip);
    row.put(PATH, path);
    row.put(TYPE, type);
    row.put(ELEMENT, element);
    websocketLinks.add(row);
  }

  public void addLink(String id, String ip, String path, String type, String element) {
    Map<String, String> row = new HashMap<>();
    row.put(ID, id);
    row.put(IP, ip);
    row.put(PATH, path);
    row.put(TYPE, type);
    row.put(ELEMENT, element);
    htmlLinks.add(row);
  }
  
  public void addLinks(Vector data) {
    htmlLinks.clear();
    Iterator iter = data.iterator();
    while (iter.hasNext()) {
      Vector row = (Vector)iter.next();
      String id = (String)row.get(0),
          ip = (String)row.get(1),
          path = (String)row.get(2),
          type = (String)row.get(3),
          element = (String)row.get(4);
      addLink(id, ip, path, type, element);
    }
  }

  public void addWebsocketsLinks(Vector data) {
    websocketLinks.clear();
    Iterator iter = data.iterator();
    while (iter.hasNext()) {
      Vector row = (Vector)iter.next();
      String id = (String)row.get(0),
          ip = (String)row.get(1),
          path = (String)row.get(2),
          type = (String)row.get(3),
          element = (String)row.get(4);
      addWebsocketsLink(id, ip, path, type, element);
    }
  }
  
  public List<Map<String, String>> getHtmlLinks() {
    return htmlLinks;
  }

  public List<Map<String, String>> getWebsocketsLinks() {
    return websocketLinks;
  }
  
  public String load(String config) {
    JsonObject response;
    try {
      InputStream stream = new ByteArrayInputStream(config.getBytes("UTF-8"));
      JsonReader reader = Json.createReader(stream);
      response = reader.readObject();
      JsonObject websocketProxyList = (JsonObject)response.get("ListConnectionProxyWebsocket");
      this.websocketProxies = getProxies(websocketProxyList, "ConnectionProxyWebsocket");
      JsonObject htmlProxyList = (JsonObject)response.get("ListConnectionProxyHTML");
      this.htmlProxies = getProxies(htmlProxyList, "ConnectionProxyHTML");
    } catch (UnsupportedEncodingException | JsonException e) {
      System.err.println(e.getCause());
      return null;
    }
    return "";
  }
  
  private ArrayList<Map<String, String>> getProxies(JsonObject proxyList, String key) {
    ArrayList<Map<String, String>> proxies = new ArrayList<>();
    JsonArray proxiesArray = (JsonArray)proxyList.get(key);
    Iterator<JsonValue> iter = proxiesArray.iterator();
    while (iter.hasNext()) {
      JsonObject proxy = (JsonObject)iter.next();
      if (proxy != null) {
        HashMap<String, String> htmlProxy = new HashMap<>();
        String ip = proxy.getString("IPInternal");
        int port = proxy.getInt("PortInternal");
        String description = proxy.getString("Description");
        htmlProxy.put("url", ip+":"+port);
        htmlProxy.put("description", description);
        proxies.add(htmlProxy);
      }
    }
    return proxies;
  }
  
  public void restore(String state) {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      InputSource is = new InputSource(new StringReader(state));        
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(is);
      security = doc.getElementsByTagName(XNL_SECURITY).item(0).getTextContent().equals("true");
      host = doc.getElementsByTagName(XNL_SERVER).item(0).getTextContent();
      port = doc.getElementsByTagName(XNL_PORT).item(0).getTextContent();
      spath = doc.getElementsByTagName(XNL_SPATH).item(0).getTextContent();
      experience = doc.getElementsByTagName(XNL_EXPERIENCE).item(0).getTextContent();
      username = doc.getElementsByTagName(XNL_USERNAME).item(0).getTextContent();
      password = doc.getElementsByTagName(XNL_PASSWORD).item(0).getTextContent();

      htmlLinks.clear();
      NodeList links = doc.getElementsByTagName(XNL_LINKS).item(0).getChildNodes();
      for (int i=0; i<links.getLength(); i++) {
        NamedNodeMap info = links.item(i).getAttributes();
        String id = info.getNamedItem(XNL_ID).getTextContent(),
            ip = info.getNamedItem(XNL_IP).getTextContent(),
            path = info.getNamedItem(XNL_PATH).getTextContent(),
            type = info.getNamedItem(XNL_TYPE).getTextContent(),
            element = info.getNamedItem(XNL_ELEMENT).getTextContent();
        addLink(id, ip, path, type, element);
      }

      websocketLinks.clear();
      links = doc.getElementsByTagName(XNL_WEBSOCKETS_LINKS).item(0).getChildNodes();
      for (int j=0; j<links.getLength(); j++) {
        NamedNodeMap info = links.item(j).getAttributes();
        String id = info.getNamedItem(XNL_ID).getTextContent(),
            ip = info.getNamedItem(XNL_IP).getTextContent(),
            path = info.getNamedItem(XNL_PATH).getTextContent(),
            type = info.getNamedItem(XNL_TYPE).getTextContent(),
            element = info.getNamedItem(XNL_ELEMENT).getTextContent();
        addWebsocketsLink(id, ip, path, type, element);
      }
    } catch (ParserConfigurationException | SAXException | IOException e) {
      System.err.println("Error al restaurar el estado del elemento.");
    } catch (Exception e) {
      System.err.println("Error desconocido al restaurar el estado del elemento.");
    }
  }

  public String dump() {
    String result = "<" + XNL_SARLAB + ">"
         + "<" + XNL_SECURITY + ">" + security + "</" + XNL_SECURITY + ">"
         + "<" + XNL_SERVER + ">" + host + "</" + XNL_SERVER + ">"
         + "<" + XNL_PORT + ">" + port + "</" + XNL_PORT + ">"
         + "<" + XNL_SPATH + ">" + spath + "</" + XNL_SPATH + ">"
         + "<" + XNL_EXPERIENCE + ">" + experience + "</" + XNL_EXPERIENCE + ">"
         + "<" + XNL_USERNAME + ">" + username + "</" + XNL_USERNAME + ">"
         + "<" + XNL_PASSWORD + ">" + password + "</" + XNL_PASSWORD + ">";
    result += "<" + XNL_LINKS + ">";
    for (Map<String, String> link : htmlLinks) {
        result += "<" + XNL_LABEL_LINK + " " + 
            XNL_ID + "=\"" + link.get("id") + "\" " +
            XNL_IP + "=\"" + link.get("ip") + "\" " +
            XNL_PATH + "=\"" + link.get("path") + "\" " +
            XNL_TYPE + "=\"" + link.get(TYPE) + "\" " +
            XNL_ELEMENT + "=\"" + link.get("element") + "\" " +
        "></" + XNL_LABEL_LINK + ">";
    }
    result += "</" + XNL_LINKS + ">"; 
    result += "<" + XNL_WEBSOCKETS_LINKS + ">";
    for (Map<String, String> link : websocketLinks) {
      result += "<" + XNL_LABEL_LINK + " " +
          XNL_ID + "=\"" + link.get("id") + "\" " +
          XNL_IP + "=\"" + link.get("ip") + "\" " +
          XNL_PATH + "=\"" + link.get("path") + "\" " +
          XNL_TYPE + "=\"" + link.get(TYPE) + "\" " +
          XNL_ELEMENT + "=\"" + link.get("element") + "\" " +
      "></" + XNL_LABEL_LINK + ">";
    }
    result += "</" + XNL_WEBSOCKETS_LINKS + ">"; 
    result += "</" + XNL_SARLAB + ">";
    return result;
  }
  
}