package com.redes.scrabble;

public class PruebaDiccionario {

	public static void main( String args[] )
	{
		Diccionario miDiccionario = new Diccionario();
		String palabra = "NEURONAS";
		palabra = palabra.toUpperCase();
		if (miDiccionario.existePalabra(palabra)) {
			System.out.println("La palabra " + palabra + " existe en el diccionario.");
		}else {
			System.out.println("La palabra " + palabra + " no existe en el diccionario.");
		}
			
	}//fin del main
}//fin de la clase