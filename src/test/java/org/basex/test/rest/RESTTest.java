package org.basex.test.rest;

import static org.basex.api.HTTPText.*;
import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.basex.api.BaseXHTTP;
import org.basex.api.rest.RESTText;
import org.basex.core.Text;
import org.basex.data.DataText;
import org.basex.util.Base64;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the REST implementation.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class RESTTest {
  /** REST identified. */
  private static final String NAME = "rest";
  /** REST URI. */
  private static final String URI = string(RESTText.RESTURI);
  /** Opening result. */
  private static final String WRAP =
      "<" + NAME + ":results xmlns:" + NAME + "=\"" + URI + "\"/>";
  /** Root path. */
  private static final String ROOT = "http://localhost:8984/" + NAME + '/';
  /** Input file. */
  private static final String FILE = "etc/test/input.xml";
  /** Test database. */
  private static final String DB = NAME;
  /** Start servers. */
  private static BaseXHTTP http;

  // INITIALIZERS =============================================================

  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeClass
  public static void start() throws Exception {
    http = new BaseXHTTP("-czWU" + Text.ADMIN + " -P" + Text.ADMIN);
  }

  /**
   * Stop server.
   * @throws Exception exception
   */
  @AfterClass
  public static void stop() throws Exception {
    http.stop();
  }

  // TEST METHODS =============================================================

  /**
   * GET Test.
   * @throws Exception exception
   */
  @Test
  public void get() throws Exception {
    assertEquals("1 2 3", get("?query=1+to+3&wrap=no"));
  }

  /**
   * GET Test.
   * @throws Exception exception
   */
  @Test
  public void get2() throws Exception {
    assertEquals(WRAP, get("?query=()&wrap=yes"));
  }

  /**
   * GET Test.
   * @throws IOException I/O exception
   */
  @Test
  public void getBind() throws IOException {
    assertEquals("123", get("?"
        + "query=declare+variable+$x+as+xs:integer+external;$x&$x=123"));
  }

  /**
   * GET Test.
   * @throws IOException I/O exception
   */
  @Test
  public void getBind2() throws IOException {
    assertEquals("124", get("?wrap=no&$x=123&"
        + "query=declare+variable+$x+as+xs:integer+external;$x%2b1"));
  }

  /**
   * GET Test.
   * @throws IOException I/O exception
   */
  @Test
  public void getBind3() throws IOException {
    assertEquals("6", get("?wrap=no&"
        + "query=declare+variable+$a++as+xs:integer+external;"
        + "declare+variable+$b+as+xs:integer+external;"
        + "declare+variable+$c+as+xs:integer+external;" + "$a*$b*$c&"
        + "$a=1&$b=2&$c=3"));
  }

  /** GET Test. */
  @Test
  public void getErr1() {
    try {
      get("?query=(");
      fail("Error expected.");
    } catch(final IOException ex) {
      assertContains(ex.getMessage(), "[XPST0003]");
    }
  }

  /** GET Test. */
  @Test
  public void getErr2() {
    try {
      get("?query=()&wrp=no");
      fail("Error expected.");
    } catch(final IOException ex) {
    }
  }

  /** GET Test. */
  @Test
  public void getErr3() {
    try {
      get("?query=()&wrap=n");
      fail("Error expected.");
    } catch(final IOException ex) {
    }
  }

  /** GET Test. */
  @Test
  public void getErr4() {
    try {
      get("?query=()&method=xxx");
      fail("Error expected.");
    } catch(final IOException ex) {
    }
  }

  /**
   * POST Test: execute a query.
   * @throws IOException I/O exception
   */
  @Test
  public void postQuery1() throws IOException {
    assertEquals("123",
        postQuery("", "<query xmlns=\"" + URI + "\">" +
          "<text>123</text><parameter name='wrap' value='no'/></query>"));
  }

  /**
   * POST Test: execute a query.
   * @throws IOException I/O exception
   */
  @Test
  public void postQuery2() throws IOException {
    assertEquals("",
        postQuery("", "<query xmlns=\"" + URI + "\">" +
          "<text>()</text><parameter name='wrap' value='no'/></query>"));
  }

  /**
   * POST Test: execute a query.
   * @throws IOException I/O exception
   */
  @Test
  public void postQuery3() throws IOException {
    assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?> 123",
        postQuery("", "<query xmlns=\"" + URI + "\">" +
          "<text>123</text><parameter name='wrap' value='no'/>" +
          "<parameter name='omit-xml-declaration' value='no'/></query>"));
  }

  /**
   * POST Test: execute a query and ignore/overwrite duplicates declarations.
   * @throws IOException I/O exception
   */
  @Test
  public void postQuery4() throws IOException {
    assertEquals("<html></html>",
        postQuery("", "<query xmlns=\"" + URI + "\">" +
        "<text><![CDATA[<html/>]]></text>" +
        "<parameter name='wrap' value='yes'/>" +
        "<parameter name='wrap' value='no'/>" +
        "<parameter name='omit-xml-declaration' value='no'/>" +
        "<parameter name='omit-xml-declaration' value='yes'/>" +
        "<parameter name='method' value='xhtml'/>" + "</query>"));
  }

  /**
   * POST Test: execute a query.
   * @throws IOException I/O exception
   */
  @Test
  public void postQuery5() throws IOException {
    assertEquals("123", postQuery("",
        "<query xmlns=\"" + URI + "\">" +
        "<text>123</text>" +
        "<parameter name='wrap' value='no'/>" +
        "<parameter name='omit-xml-declaration' value='no'/>" +
        "<parameter name='omit-xml-declaration' value='yes'/>" +
        "</query>"));
  }

  /** POST Test: execute buggy query. */
  @Test
  public void postQueryErr() {
    try {
      assertEquals("", postQuery("",
          "<query xmlns=\"" + URI + "\"><text>(</text></query>"));
    } catch(final IOException ex) {
      assertContains(ex.getMessage(), "[XPST0003]");
    }
  }

  /**
   * POST Test: create and add database.
   * @throws IOException I/O exception
   */
  @Test
  public void post1() throws IOException {
    put(DB, null);
    post(DB, stream("<a>A</a>"));
    assertEquals("1", get(DB + "?query=count(/)"));
    delete(DB);
  }

  /**
   * PUT Test: create empty database.
   * @throws IOException I/O exception
   */
  @Test
  public void put1() throws IOException {
    put(DB, null);
    assertEquals("0", get(DB + "?query=count(/)"));
    delete(DB);
  }

  /**
   * PUT Test: create simple database.
   * @throws IOException I/O exception
   */
  @Test
  public void put2() throws IOException {
    put(DB, stream("<a>A</a>"));
    assertEquals("A", get(DB + "?query=/*/text()"));
    delete(DB);
  }

  /**
   * PUT Test: create and overwrite database.
   * @throws IOException I/O exception
   */
  @Test
  public void put3() throws IOException {
    put(DB, new FileInputStream(FILE));
    put(DB, new FileInputStream(FILE));
    assertEquals("XML", get(DB + "?query=//title/text()"));
    delete(DB);
  }

  /**
   * PUT Test: create two documents in a database.
   * @throws IOException I/O exception
   */
  @Test
  public void put4() throws IOException {
    put(DB, null);
    put(DB + "/a", stream("<a>A</a>"));
    put(DB + "/b", stream("<b>B</b>"));
    assertEquals("2", get(DB + "?query=count(//text())"));
    assertEquals("2", get("?query=count(db:open('" + DB + "')//text())"));
    assertEquals("1", get("?query=count(db:open('" + DB + "/b')/*)"));
    delete(DB);
  }

  /**
   * DELETE Test.
   * @throws IOException I/O exception
   */
  @Test
  public void delete1() throws IOException {
    put(DB, new FileInputStream(FILE));
    // delete database
    assertEquals(delete(DB).trim(), Util.info(DBDROPPED, DB));
    try {
      // no database left
      delete(DB);
      fail("Error expected.");
    } catch(final FileNotFoundException ex) {
    }
  }

  /**
   * DELETE Test.
   * @throws IOException I/O exception
   */
  @Test
  public void delete2() throws IOException {
    put(DB, null);
    post(DB + "/a", stream("<a/>"));
    post(DB + "/a", stream("<a/>"));
    post(DB + "/b", stream("<b/>"));
    // delete 'a' directory
    assertContains(delete(DB + "/a"), "2 document");
    // delete 'b' directory
    assertContains(delete(DB + "/b"), "1 document");
    // no 'b' directory left
    assertContains(delete(DB + "/b"), "0 document");
    // delete database
    assertEquals(delete(DB).trim(), Util.info(DBDROPPED, DB));
    try {
      // no database left
      delete(DB);
      fail("Error expected.");
    } catch(final FileNotFoundException ex) {
    }
  }

  // PRIVATE METHODS ==========================================================

  /**
   * Checks if a string is contained in another string.
   * @param str string
   * @param sub sub string
   */
  private void assertContains(final String str, final String sub) {
    if(!str.contains(sub)) {
      fail("'" + sub + "' not contained in '" + str + "'.");
    }
  }

  /**
   * Executes the specified GET request.
   * @param query request
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  private String get(final String query) throws IOException {
    final URL url = new URL(ROOT + query);

    // create connection
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    try {
      return read(conn.getInputStream()).replaceAll("\r?\n *", "");
    } catch(final IOException ex) {
      throw new IOException(read(conn.getErrorStream()));
    } finally {
      conn.disconnect();
    }
  }

  /**
   * Executes the specified PUT request.
   * @param path path
   * @param query request
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  private String postQuery(final String path, final String query)
      throws IOException {
    final URL url = new URL(ROOT + path);

    // create connection
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("POST");
    conn.setRequestProperty(DataText.CONTENT_TYPE, DataText.APP_QUERYXML);
    // basic authentication example
    final String user = Text.ADMIN;
    final String pw = Text.ADMIN;
    final String userpw = user + ":" + pw;
    final String encoded = Base64.encode(userpw);
    conn.setRequestProperty(AUTHORIZATION, BASIC + ' ' + encoded);
    // send query
    final OutputStream out = conn.getOutputStream();
    out.write(token(query));
    out.close();

    try {
      return read(conn.getInputStream()).replaceAll("\r?\n *", "");
    } catch(final IOException ex) {
      throw new IOException(read(conn.getErrorStream()));
    } finally {
      conn.disconnect();
    }
  }

  /**
   * Executes the specified PUT request.
   * @param query request
   * @param is input stream
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  private String put(final String query, final InputStream is)
      throws IOException {

    final URL url = new URL(ROOT + query);
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("PUT");
    final OutputStream bos = new BufferedOutputStream(conn.getOutputStream());
    if(is != null) {
      // send input stream if it not empty
      final BufferedInputStream bis = new BufferedInputStream(is);
      for(int i; (i = bis.read()) != -1;) bos.write(i);
      bis.close();
      bos.close();
    }
    try {
      return read(conn.getInputStream());
    } catch(final IOException ex) {
      throw new IOException(read(conn.getErrorStream()));
    } finally {
      conn.disconnect();
    }
  }

  /**
   * Executes the specified PUT request.
   * @param query request
   * @param is input stream
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  private String post(final String query, final InputStream is)
      throws IOException {

    final URL url = new URL(ROOT + query);
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("POST");
    conn.setRequestProperty(DataText.CONTENT_TYPE, DataText.APP_XML);
    final OutputStream bos = new BufferedOutputStream(conn.getOutputStream());
    final BufferedInputStream bis = new BufferedInputStream(is);
    for(int i; (i = bis.read()) != -1;) bos.write(i);
    bis.close();
    bos.close();
    try {
      return read(conn.getInputStream());
    } catch(final IOException ex) {
      throw new IOException(read(conn.getErrorStream()));
    } finally {
      conn.disconnect();
    }
  }

  /**
   * Executes the specified DELETE request.
   * @param query request
   * @return response code
   * @throws IOException I/O exception
   */
  private String delete(final String query) throws IOException {
    final URL url = new URL(ROOT + query);
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    try {
      conn.setRequestMethod("DELETE");
      return read(conn.getInputStream());
    } finally {
      conn.disconnect();
    }
  }

  /**
   * Returns a string result from the specified input stream.
   * @param is input stream
   * @return string
   * @throws IOException I/O exception
   */
  private String read(final InputStream is) throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final BufferedInputStream bis = new BufferedInputStream(is);
    for(int i; (i = bis.read()) != -1;) baos.write(i);
    bis.close();
    return baos.toString();
  }

  /**
   * Creates a byte input stream for the specified string.
   * @param str string
   * @return stream
   */
  private InputStream stream(final String str) {
    return new ByteArrayInputStream(token(str));
  }
}
