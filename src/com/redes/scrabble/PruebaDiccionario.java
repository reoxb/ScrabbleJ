package com.redes.scrabble;

public class PruebaDiccionario {

	public static void main( String args[] )
	{
		Diccionario miDiccionario = new Diccionario();
		String palabra = "HOLA";
		if (miDiccionario.existePalabra(palabra)) {
			System.out.println("La palabra " + palabra + " existe en el diccionario.");
		}//fin del if
	}//fin del main
}//fin de la clase