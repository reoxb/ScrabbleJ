package com.redes.scrabble;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

public class Diccionario{
	
		private ArrayList<String> listaDePalabras;

		//constructor del diccionario
		public Diccionario() {
			listaDePalabras = new ArrayList<String>();
			try{
				BufferedReader txtDic = new BufferedReader(new FileReader("assets/diccionario.txt"));
				while(txtDic.ready()){
					String l = txtDic.readLine();
					listaDePalabras.add(l.toString());
				}
				txtDic.close();
			}catch(FileNotFoundException e) {
				System.out.println("No se encontro el archivo " + e);
			}catch(IOException ioe) {
				JOptionPane.showMessageDialog(null,"No se pudo leer el archivo");
			}
		}//fin del constructor

		public ArrayList<String> getDiccionario(){
			return listaDePalabras;
		}//fin del metodo
		
		//crea un arreglo de palabras que coinciden con el string generado
		public String[] filtrarDiccionario(String s){
			String[] m = new String[listaDePalabras.size()];
			int count =0;
			for(int i=0; i<listaDePalabras.size(); i++){
				String n = listaDePalabras.get(i);
				if(n.startsWith(s)){
					m[i] = n;
					count++;
				}
			}
			String[] r = new String[count];
			for(int i=0; i<count; i++){
				r[i]=m[i];
			}
			return r; 
		}//fin del metodo

		public boolean existePalabra(String palabra){
			for(int i=0; i<listaDePalabras.size(); i++){
				String s = listaDePalabras.get(i);
				if(palabra.compareTo(s)==0){
					return true;
				}
			}
			return false;
		}//fin del metodo 
}//fin de la clase Diccionario
