<?php
/*
 * This example shows how new documents can be added.
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * (C) BaseX Team 2005-11, BSD License
 */
include("BaseXClient.php");

try {

  // create session
  $session = new Session("localhost", 1984, "admin", "admin");
  
  // create new database
  $session->execute("create db database");
  print $session->info();
  
  // add document
  $session->add("World.xml", "/world", "<x>Hello World!</x>");
  print "<br/>".$session->info();
  
  // add document
  $session->add("Universe.xml", "", "<x>Hello Universe!</x>");
  print "<br/>".$session->info();
  
  // run query on database
  print "<br/>".$session->execute("xquery /");
  
  // drop database
  $session->execute("drop db database");

  // close session
  $session->close();

} catch (Exception $e) {
  // print exception
  print $e->getMessage();
}
?>
