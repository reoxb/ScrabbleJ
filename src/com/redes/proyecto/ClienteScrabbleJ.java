//clase que instancia a un cliente para el juego 
//permite al usuario interactuar con el tablero mientras espera mensajes del servidor
package com.redes.proyecto;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.Socket;
import java.net.InetAddress;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.util.Formatter;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class ClienteScrabbleJ extends JFrame implements Runnable {
	private JTextField campoId; // campo de texto para mostrar la marca del jugador
	   private JTextArea areaPantalla; // objeto JTextArea para mostrar la salida
	   private JPanel panelTablero; // panel para el tablero de scrabble
	   private JPanel panel2; // panel que contiene el tablero
	   private Cuadro tablero[][]; // tablero de scrabble
	   private Cuadro cuadroActual; // el cuadro actual
	   private Socket conexion; // conexión con el servidor
	   private Scanner entrada; // entrada del servidor
	   private Formatter salida; // salida al servidor
	   private String hostScrabble; // nombre de host para el servidor
	   private String miMarca; // la marca de este cliente
	   private boolean miTurno; // determina de qué cliente es el turno
	   private final String MARCA_X = "X"; // marca para el primer cliente
	   private final String MARCA_O = "O"; // marca para el segundo cliente

	   // establece la interfaz de usuario y el tablero
	   public ClienteScrabbleJ( String host )
	   { 
	      hostScrabble = host; // establece el nombre del servidor
	      areaPantalla = new JTextArea( 4, 30 ); // establece objeto JTextArea
	      areaPantalla.setEditable( false );
	      add( new JScrollPane( areaPantalla ), BorderLayout.SOUTH );

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
	        	//<-- Cambiar 3 por 15 para determinar la posicion de los cuadros
	            tablero[ fila ][ columna ] = new Cuadro( " ", fila * 15 + columna );
	            panelTablero.add( tablero[ fila ][ columna ] ); // agrega el cuadro       
	         } // fin de for interior
	      } // fin de for exterior

	      campoId = new JTextField(); // establece campo de texto
	      campoId.setEditable( false );
	      add( campoId, BorderLayout.NORTH );
	      
	      panel2 = new JPanel(); // establece el panel que contiene a panelTablero
	      panel2.add( panelTablero, BorderLayout.CENTER ); // agrega el panel del tablero
	      add( panel2, BorderLayout.CENTER ); // agrega el panel contenedor
	     

	      setSize( 475, 625 ); // establece el tamaño de la ventana
	      setVisible( true ); // muestra la ventana

	      iniciarCliente();
	   } // fin del constructor de ClienteScrabbleJ

	   // inicia el subproceso cliente
	   public void iniciarCliente()
	   {
	      try // se conecta al servidor, obtiene los flujos e inicia subproceso de salida
	      {
	         // realiza conexión con el servidor
	         conexion = new Socket( 
	            InetAddress.getByName( hostScrabble ), 12345 );

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
	      miMarca = entrada.nextLine(); // obtiene la marca del jugador (X o O)

	      SwingUtilities.invokeLater( 
	         new Runnable() 
	         {         
	            public void run()
	            {
	               // muestra la marca del jugador
	               campoId.setText( "Usted es el jugador \"" + miMarca + "\"" );
	            } // fin del método run
	         } // fin de la clase interna anónima
	      ); // fin de la llamada a SwingUtilities.invokeLater
	         
	      miTurno = ( miMarca.equals( MARCA_X ) ); // determina si es turno del cliente

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
	      if ( mensaje.equals( "Movimiento valido." ) ) 
	      {
	         mostrarMensaje( "Movimiento valido, por favor espere.\n" );
	         establecerMarca( cuadroActual, miMarca ); // establece marca en el cuadro
	      } // fin de if
	      else if ( mensaje.equals( "Movimiento invalido, intente de nuevo" ) ) 
	      {
	         mostrarMensaje( mensaje + "\n" ); // muestra el movimiento inválido
	         miTurno = true; // sigue siendo turno de este cliente
	      } // fin de else if
	      else if ( mensaje.equals( "El oponente realizo movimiento" ) ) 
	      {
	         int ubicacion = entrada.nextInt(); // obtiene la ubicación del movimiento
	         entrada.nextLine(); // salta nueva línea después de la ubicación int
	         int fila = ubicacion / 15; // calcula la fila
	         int columna = ubicacion % 15; // calcula la columna

	         establecerMarca(  tablero[ fila ][ columna ], 
	            ( miMarca.equals( MARCA_X ) ? MARCA_O : MARCA_X ) ); // marca el movimiento                
	         mostrarMensaje( "El oponente hizo un movimiento. \nAhora es su turno.\n" );
	         miTurno = true; // ahora es turno de este cliente
	      } // fin de else if
	      else
	         mostrarMensaje( mensaje + "\n" ); // muestra el mensaje
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
	   public void enviarCuadroClic( int ubicacion )
	   {
	      // si es mi turno
	      if ( miTurno ) 
	      {
	         salida.format( "%d\n", ubicacion ); // envía la ubicación al servidor
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
