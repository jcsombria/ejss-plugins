package org.colos.ejss.model_elements.plugins.installer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class PluginManager {
  private List<PluginInfo> plugins = new ArrayList<>();
  private File folder;

  PluginManager(File folder) {
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
    return plugins;
  }
  
  public boolean install(File src) {
    if(find(src.getName())) {
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

  private boolean find(String name) {
    for (PluginInfo p : plugins) {
      if(name.equals(p.name + ".jar")) {
        return true;
      }
    }
    return false;
  }
}