package org.colos.ejss.model_elements.plugins.installer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.swing.ImageIcon;

import org.colos.ejs.model_elements.AbstractModelElement;

public class PluginInfo {
  public String name = "";
  public boolean enabled = false;
  public boolean userCanDisable = false;
  public File file;
  List<String> plugins;
  List<Map<String, Object>> info;
  
  PluginInfo() {}

  PluginInfo(File f) {
    this.file = f;
    JarFile jf = null;
    try {
      jf = new JarFile(f);
      Object[] info = this.getInfo(jf.getName());
      this.name = (String)info[0];
      this.enabled = (boolean)info[1];
      this.plugins = this.findPlugins(jf);
      this.info = loadInfo(jf, this.plugins);
    } catch(IOException e) {
      System.err.println("[ERROR] Cannot retrieve plugin info.");
    } finally {
      try {
        jf.close();
      } catch (IOException e) {}
    }
  }

  private Object[] getInfo(String path) {
    int i = path.lastIndexOf("/");
    int j = path.length();
    boolean isEnabled = false;
    String disabled = ".jar.disabled", enabled = ".jar"; 
    if(path.endsWith(disabled)) {
      j -= disabled.length();
      isEnabled = false;
    } else if (path.endsWith(enabled)) {
      j -= enabled.length();
      isEnabled = true;
    }
    String name = path.substring(i+1, j);
    return new Object[] {name, isEnabled};
  }
 
  private List<String> findPlugins(JarFile f) {
    List<String> plugins = new ArrayList<>();
    URLClassLoader cl = null;
    try {
      Enumeration<JarEntry> entries = f.entries();
      String path = String.format("jar:file:%s!/", f.getName());
      URL[] urls = { new URL(path) };
      cl = new URLClassLoader(urls);
      while (entries.hasMoreElements()) {
        JarEntry e = entries.nextElement();
        try {
          String name = e.getName();
          if(name.endsWith(".class")) {
            String classname = name.substring(0, name.length()-6).replace('/', '.'); 
            Class<?> c = cl.loadClass(classname);
            if(org.colos.ejs.osejs.plugins.Plugin.class.isAssignableFrom(c)) {
              plugins.add(name);
            }
          }
        } catch (ClassNotFoundException e1) {
          e1.printStackTrace();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        cl.close();
      } catch (IOException e) {}
    }
    return plugins;
  }
  
  private List<Map<String, Object>> loadInfo(JarFile jf, List<String> plugins) {
    List<Map<String, Object>> pluginInfoList = new ArrayList<>();
    for (String p : plugins) {
      int i = p.lastIndexOf('/');
      String dir = p.substring(0, i);
      String name = p.substring(i+1, p.length()-6);
      String path = String.format("%s/%s_info.js", dir, name);
      String theName = "", theDescription = "", theIcon = "";
      try {
        InputStream is = jf.getInputStream(jf.getEntry(path));
        JsonReader jsonReader = Json.createReader(is);
        JsonArray array = jsonReader.readArray();
        JsonObject obj = array.getJsonObject(0);
        Map<String, Object> info = new HashMap<>();
        theName = obj.getString("name");
        theDescription = obj.getString("description");
        theIcon = obj.getString("icon");
        info.put("name", theName);
        info.put("description", theDescription);
        ImageIcon icon = AbstractModelElement.createImageIcon(theIcon);
        try {
          icon = new ImageIcon(icon.getImage().getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH));
        } catch(Exception e) {
          System.err.println("[WARNING] Cannot load plugin icon.");
          icon = new ImageIcon();
        }
        info.put("icon", icon);
        pluginInfoList.add(info);
        jsonReader.close();
      } catch(Exception e) {
        System.out.println(String.format("[ERROR] Cannot open %s info", name));
      }
    }
    return pluginInfoList;
  }
  
  public boolean disable() {
    System.out.println(this.enabled);
    if(!this.enabled || this.name.contains("PluginInstaller")) {
      return false;
    }
    this.enabled = false;
    String f = this.file.getAbsolutePath() + ".disabled";
    File p = this.file;
    this.file = new File(f);
    p.renameTo(this.file);
    return true;
  }

  public boolean enable() {
    if(this.enabled) {
      return false;
    }
    this.enabled = true;
    String f = this.file.getAbsolutePath();
    f = f.substring(0, f.lastIndexOf("."));
    File p = this.file;
    this.file = new File(f);
    p.renameTo(this.file);
    return true;
  }

  @Override
  public String toString() {
    return this.name;
  }
  
  public String getName() {
    return this.name;
  }
  
  public boolean matchName(String name) {
    return name.equals(this.name) || name.equals(getDisabledName()) || name.equals(getEnabledName());
  }

  public String getEnabledName() {
     return this.name + ".jar";
  }

  public String getDisabledName() {
    return this.name + ".jar.disabled";
 }
  
  public String getModifiedName() {
    return this.name + (this.enabled ? ".jar" : ".jar.disabled");
  }
}