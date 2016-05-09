package com.redes.scrabble;

public class PruebaScrabbleJ {
	
	public static void main( String args[] )
	   {
	      ScrabbleJ aplicacion = new ScrabbleJ(); // declara la aplicación cliente
	      //genera diferentes cadenas de letras
	      for (int i = 0; i < 10; i++) {
	    	  String word = aplicacion.generarLetrasAleatorias();
		      System.out.println(word);
		      word.toUpperCase();
		      int score = aplicacion.obtenerPuntosPorPalabra(word);
		      System.out.println(score);
	      }//fin del for
	   } // fin de main
}//fin de la clase ScrabbleJ
