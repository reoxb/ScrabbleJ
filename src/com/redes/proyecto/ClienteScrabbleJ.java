//clase que instancia a un cliente para el juego 
//permite al usuario interactuar con el tablero mientras espera mensajes del servidor
package com.redes.proyecto;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.Socket;
import java.net.InetAddress;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JRadioButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;

import com.redes.scrabble.Diccionario;
import com.redes.scrabble.ScrabbleJ;

import java.util.Formatter;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class ClienteScrabbleJ extends JFrame implements Runnable {
	   private JLabel etiquetaPalabra; // campo de texto para mostrar los puntos del jugador
	   private JTextField campoId; // campo de texto para mostrar la marca del jugador
	   private JTextField campoScore; // campo de texto para mostrar los puntos del jugador
	   private JTextField campoPalabra; // introduce la palabra del usuario
       private JLabel etiquetaCadenaAleatoria;
	   private JTextArea areaPantalla; // objeto JTextArea para mostrar la salida
	   private JRadioButton horizontalBoton; // boton que determina la posicion horizontal
	   private JRadioButton verticalBoton; //boton que determina la posicion vertical
	   private ButtonGroup grupoOpciones;
	   private JPanel panelTablero; // panel para el tablero de scrabble
	   private JPanel panel2; // panel que contiene el tablero
	   private Cuadro tablero[][]; // tablero de scrabble
	   private Cuadro cuadroActual; // el cuadro actual
	   private Socket conexion; // conexión con el servidor
	   private Scanner entrada; // entrada del servidor
	   private Formatter salida; // salida al servidor
	   private String hostScrabble; // nombre de host para el servidor
	   private String miMarcaCliente; // la marca de este cliente
	   private String CADENA; //almacena la cadena generada por scrabble para Dos
	   private String palabraFinal;
	   private String palabra; //palabra formada por el cliente
	   private ScrabbleJ scrabble; //crea el juego scrabble
	   private Diccionario diccionario;
	   private boolean miTurno; // determina de qué cliente es el turno
	   private boolean playGame;
	   private final String MARCA_1 = "Uno"; // marca para el primer cliente
	   private final String MARCA_2 = "Dos"; // marca para el segundo cliente
	   private int score;
	   private char orientacion;
	   private int ubicacion;
	  
	   // establece la interfaz de usuario y el tablero
	   public ClienteScrabbleJ( String host )
	   { 
	      hostScrabble = host; // establece el nombre del servidor
	      scrabble = new ScrabbleJ(); //crea el objeto scrabble
	      diccionario = new Diccionario(); //creamos un objeto diccionario
	      playGame = false;
	      palabraFinal = "";
	      CADENA = "";
	      score = 0;

		  //crea un panel principal para determinar las posiciones
		  Container panelPrincipal = getContentPane();
		  panelPrincipal.setLayout(new BorderLayout());
		  
		  //crea una cajas para meter campos en las posiciones
		  Box cajaSuperior = Box.createVerticalBox();
		  Box cajaInferior = Box.createVerticalBox();
		  Box boxEtiquetaCampo = Box.createHorizontalBox();
		  Box radioBotones = Box.createHorizontalBox();

		  panelPrincipal.add(cajaSuperior, BorderLayout.NORTH );
		  panelPrincipal.add(cajaInferior, BorderLayout.SOUTH );

	      campoId = new JTextField(); // establece campo de texto
	      campoScore = new JTextField();
	      campoId.setEditable( false );
	      campoScore.setEditable( false );
          campoScore.setText("Score: " + String.valueOf(score));
         
	      //campo que muestra la informacion del usuario
	      cajaSuperior.add(campoId);
	      //campo que muestra la puntacion del usuario
	      cajaSuperior.add(campoScore);

	      /*Tablero*/
	      panelTablero = new JPanel(); // establece panel para los cuadros en el tablero
	      panelTablero.setLayout( new GridLayout( 15, 15, 0, 0 ) );
	      
	      //filas[]columnas[]
	      tablero = new Cuadro[ 15 ][ 15 ]; // crea el tablero

	      // itera a través de las filas en el tablero
	      for ( int fila = 0; fila < tablero.length; fila++ ) 
	      {
	         // itera a través de las columnas en el tablero
	         for ( int columna = 0; columna < tablero[ fila ].length; columna++ ) 
	         {
	            // crea un cuadro
	        	//<-- Crear un aleatorio para marcar el tablero Cuadro("-" , #)
	            tablero[ fila ][ columna ] = new Cuadro( " ", fila * 15 + columna );
	            panelTablero.add( tablero[ fila ][ columna ] ); // agrega el cuadro       
	         } // fin de for interior
	      } // fin de for exterior
	      
	      panel2 = new JPanel(); // establece el panel que contiene a panelTablero
	      panel2.add( panelTablero, BorderLayout.CENTER ); // agrega el panel del tablero
	      panelPrincipal.add( panel2, BorderLayout.CENTER ); // agrega el panel contenedor
	      
	      //etiqueta que muestra la cadena aleatoria creada por el usuario
	      etiquetaCadenaAleatoria = new JLabel();
	      etiquetaCadenaAleatoria.setText("\"Bienvenido!\"");
	      //le da un tamanio al la fuente para mostrarla mas marcada
	      etiquetaCadenaAleatoria.setFont (etiquetaCadenaAleatoria.getFont().deriveFont(26.0f));
	      cajaInferior.add(etiquetaCadenaAleatoria);

	      etiquetaPalabra = new JLabel();
	      etiquetaPalabra.setText("Introduce tu palabra: ");
	      boxEtiquetaCampo.add(etiquetaPalabra);
	      
      
	      //obtiene la palabra formada por el usuario
	      campoPalabra = new JTextField(); // crea objeto campoPalabra
	      campoPalabra.setEnabled(false);
	      
	      campoPalabra.addActionListener(
	         new ActionListener() 
	         {
	            // envia el mensaje al servidor
	            public void actionPerformed( ActionEvent evento )
	            {
	            	//recibe la entrada del cliente
	            		palabra = evento.getActionCommand();	
	            		//genera el juego
	            		jugarScrabble(palabra);
	            } // fin del método actionPerformed
	         } // fin de la clase interna anónima
	      ); // fin de la llamada a addActionListener
	      
	      boxEtiquetaCampo.add( campoPalabra );
	      cajaInferior.add(boxEtiquetaCampo);
	      
	      horizontalBoton = new JRadioButton("horizontal", true);
	      verticalBoton = new JRadioButton("vertical", false);
	      grupoOpciones = new ButtonGroup();
	      grupoOpciones.add(horizontalBoton);
	      grupoOpciones.add(verticalBoton);
	      radioBotones.add(horizontalBoton);
	      radioBotones.add(verticalBoton);
	      cajaInferior.add(radioBotones);
	           
	      areaPantalla = new JTextArea( 4, 40 ); // establece objeto JTextArea
	      areaPantalla.setEditable( false );
	      cajaInferior.add( new JScrollPane( areaPantalla ));
	      

	      setSize( 478, 665 ); // establece el tamaño de la ventana
	      setVisible( true ); // muestra la ventana

	      iniciarCliente();
	   } // fin del constructor de ClienteScrabbleJ

	   //recibe la entrada del usuario y realiza el juego
	   protected void jugarScrabble(String entrada) {
		   int puntos;
		  
		if(scrabble.validarCadenaYPalabra(CADENA, entrada))
   		{
			if(diccionario.existePalabra(entrada)){
				puntos = scrabble.obtenerPuntosPorPalabra(entrada);	
	   			mostrarMensaje("La palabra " + "\"" + entrada + "\"" + " es valida. " +
	   					"Usted obtuvo: " + puntos + " puntos.\n");
	   			palabraFinal = entrada;
	   			score = score + puntos;
				//System.out.println("Valor del score: " +  score);
				playGame = true;
				campoPalabra.setEnabled(false);
				mostrarMensaje("Por favor indique orientacion y marque un cuadro " +
						"para colocar su palabra!. \n");
				
			}else {
	   			mostrarMensaje("Esta palabra no existe en el diccionario. \n");
			}
   		}else{
   			mostrarMensaje("La cadena no coincide con la palabra \n");
   		}
		
        campoScore.setText("Score: " + String.valueOf(score));
   		campoPalabra.setText( "" );		
	}

	// inicia el subproceso cliente
	   public void iniciarCliente()
	   {
	      try // se conecta al servidor, obtiene los flujos e inicia subproceso de salida
	      {
	         // realiza conexión con el servidor
	         conexion = new Socket( 
	            InetAddress.getByName( hostScrabble ), 12345 );
	         //determina la direccion IP de un host, proporcionando el nombre.

	         // obtiene flujos para entrada y salida
	         entrada = new Scanner( conexion.getInputStream() );
	         salida = new Formatter( conexion.getOutputStream() );
	      } // fin de try
	      catch ( IOException excepcionES )
	      {
	         excepcionES.printStackTrace();         
	      } // fin de catch

	      // crea e inicia subproceso trabajador para este cliente
	      ExecutorService trabajador = Executors.newFixedThreadPool( 1 );
	      trabajador.execute( this ); // ejecuta el cliente
	   } // fin del método iniciarCliente

	   // subproceso de control que permite la actualización continua de areaPantalla
	   public void run()
	   {
	      miMarcaCliente = entrada.nextLine(); // obtiene la marca del jugador (Uno o Dos)
     
	      SwingUtilities.invokeLater( 
	         new Runnable() 
	         {         
	            public void run()
	            {
	               // muestra la marca del jugador
	               campoId.setText( "Usted es el jugador \"" + miMarcaCliente + "\"" );
	            } // fin del método run
	         } // fin de la clase interna anónima
	      ); // fin de la llamada a SwingUtilities.invokeLater
	      miTurno = ( miMarcaCliente.equals( MARCA_1 ) ); // determina si es turno del cliente
	      
	      if(!miTurno){
	      }
	      // recibe los mensajes que se envían al cliente y los imprime en pantalla
	      while ( true )
	      {
	         if ( entrada.hasNextLine() )
	            procesarMensaje( entrada.nextLine() ); 
	      } // fin de while
	      
	   } // fin del método run

	   // procesa los mensajes recibidos por el cliente
	   private void procesarMensaje( String mensaje )
	   {
	      // ocurrió un movimiento válido
		  if (mensaje.equals("PLAY")){
			  campoPalabra.setEnabled(true);
	    	  CADENA = scrabble.generarLetrasAleatorias();
	    	  etiquetaCadenaAleatoria.setText(CADENA);
		  }
		  else if ( mensaje.equals( "Movimiento valido." ) ) 
	      {
	         mostrarMensaje( "Movimiento valido, por favor espere.\n" );
	         generaMarcas(ubicacion, palabraFinal, orientacion ); // marca el movimiento  
	         

	      } // fin de if
	      else if ( mensaje.equals( "Movimiento invalido, intente de nuevo" ) ) 
	      {	 
	         mostrarMensaje( mensaje + "\n" ); // muestra el movimiento inválido
	         miTurno = true; // sigue siendo turno de este cliente
	      } // fin de else if
	      else if ( mensaje.equals( "El oponente realizo movimiento" ) ) 
	      {
		     mostrarMensaje( "El oponente hizo un movimiento. \nAhora es su turno.\n" );
	         String in = entrada.nextLine(); // obtiene la ubicación del movimiento
	         StringTokenizer separador = new StringTokenizer(in, "-"); //indica como recibir el mensaje
          	 int ubicacion = Integer.parseInt(separador.nextToken()); //obtiene la ubicacion
          	 String getPalabra = separador.nextToken(); //obtiene la palabra
          	 String getOrientacion = separador.nextToken(); //obtiene la orientacion
          	 char orientacion = getOrientacion.charAt(0);
	         campoPalabra.setEnabled(true); // permite que el campo este habilitado         
	         //entrada.nextLine(); // salta nueva línea después de la ubicación int
	         //mostrarMensaje("Ubicacion: " + ubicacion + "Palabra: " + getPalabra + "Orientacion: " + getOrientacion + "\n");
	         generaMarcas(ubicacion, getPalabra, orientacion ); // marca el movimiento                
	         miTurno = true; // ahora es turno de este cliente
	         playGame = false;
	    	 CADENA = scrabble.generarLetrasAleatorias();
	    	 etiquetaCadenaAleatoria.setText(CADENA);
	      } // fin de else if
	      else {
	    	  mostrarMensaje( mensaje + "\n" ); // muestra el mensaje
	      }
	   } // fin del método procesarMensaje

	   // manipula el objeto areaSalida en el subproceso despachador de eventos
	   private void mostrarMensaje( final String mensajeAMostrar )
	   {
	      SwingUtilities.invokeLater(
	         new Runnable() 
	         {
	            public void run() 
	            {
	               areaPantalla.append( mensajeAMostrar ); // actualiza la salida
	            } // fin del método run
	         }  // fin de la clase interna
	      ); // fin de la llamada a SwingUtilities.invokeLater
	   } // fin del método mostrarMensaje

	   private void generaMarcas(int ubicacion, String palabra, char orientacion){
		   
		   int fila = ubicacion / 15; // calcula la fila
		      int columna = ubicacion % 15; // calcula la columna
			  char marca;
			  
			  if(orientacion == 'H'){
				  // cicla por las columnas para comparar la casilla
				  for (int j = 0; j < palabra.length(); j++) {
					  // en cada casilla compara la palabra completa
					 marca = palabra.charAt(j);  
					 establecerMarca( tablero[ fila ][columna + j],  String.valueOf(marca));
				  }//fin del for
			  }else{ //si la orientacion es vertical
				  for (int i = 0; i < palabra.length(); i++) {
					  // en cada casilla compara la palabra completa
					 marca = palabra.charAt(i);  
					 establecerMarca(tablero[ fila + i][columna],  String.valueOf(marca)); 
				  }//fin del for
			  }//fin del else vertical
	   }
	   
	   // método utilitario para establecer una marca en el tablero, en el subproceso despachador de eventos
	   private void establecerMarca( final Cuadro cuadroAMarcar, final String marca )
	   {
	      SwingUtilities.invokeLater(
	         new Runnable() 
	         {
	            public void run()
	            {
	               cuadroAMarcar.establecerMarca( marca ); // establece la marca en el cuadro
	            } // fin del método run
	         } // fin de la clase interna anónima
	      ); // fin de la llamada a SwingUtilities.invokeLater
	   } // fin del método establecerMarca

	   // envía un mensaje al servidor, indicando el cuadro en el que se hizo clic
	   public void enviarCuadroClic( int ubicacionCuadro )
	   {
		   ubicacion = ubicacionCuadro;
	      // si es mi turno
		      if ( playGame && miTurno ) 
		      {
		    	 if(horizontalBoton.isSelected())
		    		 orientacion = 'H';
		    	 else 
		    		 orientacion = 'V';
		    	 
		         salida.format( "%d-%s-%c\n", ubicacion, palabraFinal, orientacion ); // envía la ubicación al servidor
		         salida.flush();

		         miTurno = false; // ya no es mi turno
		      } // fin de if
	   } // fin del método enviarCuadroClic

	   // establece el cuadro actual
	   public void establecerCuadroActual( Cuadro cuadro )
	   {
	      cuadroActual = cuadro; // asigna el argumento al cuadro actual
	   } // fin del método establecerCuadroActual

	   // clase interna privada para los cuadros en el tablero
	   private class Cuadro extends JPanel 
	   {
	      private String marca; // marca a dibujar en este cuadro
	      private int ubicacion; // ubicacion del cuadro
	   
	      public Cuadro( String marcaCuadro, int ubicacionCuadro )
	      {
	         marca = marcaCuadro; // establece la marca para este cuadro
	         ubicacion = ubicacionCuadro; // establece la ubicación de este cuadro

	         addMouseListener( 
	            new MouseAdapter() 
		    {
	               public void mouseReleased( MouseEvent e )
	               {
	                  establecerCuadroActual( Cuadro.this ); // establece el cuadro actual

	                  // envía la ubicación de este cuadro
	                  enviarCuadroClic( obtenerUbicacionCuadro() );
	               } // fin del método mouseReleased
	            } // fin de la clase interna anónima
	         ); // fin de la llamada a addMouseListener
	      } // fin del constructor de Cuadro

	      // devuelve el tamaño preferido del objeto Cuadro
	      public Dimension getPreferredSize() 
	      { 
	         return new Dimension( 30, 30 ); // devuelve el tamaño preferido
	      } // fin del método getPreferredSize

	      // devuelve el tamaño mínimo del objeto Cuadro
	      public Dimension getMinimumSize() 
	      {
	         return getPreferredSize(); // devuelve el tamaño preferido
	      } // fin del método getMinimumSize

	      // establece la marca para el objeto Cuadro
	      public void establecerMarca( String nuevaMarca ) 
	      { 
	         marca = nuevaMarca; // establece la marca del cuadro
	         repaint(); // vuelve a pintar el cuadro
	      } // fin del método establecerMarca
	   
	      // devuelve la ubicación del objeto Cuadro
	      public int obtenerUbicacionCuadro() 
	      {
	         return ubicacion; // devuelve la ubicación del cuadro
	      } // fin del método obtenerUbicacionCuadro
	   
	      // dibuja el objeto Cuadro
	      public void paintComponent( Graphics g )
	      {
	         super.paintComponent( g );

	         g.drawRect( 0, 0, 29, 29 ); // dibuja el cuadro
	         g.drawString( marca, 11, 20 ); // dibuja la marca   
	      } // fin del método paintComponent
	   } // fin de la clase interna Cuadro
}//fin de la clase
