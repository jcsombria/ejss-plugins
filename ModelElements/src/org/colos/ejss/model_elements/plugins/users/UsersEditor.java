/**
 * 
 */
package org.colos.ejss.model_elements.plugins.users;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.SearchResult;
import org.colos.ejss.xml.SimulationXML;
import org.opensourcephysics.display.OSPRuntime;

/**
 * @author Jesús Chacón
 *
 */
public class UsersEditor implements Editor {
  private static final String RETRIEVE_USERS = "Retrieve Users";
  private static final String SEND_USERS = "Send Users";
  private static final String LOAD_CSV = "Import ...";
  private org.colos.ejs.osejs.Osejs mEjs;
  private String name = "";
  private boolean internal = true;
  private boolean active = true;
  private boolean changed = false;

  private JPanel mainPanel;
  private UsersTableModel usersTableModel = new UsersTableModel();
  private UsersTable usersTable;
  private JTextField urlText;
  private JTextField userText;
  private JPasswordField passwordText;
  private UserUpdater updater = new SocketIoUserUpdater();

  public static void main(String args[]) {
    System.out.println("Testing UsersPlugin Interface");
    Osejs ejs = new Osejs();
    new UsersEditor(ejs).test();
  }

  public void test() {
    JFrame frame = new JFrame("Users Editor - Test");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);
  }

  /**
   * 
   */
  public UsersEditor(org.colos.ejs.osejs.Osejs _ejs) {
    mEjs = _ejs;

    List<User> list = new ArrayList<User> ();
    list.add(new User());
    usersTableModel.setDataVector(list);
    usersTable = new UsersTable(usersTableModel);
    JScrollPane usersTableScroll = new JScrollPane(usersTable);
    usersTableScroll.setPreferredSize(new Dimension(800, 600));

    // TO DO: Move to a better place
    AbstractAction sendAction = new AbstractAction(SEND_USERS) {
      @Override
      public void actionPerformed(ActionEvent e) {
        Vector data = usersTableModel.getDataVector();
        updater.setHost(urlText.getText());
        updater.setCredentials(userText.getText(), new String(passwordText.getPassword()));
        updater.clear();
        int n = data.size();
        for(int i=0; i<n; i++) {
          Vector row = (Vector)data.elementAt(i);
          String id = (String)row.get(0);
          String user = (String)row.get(1);
          String name = (String)row.get(2);
          String password = (String)row.get(3);
          String emails = (String)row.get(4);
          String permissions = (String)row.get(5);
          User anUser = new User(id, user, name, password, emails.split(";"), permissions);
          updater.addUser(anUser);
        }
        try {
          updater.send();
          JOptionPane.showMessageDialog(mEjs.getMainFrame(), "The users' list has been sent to the server.", "Users Updated", JOptionPane.INFORMATION_MESSAGE);
        } catch (AuthenticationException e1) {
          JOptionPane.showMessageDialog(mEjs.getMainFrame(), "Invalid username or password.", "Authentication Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    AbstractAction retrieveAction = new AbstractAction(RETRIEVE_USERS) {
      @Override
      public void actionPerformed(ActionEvent e) {
        updater.setHost(urlText.getText());
        updater.setCredentials(userText.getText(), new String(passwordText.getPassword()));
        try {
          List<User> users = updater.getList();
          usersTableModel.setDataVector(users);
          usersTable.setEditors();
          JOptionPane.showMessageDialog(mEjs.getMainFrame(), "The users' list has been updated with the information retrieved from the server.", "Users Updated", JOptionPane.INFORMATION_MESSAGE);
        } catch(AuthenticationException e1) {
          JOptionPane.showMessageDialog(mEjs.getMainFrame(), "Invalid username or password.", "Authentication Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    AbstractAction importAction = new AbstractAction(LOAD_CSV) {
      @Override
      public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV File", "csv");
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);
        int result = chooser.showOpenDialog(mEjs.getMainFrame());
        if(result == JFileChooser.APPROVE_OPTION) {
          File file = chooser.getSelectedFile();
          updater.clear();
          String delimiter;
          boolean valid = false;
          do {
            delimiter = JOptionPane.showInputDialog("Field delimiter:", ",");
            valid = (delimiter.length() == 1);
            if(!valid) {
              JOptionPane.showMessageDialog(mEjs.getMainFrame(), "The delimiter must be a character."); 
            }
          } while(!valid);
          List<User> users = updater.fromCSV(file, delimiter.charAt(0));
          usersTableModel.setDataVector(users);
          usersTable.setEditors();
          JOptionPane.showMessageDialog(mEjs.getMainFrame(), "Users imported from csv file.", "Import Users", JOptionPane.INFORMATION_MESSAGE);
        }
      }
    };
    SpringLayout layout = new SpringLayout();
    JPanel configPanel = new JPanel(layout);
    JLabel urlLabel = new JLabel("Host:");
    urlText = new JTextField("http://localhost:8080", 25);
    JLabel userLabel = new JLabel("Username:");
    userText = new JTextField(12);
    JLabel passwordLabel = new JLabel("Password:");
    passwordText = new JPasswordField(12);
    configPanel.add(urlLabel);
    configPanel.add(urlText);
    configPanel.add(userLabel);
    configPanel.add(userText);
    configPanel.add(passwordLabel);
    configPanel.add(passwordText);
    layout.putConstraint(SpringLayout.VERTICAL_CENTER, urlLabel, 0, SpringLayout.VERTICAL_CENTER, configPanel);
    layout.putConstraint(SpringLayout.WEST, urlLabel, 10, SpringLayout.WEST, configPanel);
    layout.putConstraint(SpringLayout.WEST, urlText, 10, SpringLayout.EAST, urlLabel);
    layout.putConstraint(SpringLayout.WEST, userLabel, 10, SpringLayout.EAST, urlText);
    layout.putConstraint(SpringLayout.WEST, userText, 10, SpringLayout.EAST, userLabel);
    layout.putConstraint(SpringLayout.WEST, passwordLabel, 10, SpringLayout.EAST, userText);
    layout.putConstraint(SpringLayout.WEST, passwordText, 10, SpringLayout.EAST, passwordLabel);
    layout.putConstraint(SpringLayout.EAST, passwordText, -10, SpringLayout.EAST, configPanel);
    layout.putConstraint(SpringLayout.VERTICAL_CENTER, urlText, 0, SpringLayout.VERTICAL_CENTER, urlLabel);
    layout.putConstraint(SpringLayout.VERTICAL_CENTER, userLabel, 0, SpringLayout.VERTICAL_CENTER, urlText);
    layout.putConstraint(SpringLayout.VERTICAL_CENTER, userText, 0, SpringLayout.VERTICAL_CENTER, userLabel);
    layout.putConstraint(SpringLayout.VERTICAL_CENTER, passwordLabel, 0, SpringLayout.VERTICAL_CENTER, userText);
    layout.putConstraint(SpringLayout.VERTICAL_CENTER, passwordText, 0, SpringLayout.VERTICAL_CENTER, passwordLabel);
    configPanel.setPreferredSize(new Dimension(800, 50));
    JPanel buttonPanel = new JPanel();
    JButton sendButton = new JButton(sendAction);
    JButton retrieveButton = new JButton(retrieveAction);
    JButton importButton = new JButton(importAction);

    buttonPanel.add(sendButton);
    buttonPanel.add(retrieveButton);
    buttonPanel.add(importButton);
    mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    mainPanel.setPreferredSize(new Dimension(800, 600));

    mainPanel.add(configPanel, BorderLayout.NORTH);
    mainPanel.add(usersTableScroll, BorderLayout.CENTER);
    mainPanel.add(buttonPanel, BorderLayout.SOUTH);
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
  public List<SearchResult> search(String _info, String _searchString, int _mode) {
    return null;
  }

}

class UsersTableModel extends DefaultTableModel {
  private final String[] COLUMNS = {"Id", "Username", "Display Name", "Password", "E-mail", "Permissions"};
  
  public void setDataVector(Object[][] dataVector) {
    setDataVector(dataVector, COLUMNS);
  }

  public void setDataVector(List<User> users) {
    Object[][] data = new Object[users.size()][];
    int i = 0;
    for (User u : users) {
      data[i++] = u.toArray();
    }
    this.setDataVector(data, COLUMNS);
  }

  public Class<?> getColumnClass(int c) {
    try {
      return getValueAt(0, c).getClass();
    } catch(Exception e) {
      return String.class;
    }
  }

  public boolean isCellEditable(int row, int column) {
    return true;
  }
}

class UsersTable extends JTable {
  
  public UsersTable(DefaultTableModel model) {
    super(model);
    this.setEditors();
  }
  
  public void setEditors() {
    PasswordRenderer passwordRenderer = new PasswordRenderer();
    EmailEditor emailEditor = new EmailEditor();
    PermissionsEditor permissionsEditor = new PermissionsEditor();
    getColumnModel().getColumn(3).setCellRenderer(passwordRenderer);
    getColumnModel().getColumn(4).setCellEditor(emailEditor);
    getColumnModel().getColumn(5).setCellEditor(permissionsEditor);
  }
}

class PasswordRenderer extends JPasswordField implements TableCellRenderer {

  public PasswordRenderer() {
    super();
    this.setText("filler123");
  }

  public Component getTableCellRendererComponent(JTable arg0, Object arg1, boolean arg2, boolean arg3, int arg4, int arg5) {
    return this;
  }
}


class PermissionsEditor extends DefaultCellEditor {
  public static final String[] permissions = { User.P_USER, User.P_ADMIN, User.P_SUPERVISOR, User.P_READ_ONLY };
  
  public PermissionsEditor() {
    super(new JComboBox<String>(permissions));
  }
}

class EmailEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
  JButton button;
  JDialog dialog;
  JTable table;
  JPanel panel;
  private String[] emails;
  private DefaultTableModel tableModel = new DefaultTableModel() {
    @Override
    public boolean isCellEditable(int row, int column) {
      return true;
    }
  };
  private JButton closeButton;
  private JPopupMenu popupMenu;
  protected static final String EDIT = "edit";
  protected static final String CLOSE = "close";

  public EmailEditor() {
      button = new JButton();
      button.setActionCommand(EDIT);
      button.addActionListener(this);
      button.setBorderPainted(false);

      //Set up the dialog that the button brings up.
      panel = new JPanel(new BorderLayout());
      panel.setPreferredSize(new Dimension(400, 300));
      table = new JTable(tableModel);
      table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      popupMenu = new JPopupMenu();
      AbstractAction addEmail = new AbstractAction("+ Add New Email") {
        @Override
        public void actionPerformed(ActionEvent e) {
          tableModel.addRow(new String[] {""});
        }
      };
      AbstractAction deleteEmail = new AbstractAction("- Delete Email") {
        @Override
        public void actionPerformed(ActionEvent e) {
          int row = table.getSelectedRow();
          if (row != -1) {
            tableModel.removeRow(row);
          }
        }
      };
      popupMenu.add(addEmail);
      popupMenu.add(deleteEmail);
      
      table.addMouseListener(new MouseAdapter() {
          public void mousePressed (MouseEvent _evt) {
            if (OSPRuntime.isPopupTrigger(_evt) && table.isEnabled ()) {
              int row = table.rowAtPoint(_evt.getPoint()); 
              if(row != -1) table.setRowSelectionInterval(row, row);
              popupMenu.show(_evt.getComponent(), _evt.getX(), _evt.getY());
            }
          }
      });
      panel.add(table, BorderLayout.CENTER);

      closeButton = new JButton(CLOSE);
      closeButton.setActionCommand(CLOSE);
      closeButton.addActionListener(this);

      dialog = new JDialog((JFrame)null, "Edit e-mails", true); 
      dialog.setLayout(new BorderLayout());
      dialog.add(panel, BorderLayout.CENTER);
      dialog.add(closeButton, BorderLayout.SOUTH);
      dialog.pack();
  }
  
  public void actionPerformed(ActionEvent e) {
    if (EDIT.equals(e.getActionCommand())) {
      tableModel.setRowCount(0);
      tableModel.setColumnCount(0);
      tableModel.addColumn("e-mail", emails);
      dialog.setVisible(true);
      fireEditingStopped(); //Make the renderer reappear.
    } else {
      int n = tableModel.getRowCount();
      emails = new String[n];
      for(int i=0; i<n; i++) {
        emails[i] = (String)tableModel.getValueAt(i, 0);
      }
      dialog.setVisible(false);
    }
  }

  public Object getCellEditorValue() {
    return String.join(";", emails);
  }

  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    emails = ((String)value).split(";");
    return button;
  }

}