package org.colos.ejss.model_elements.plugins.users;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpUserUpdater extends UserUpdater {
  public static final String QUERY_TEMPLATE = "?mode=maintenance&user=%s&password=%s";
  public static final String URL_TEMPLATE = "http://%s:%s/%s";
  public static final String GET_USERS = "users/get";
  public static final String SET_USERS = "users/set";

//  public static void main(String[] args) {
//    HttpUserUpdater updater = new HttpUserUpdater();
//    updater.setHost("http://localhost:5000");
//    updater.setCredentials("admin", "admin");
//    try {
//      List<User> users = updater.getList();
//      updater.send();
//    } catch(AuthenticationException e) {}
//  }

  @Override
  public List<User> getList() throws AuthenticationException {
    String url = String.format(URL_TEMPLATE, host, port, GET_USERS);
    HttpClient client = new HttpClient();
    HttpMethod get = new GetMethod(url);
    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, password); 
    client.getState().setCredentials(new AuthScope("localhost", 5000, AuthScope.ANY_REALM), credentials);
    get.setDoAuthentication(true);
    try {
      int statusCode = client.executeMethod(get);
      if (statusCode != HttpStatus.SC_OK) {
        System.err.println("Method failed: " + get.getStatusLine());
      }
      byte[] responseBody = get.getResponseBody();
      String body = new String(responseBody);
      System.out.println(body);
      try {
        JSONArray usersArray = new JSONArray(body);
        int n = usersArray.length();
        users.clear();
        for(int i=0; i<n; i++) {
          JSONObject u = (JSONObject)usersArray.get(i);
          User anUser = new User(u);
          users.add(anUser);
        }
      } catch (JSONException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } catch (HttpException e) {
      System.err.println("Fatal protocol violation: " + e.getMessage());
      e.printStackTrace();
    } catch (IOException e) {
      System.err.println("Fatal transport error: " + e.getMessage());
      throw new AuthenticationException();
    } finally {
      get.releaseConnection();
    }
    return users;
  }

  @Override
  public void send() throws AuthenticationException {
    String query = String.format(QUERY_TEMPLATE, user, password);
    String url = String.format(URL_TEMPLATE, host, port, SET_USERS);
    HttpClient client = new HttpClient();
    PostMethod post = new PostMethod(url+query);
    post.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,  new DefaultHttpMethodRetryHandler(3, false));
    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, password); 
    client.getState().setCredentials(new AuthScope("localhost", 5000, AuthScope.ANY_REALM), credentials);
    post.setDoAuthentication(true);
    try {
      JsonArrayBuilder users = Json.createArrayBuilder();
      for(User u : this.users) { users.add(u.toJson()); }
      JsonArray b = users.build();
      post.addRequestHeader("Content-Type", "application/json");
      RequestEntity entity = new StringRequestEntity(b.toString(), "application/json", "UTF-8");
      post.setRequestEntity(entity);
      int statusCode = client.executeMethod(post);
      if (statusCode != HttpStatus.SC_OK) {
        System.err.println("Method failed: " + post.getStatusLine());
      }
    } catch (HttpException e) {
      System.err.println("Fatal protocol violation: " + e.getMessage());
      e.printStackTrace();
    } catch (IOException e) {
      System.err.println("Fatal transport error: " + e.getMessage());
      e.printStackTrace();
    } finally {
      post.releaseConnection();
    }
  }
}