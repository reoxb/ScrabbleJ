//clase que reliza la puntuacion del juego
package com.redes.scrabble;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ScrabbleJ {
	
	private List<puntuarLetra> cadenasPuntuadas;

    public ScrabbleJ() {
    	//crea un lista de puntos y palabras
        cadenasPuntuadas = Arrays.asList(
        //crea objetos del tipo puntuarLetra y llena la lista
                new puntuarLetra(0, "*"),
                new puntuarLetra(1, "AEIOULNRST"),
                new puntuarLetra(2, "DG"),
                new puntuarLetra(3, "BCMP"),
                new puntuarLetra(4, "FHVWY"),
                new puntuarLetra(5, "KÑ"),
                new puntuarLetra(8, "JX"),
                new puntuarLetra(10, "QZ")
        );
    }//constructor Scrabble

    //recibe una palabra y la convierte en un arreglo de cadenas
    public int obtenerPuntosPorPalabra(String palabra) {
        int puntuacionFinal = 0;
        for (char caracter : palabra.toCharArray()) {
        	//cadena por cadena es enviada para obtener su puntuacion
            puntuacionFinal += obtenerPuntosPorLetra(caracter);
        }//fin del for
        return puntuacionFinal;
    }//fin del metodo
    //cada caracter recibida es comparada en un set de letras para obtener sus puntos
    public int obtenerPuntosPorLetra(char caracter) {
        for (puntuarLetra cadenaDeLetras : cadenasPuntuadas) {
            if (cadenaDeLetras.contieneLetra(caracter)) {
                return cadenaDeLetras.obtenerPuntos();
            }//fin del if
        }//fin del for

        throw new IllegalArgumentException("'" + caracter + "' no es una caracter valido de ScrabbleJ");
    }//fin del metodo
    
    public String generarLetrasAleatorias(){
    	String S = "";
    	StringBuilder s = new StringBuilder(S);
    	char[] consonantes, vocales;
    	//caracteres = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'Ñ', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
    	consonantes = new char[]{'B', 'C', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'Ñ', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z' };
    	vocales = new char[]{'A', 'E', 'I', 'O', 'U'};
        int num_constantes = 7;
        int num_vocales = 3;
        
        /*generamos los caracteres aleatorios
         * nextInt(int n)  Devuelve un pseudoaleatorio de tipo int comprendido entre cero (incluido)
         * y el valor especificado (excluido). 
         */
    	for (int j = 0; j < num_constantes; j++) {
            //S += consonantes[new Random().nextInt(22)];
    		s.append(consonantes[new Random().nextInt(22)]);
            //System.out.println("Posible ultimo caracter de la cadena... " + caracteres[28]);
        }
    	for (int i = 0; i < num_vocales; i++) {
            s.append(vocales[new Random().nextInt(5)]);
            //System.out.println("Posible ultimo caracter de la cadena... " + caracteres[28]);
        }
    	
    	//System.out.println(s);
    	S = s.toString();
    	return S;
    }
    
    //-------------------------------------------------------------------------

    public static final class puntuarLetra {

        private int puntos;
        private List<Character> letras;
        //constructor 
        public puntuarLetra(int puntos, String letras) {
            this.puntos = puntos;
            this.letras = new ArrayList<Character>(letras.length());
            for (char caracter : letras.toCharArray()) {
                this.letras.add(caracter);
            }
        }//fin del constructor

        public int obtenerPuntos() {
        	//regresa los puntos que genera esa cadena de caracteres
            return puntos;
        }

        public boolean contieneLetra(Character caracter) {
            //regresa verdadero si contiene ese elemento en especifico
            return letras.contains(caracter);
        }
    }

}
