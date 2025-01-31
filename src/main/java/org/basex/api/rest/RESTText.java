package org.basex.api.rest;

import static org.basex.util.Token.*;
import org.basex.core.Text;

/**
 * This class assembles texts which are used in the HTTP classes.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public interface RESTText {
  /** REST string.  */
  byte[] REST = token("rest");
  /** REST URI. */
  byte[] RESTURI = concat(token(Text.URL), SLASH, REST);

  /** Element. */
  byte[] DATABASES = concat(REST, COLON, token("databases"));
  /** Element. */
  byte[] DATABASE = concat(REST, COLON, token("database"));
  /** Element. */
  byte[] RESOURCE = concat(REST, COLON, token("resource"));

  /** Token. */
  byte[] RESOURCES = token("resources");
  /** Token. */
  byte[] NAME = token("name");

  /** Error message. */
  String ERR_UNEXPECTED = "Unexpected error: ";
  /** Error message. */
  String ERR_PARAM = "Unknown parameter: ";
  /** Error message. */
  String ERR_NOPATH = "No path specified.";
  /** Error message. */
  String ERR_INVBODY = "Invalid query: %.";
  /** Error message. */
  String ERR_NOPARAM = "No parameters supported.";
  /** Error message. */
  String ERR_ONLYONE = "Only one operation can be specified.";

  /** Wrap parameter. */
  String WRAP = "wrap";
  /** Command parameter. */
  String COMMAND = "command";
  /** Run parameter. */
  String RUN = "run";
  /** Query parameter. */
  String QUERY = "query";
}
