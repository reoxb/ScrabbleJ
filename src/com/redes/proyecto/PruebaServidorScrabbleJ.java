package com.redes.proyecto;

import javax.swing.JFrame;

public class PruebaServidorScrabbleJ {

	 public static void main( String args[] )
	   {
	      ServidorScrabbleJ aplicacion = new ServidorScrabbleJ();
	      aplicacion.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	      aplicacion.execute();
	   } // fin de main
}
