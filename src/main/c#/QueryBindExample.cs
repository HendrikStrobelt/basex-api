﻿/*
 * This example shows how queries can be executed in an iterative manner.
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * (C) BaseX Team 2005-11, BSD License
 */
using System;
using System.Diagnostics;
using System.IO;

namespace BaseXClient
{
  public class QueryIteratorExample
  {
    public static void Main(string[] args)
    {
      try
      {
        // create session
        Session session = new Session("localhost", 1984, "admin", "admin");

        try
        {
          // create query instance
          string input = "declare variable $name external;" +
          	"for $i in 1 to 10 return element { $name } { $i }";

          Query query = session.Query(input);
		  
		      // bind variable
		      query.Bind("$name", "number");
		  	
          // initialize query
          Console.WriteLine(query.Init());

          // loop through all results
          while (query.More()) 
          {
            Console.WriteLine(query.Next());
          }

          // close query
          Console.WriteLine(query.Close());
        }
        catch (IOException e)
        {
          // print exception
          Console.WriteLine(e.Message);
        }

        // close session
        session.Close();
      }
      catch (IOException e)
      {
        // print exception
        Console.WriteLine(e.Message);
      }
    }
  }
}
