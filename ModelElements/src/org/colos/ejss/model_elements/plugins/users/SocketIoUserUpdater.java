package org.colos.ejss.model_elements.plugins.users;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.httpclient.Credentials;
import org.json.JSONArray;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketIoUserUpdater extends UserUpdater {
  public static final String QUERY_TEMPLATE = "mode=maintenance&user=%s&password=%s";
  public static final String URL_TEMPLATE = "http://%s:%s";
  public static final String EVENT_GET_USERS = "users.get";
  public static final String EVENT_SET_USERS = "users.set";
  public static final String EVENT_LOGIN_ERROR = "login_error";
  public static final int CONNECTION_TIMEOUT_SECONDS = 5;
  private Socket socket;
  private boolean loginError = false;

  public List<User> getList() throws AuthenticationException {
    Socket socket = createConnection();
    socket.connect();
    try {
      waitConnection(socket, CONNECTION_TIMEOUT_SECONDS);
      if (socket.connected()) {
        System.out.println("[INFO] Connected");
        socket.emit(EVENT_GET_USERS, new Object[] {});
      }
      if (loginError) {
        throw new AuthenticationException();
      }
      waitDisconnection(socket, CONNECTION_TIMEOUT_SECONDS);
    } catch (InterruptedException e) {
      System.err.println("[ERROR] The thread was interrupted.");
      e.printStackTrace();
    }
    return users;
  }

  private Socket createConnection() {
    String query = String.format(QUERY_TEMPLATE, user, password);
    String url = String.format(URL_TEMPLATE, host, port);

    IO.Options opts = new IO.Options();
    opts.forceNew = true;
    opts.query = query;
    opts.reconnection = false;
    
    try {
      socket = IO.socket(url, opts);

      socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
        @Override
        public void call(Object... arg0) {
          System.out.println("[INFO] " + Socket.EVENT_CONNECT);
        }
      });
      socket.on(EVENT_GET_USERS, new Emitter.Listener() {
        @Override
        public void call(Object... arg0) {
          System.out.println("[INFO] " + EVENT_GET_USERS);
          clear();
          try {
            JSONArray usersArray = (JSONArray) arg0[0];
            int n = usersArray.length();
            for(int i=0; i<n; i++) {
              JSONObject u = (JSONObject)usersArray.get(i);
              User anUser = new User(u);
              users.add(anUser);
            }
            socket.disconnect();
          } catch(Exception e) {}
        }
      });
      socket.on(EVENT_SET_USERS, new Emitter.Listener() {
        @Override
        public void call(Object... arg0) {
          socket.disconnect();
        }
      });
      socket.on(EVENT_LOGIN_ERROR, new Emitter.Listener() {
        @Override
        public void call(Object... arg0) {
          loginError = true;
        }
      });
      socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
        @Override
        public void call(Object... args) {
          System.out.println("[INFO] " + Socket.EVENT_DISCONNECT);
        }
      });
    } catch (URISyntaxException e) {
      System.err.println("[ERROR] Incorrect URI");
      e.printStackTrace();
    }
    return socket;
  }

  @Override
  public void send() throws AuthenticationException {
    Socket socket = createConnection();
    socket.connect();
    try {
      waitConnection(socket, CONNECTION_TIMEOUT_SECONDS);
      if (socket.connected()) {
        System.out.println("[INFO] Connected");
        JsonArrayBuilder users = Json.createArrayBuilder();
        for(User u : this.users) {
          users.add(u.toJson());
        }
        JsonArray b = users.build();
        socket.emit(EVENT_SET_USERS, new Object[] {b.toString()});
      }
      if (loginError) {
        throw new AuthenticationException();
      }
      waitDisconnection(socket, CONNECTION_TIMEOUT_SECONDS);
    } catch (InterruptedException e) {
      System.err.println("[ERROR] The thread was interrupted.");
      e.printStackTrace();
    }
  }
 
  private void waitConnection(Socket socket, int timeout) throws InterruptedException {
    int t = 0;
    while (!socket.connected() && t <= (10 * CONNECTION_TIMEOUT_SECONDS)) {
      Thread.sleep(100);
      t = t + 1;
    }
  }

  private void waitDisconnection(Socket socket, int timeout) throws InterruptedException {
    int t = 0;
    while (socket.connected() && t <= (10 * CONNECTION_TIMEOUT_SECONDS)) {
      Thread.sleep(100);
      t = t + 1;
    }
  }
  
//  public void clear() {
//    users.clear();
//  }
//  
//  public void addUser(User anUser) {
//    users.add(anUser);
//  }
  
//  public JsonArray toJson() {
//    JsonArrayBuilder users = Json.createArrayBuilder();
//    for(User u : this.users) {
//      users.add(u.toJson());
//    }
//    return users.build();
//  }
//
//  public List<User> fromCSV(File csv, char delimiter) {
//    Reader in;
//    List<User> users = new ArrayList<User>();
//    try {
//      in = new FileReader(csv);
//      Iterable<CSVRecord> records = CSVFormat.EXCEL
//          .withFirstRecordAsHeader()
//          .withDelimiter(delimiter)
//          .parse(in);
//      for (CSVRecord record : records) {
//        String id = record.get(0);
//        String username = record.get(1);
//        String displayname = record.get(2);
//        String password = record.get(3);
//        String[] emails = ((String)record.get(4)).split(",");
//        String perms = record.get(5);
//        User u = new User(id, username, displayname, password, emails, perms);
//        users.add(u);
//      }
//    } catch (FileNotFoundException e) {
//      e.printStackTrace();
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//    return users;
//  }
}