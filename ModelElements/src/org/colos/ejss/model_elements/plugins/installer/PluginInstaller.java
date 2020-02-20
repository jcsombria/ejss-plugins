package org.colos.ejss.model_elements.plugins.installer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class PluginInstaller {
  private List<PluginInfo> plugins = new ArrayList<>();
  private File folder;

  PluginInstaller(File folder) {
    this.folder = folder;
    this.update();
  }
  
  private void update() {
    File[] files;
    try {
      files = folder.listFiles();
    } catch(SecurityException e) {
      files = new File[] {};
      System.err.println(e.getMessage());
    }
     plugins.clear();
     for (File f : files) {
       try {
         PluginInfo p = new PluginInfo(f);
         if (p != null) {
           plugins.add(p);
         }
       } catch(Exception e) {
       }
     }
  }
  
  List<PluginInfo> getPluginList() {
    update();
    return plugins;
  }
  
  public boolean install(File src) {
    if(find(src.getName()) != null) {
      return false;
    }
    try {
      File dst = new File(folder.getAbsolutePath() + File.separator + src.getName());
      dst.createNewFile();
      Files.copy(src.toPath(), new FileOutputStream(dst));
      return true;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean uninstall(String plugin) {
    PluginInfo p = find(plugin); 
    if(p == null) {
      return false;
    }
    String path = folder.getAbsolutePath() + File.separator + p.getModifiedName();
    System.out.println(path);
    File dst = new File(path);
    return dst.delete();
  }

  private PluginInfo find(String name) {
    for (PluginInfo p : plugins) {
      if(p.matchName(name)) {
        return p;
      }
    }
    return null;
  }
}