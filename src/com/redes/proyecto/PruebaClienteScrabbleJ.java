package com.redes.proyecto;

import javax.swing.JFrame;

public class PruebaClienteScrabbleJ {
	public static void main( String args[] )
	   {
	      ClienteScrabbleJ aplicacion; // declara la aplicación cliente

	      // si no hay argumentos de línea de comandos
	      if ( args.length == 0 )
	         aplicacion = new ClienteScrabbleJ( "127.0.0.1" ); // localhost
	      else
	         aplicacion = new ClienteScrabbleJ( args[ 0 ] ); // usa args

	      aplicacion.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	   } // fin de main
}
