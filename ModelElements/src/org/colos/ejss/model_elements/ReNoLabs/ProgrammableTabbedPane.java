/**
 * 
 */
package org.colos.ejss.model_elements.ReNoLabs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
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
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;

import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.ModelElement;
import org.colos.ejs.model_elements.ModelElementEditor;
import org.colos.ejs.model_elements.ModelElementMultipageEditor;
import org.colos.ejs.model_elements.ModelElementSearch;
import org.colos.ejs.model_elements.ModelElementTabbedEditor;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.osejs.GenerateJS;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.edition.CodeEditor;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.TabbedEditor;
import org.colos.ejs.osejs.edition.html_view.ElementsTree;
import org.colos.ejs.osejs.edition.html_view.OneView;
import org.colos.ejs.osejs.utils.FileUtils;
import org.colos.ejs.osejs.utils.ResourceUtil;
import org.colos.ejss.model_elements.input_output.WebSocketElement;
import org.colos.ejss.xml.JSObfuscator;
import org.colos.ejss.xml.XMLTransformerJava;
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
public class ProgrammableTabbedPane extends TabbedEditor {
  
  /**
   * 
   */
  public ProgrammableTabbedPane (org.colos.ejs.osejs.Osejs _ejs, String _header, boolean canDisablePages) {
    super(_ejs, Editor.CODE_EDITOR, _header, canDisablePages);
    // Initialize class elements
    
    //defaultString = _header;
  }
  
  public void AddPage(String _name, String _code, String _comment) {
    StringBuilder sb = new StringBuilder();
    sb.append("<Comment><![CDATA[").append(_comment).append("]]></Comment>");
    sb.append("<Code><![CDATA[\n").append(_code).append("\n]]></Code>");
    addPage(Editor.CODE_EDITOR, _name, sb.toString(), true, false);
  }
  
  public java.util.Vector<CodeEditor> getCodePages() {
    java.util.Vector<CodeEditor> result = new java.util.Vector<CodeEditor> ();
    for (Editor page : getPages()) {
      if (page instanceof CodeEditor) {
        result.add((CodeEditor) page);
      }
    }
    return result;
  }
  
  @Override
  protected void addPage (String _typeOfPage, String _name, String _code, boolean _enabled, boolean _internal) {
    super.addPage(_typeOfPage, _name, _code, _enabled, _internal);
    
    try {
      int index = tabbedPanel.getSelectedIndex();
      if (index<0) return;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

}
