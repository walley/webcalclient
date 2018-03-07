package org.walley.webcalclient2;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.cookie.Cookie;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.StatusLine;

public class wcc_http
{
  private String http_username;
  private String http_password;
  private String app_username;
  private String app_password;

  /******************************************************************************/
  public wcc_http(String u, String p)
  /******************************************************************************/
  {
    http_username = u;
    http_password = p;
  }

  /******************************************************************************/
  public wcc_http(String u, String p, String au, String ap)
  /******************************************************************************/
  {
    http_username = u;
    http_password = p;
    app_username = au;
    app_password = ap;
  }

  /******************************************************************************/
  public void set_credentials(String u, String p, String au, String ap)
  /******************************************************************************/
  {
    http_username = u;
    http_password = p;
    app_username = au;
    app_password = ap;
  }

  /******************************************************************************/
  public String get_credentials()
  /******************************************************************************/
  {
    //Log.d("WC","get_credentials(): " + http_username + ":" + http_password);
    return Base64.encodeToString((http_username + ":" + http_password).getBytes(), Base64.NO_WRAP);
  }


  /******************************************************************************/
  public void show_cookies(DefaultHttpClient hc)
  /******************************************************************************/
  {
    Log.d("WC","show_cookies():");
    List<Cookie> cookies = hc.getCookieStore().getCookies();
    if (cookies.isEmpty()) {
      Log.d("WC","# No cookies");
    } else {
      for (int i = 0; i < cookies.size(); i++) {
        Log.d("WC","# " + i + " " + cookies.get(i).toString());
      }
    }
  }

  /******************************************************************************/
  public int login_and_get_cookie(DefaultHttpClient hc) throws LoginException
  /******************************************************************************/
  {
    HttpResponse response;
    HttpEntity entity;
    int ret = 1;
    String err = "nothing";

    try {

      show_cookies(hc);

      HttpPost httppost = new HttpPost("https://dokumenty.hasici-ol.cz/wcoslejsek/login.php");
      httppost.setHeader("Authorization", "Basic " + get_credentials());

      List <NameValuePair> nvps = new ArrayList <NameValuePair>();
      nvps.add(new BasicNameValuePair("login", app_username));
      nvps.add(new BasicNameValuePair("password", app_password));

      httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

      response = hc.execute(httppost);
      entity = response.getEntity();

      StatusLine status = response.getStatusLine();
      Log.d("WC","login_and_get_cookie() - login form get: " + status);

      if (status.getStatusCode() != 200) {
        Log.e("WC","login_and_get_cookie() - login error: " + status.toString());
        throw new LoginException(status.toString());
      }

      Log.d("WC","login_and_get_cookie() - login ok: " + status);

      InputStream inputStream = entity.getContent();
      ByteArrayOutputStream content = new ByteArrayOutputStream();

      // Read response into a buffered stream
      byte[] sBuffer = new byte[512];
      int readBytes = 0;
      while ((readBytes = inputStream.read(sBuffer)) != -1) {
        content.write(sBuffer, 0, readBytes);
      }

      // Return result from buffered stream
      String str_result = new String(content.toByteArray());

      String patternString1 = "<!--ERROR(.*)ERROR-->";

      Pattern pattern = Pattern.compile(patternString1);
      Matcher matcher = pattern.matcher(str_result);

      if (matcher.find()) {
        err = matcher.group(1);
        Log.d("WC","login_and_get_cookie(): error found: " + err);
      } else {
        Log.d("WC","login_and_get_cookie(): logged in! ");
        err = "(OK)";
      }

      show_cookies(hc);

      entity_consume_content(entity);

    } catch (HttpHostConnectException e) {
      Log.e("WC","login_and_get_cookie() - connection refused: " + e.toString());
      ret = 0;
//      Toast.makeText(this, getResources().getString(R.string.connection_refused), Toast.LENGTH_LONG).show();
    } catch (Exception e) {
      Log.e("WC","login_and_get_cookie() - general error" + e.toString());
      ret = 0;
    }

    if (!err.equals("(OK)")) {
      Log.d("WC","login_and_get_cookie: " + err);
      throw new LoginException(err);
    }

    return ret;
  }


  /******************************************************************************/
  public void entity_consume_content(HttpEntity entity)
  /******************************************************************************/
  {
    if (entity != null) {
      try {
        entity.consumeContent();
      } catch (IOException e) {
        Log.e("WC","error " + e.toString());
      }
    }
  }
}
