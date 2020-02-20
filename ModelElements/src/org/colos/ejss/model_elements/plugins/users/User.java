package org.colos.ejss.model_elements.plugins.users;

import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

public class User {
  public static final String P_READ_ONLY = "RO";
  public static final String P_SUPERVISOR = "SUPERVISOR";
  public static final String P_ADMIN = "ADMIN";
  public static final String P_USER = "USER";
  public static final String F_ID = "id";
  public static final String F_NAME = "displayName";
  public static final String F_USERNAME = "username";
  public static final String F_PASSWORD = "password";
  public static final String F_EMAILS = "emails";
  public static final String F_PERMISSIONS = "permissions";
  
  public String id;
  public String username;
  public String name;
  public String password;
  public EmailList emails = new EmailList();
  public String permissions;

  User() {
    this.id = "0";
    this.username = "username";
    this.name = "User Name";
    this.password = "password";
    this.emails = new EmailList();
    this.permissions = "USER";
  }

  User(String id, String username, String name, String password, String[] email, String permissions) {
    this.id = id;
    this.username = username;
    this.name = name;
    this.password = password;
    this.emails = new EmailList(email);
    this.permissions = permissions;
  }
  
  User(JSONObject json) {
    try {
      this.id = "" + json.getInt(F_ID);
      this.name = json.getString(F_NAME);
      this.password = json.getString(F_PASSWORD);
      this.username = json.getString(F_USERNAME);
      this.emails = EmailList.fromJSON(json.getJSONArray(F_EMAILS));
    } catch(Exception e) {
      System.err.println("[ERROR] Cannot load user from JSON.");
    }
    try {
      JSONArray a = json.getJSONArray(F_PERMISSIONS);
      String[] perms = new String[a.length()];
      for(int i=0; i<a.length(); i++) {
        perms[i] = a.getString(i);
      }
      this.permissions = String.join(";", perms);
    } catch(Exception e) {
      this.permissions = P_USER;
      System.err.println("[ERROR] Cannot load user from JSON.");
    }
  }
  
  public static List<String> emailsAsList(String... emails) {
    List<String> emailsList = new ArrayList<>();
    for (String s : emails) {
      emailsList.add(s);
    }
    return emailsList;
  }

  public Object[] toArray() {
    return new Object[] {
        this.id,
        this.username,
        this.name,
        this.password,
        this.emails.toString(),
        this.permissions,
    };
  }

  public JsonObject toJson() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    builder.add(F_ID, this.id);
    builder.add(F_USERNAME, this.username);
    builder.add(F_NAME, this.name);
    builder.add(F_PASSWORD, this.password);
    builder.add(F_EMAILS, this.emails.toJson());
    JsonArrayBuilder permissionsBuilder = Json.createArrayBuilder();
    permissionsBuilder.add(this.permissions);
    builder.add(F_PERMISSIONS, permissionsBuilder.build());
    return builder.build();
  }


  public String toString() {
    return String.join(", ", this.id, this.username, this.password, this.name, this.emails.toString(), this.permissions);
  }
}

class EmailList {
  private List<String> emails = new ArrayList<>();

  public EmailList(String... emails) {
    for (String s : emails) {
      this.emails.add(s);
    }
  }

  public final List<String> getList() {
    return emails;
  }

  public static EmailList fromJSON(JSONArray json) {
    List<String> emails = new ArrayList<>();
    for(int i=0; i<json.length(); i++) {
      try {
        JSONObject e = (JSONObject) json.get(i);
        emails.add(e.getString("value"));
      } catch(Exception e) {
        System.err.println("[ERROR] Cannot parse emails.");
      }
    }
    return new EmailList(emails.toArray(new String[] {}));
  }

  public String[] toArray() {
    return emails.toArray(new String[] {});
  }
  
  public String toString() {
    String emails = "";
    for (String s : this.emails) {
      if(!emails.equals("")) {
        emails += ";";
      }
      emails += s;
    }
    return emails;
  }
  
  public JsonArray toJson() {
    JsonArrayBuilder builder = Json.createArrayBuilder();
    for(String s : emails) {
      JsonObjectBuilder b = Json.createObjectBuilder();
      b.add("value", s);
      builder.add(b.build());
    }
    return builder.build();
  }

  public boolean isValid() {
    return true;
  }
}
