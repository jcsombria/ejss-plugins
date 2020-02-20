package org.colos.ejss.model_elements.plugins.users;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class UserUpdater {
  protected List<User> users = new ArrayList<>();
  protected String user;
  protected String password;
  protected String host = "localhost";
  protected int port = 8080;

  public void send() throws AuthenticationException {}
  
  public List<User> getList() throws AuthenticationException {
    return users;
  }

  public void clear() {
    users.clear();
  }
  
  public void addUser(User anUser) {
    users.add(anUser);
  }
  
  public JsonArray toJson() {
    JsonArrayBuilder users = Json.createArrayBuilder();
    for(User u : this.users) {
      users.add(u.toJson());
    }
    return users.build();
  }

  public List<User> fromCSV(File csv, char delimiter) {
    Reader in;
    List<User> users = new ArrayList<User>();
    try {
      in = new FileReader(csv);
      Iterable<CSVRecord> records = CSVFormat.EXCEL
          .withFirstRecordAsHeader()
          .withDelimiter(delimiter)
          .parse(in);
      for (CSVRecord record : records) {
        String id = record.get(0);
        String username = record.get(1);
        String displayname = record.get(2);
        String password = record.get(3);
        String[] emails = ((String)record.get(4)).split(",");
        String perms = record.get(5);
        User u = new User(id, username, displayname, password, emails, perms);
        users.add(u);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return users;
  }
  
  public void setHost(String url) {
    try {
      URL theUrl =  new URL(url);
      this.host = theUrl.getHost();
      this.port = theUrl.getPort();
    } catch (MalformedURLException e) {
      System.err.println("[ERROR] Invalid Server URL.");
      e.printStackTrace();
    }
  }
  
  public void setCredentials(String username, String password) {
    this.user = username;
    this.password = password;
  }
}

class AuthenticationException extends Exception {}