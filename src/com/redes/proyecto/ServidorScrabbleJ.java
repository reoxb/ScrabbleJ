//Clase que conecta dos clientes
package com.redes.proyecto;
import java.awt.BorderLayout;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.Formatter;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class ServidorScrabbleJ extends JFrame{

	   private String[][] tablero = new String[15][15]; // tablero de scrabble
	   private JTextArea areaSalida; // para imprimir los movimientos en pantalla
	   private Jugador[] jugadores; // arreglo de objetos Jugador
	   private ServerSocket servidor; // socket servidor para conectarse con los clientes
	   private int jugadorActual; // lleva la cuenta del jugador que sigue en turno
	   private final static int JUGADOR_1 = 0; // constante para el primer jugador
	   private final static int JUGADOR_2 = 1; // constante para el segundo jugador
	   private final static String[] MARCAS = { "Uno", "Dos" }; // arreglo de marcas
	   private ExecutorService ejecutarJuego; // ejecuta a los jugadores
	   private Lock bloqueoJuego; // para bloquear el juego y estar sincronizado
	   private Condition otroJugadorConectado; // para esperar al otro jugador
	   private Condition turnoOtroJugador; // para esperar el turno del otro jugador

	   // cosntructor establece servidor y GUI para mostrar mensajes en pantalla
	   public ServidorScrabbleJ()
	   {
	      super( "ServidorScrabbleJ" ); // establece el título de la ventana

	      // crea objeto ExecutorService con un subproceso para cada jugador
	      ejecutarJuego = Executors.newFixedThreadPool(2);
	      bloqueoJuego = new ReentrantLock(); // crea bloqueo para el juego

	      // variable de condición para los dos jugadores conectados
	      otroJugadorConectado = bloqueoJuego.newCondition();

	      // variable de condición para el turno del otro jugador
	      turnoOtroJugador = bloqueoJuego.newCondition();      

	      for ( int i = 0; i < 15; i++ )
	    	  for (int j = 0; j < 15; j++) {
	 	         tablero[ i ][ j ] = new String( "" ); // crea tablero de scrabble
			}
	      jugadores = new Jugador[2]; // crea arreglo de jugadores
	      jugadorActual = JUGADOR_1; // establece el primer jugador como el jugador actual
	 
	      try
	      {
	    	 //establece el puerto y el numero maximo de conexiones permitidas
	         servidor = new ServerSocket(12345, 2); // establece objeto ServerSocket
	      } // fin de try
	      catch ( IOException excepcionES ) 
	      {
	         excepcionES.printStackTrace();
	         System.exit(1);
	      } // fin de catch

	      areaSalida = new JTextArea(); // crea objeto JTextArea para mostrar la salida
	      add( new JScrollPane(areaSalida), BorderLayout.CENTER );
	      areaSalida.setText( "Servidor esperando conexiones\n" );

	      setSize( 300, 325 ); // establece el tamaño de la ventana
	      setVisible( true ); // muestra la ventana
	   } // fin del constructor de ServidorScrabbleJ

	   // espera dos conexiones para poder jugar
	   public void execute()
	   {
	      // espera a que se conecte cada cliente
	      for ( int i = 0; i < jugadores.length; i++ ) //<-- dos por defecto o de 0 a 1
	      {
	         try // espera la conexión, crea el objeto Jugador, inicia objeto Runnable
	         {
	        	//el metodo accept() es bloqueante hasta recibir una conexion
	            jugadores[i] = new Jugador( servidor.accept(), i );
	            //el constructor de jugador recibe un objeto socket crea los flujos E/S
	            ejecutarJuego.execute(jugadores[i]); // ejecuta el objeto Runnable subproceso
	         } // fin de try
	         catch ( IOException excepcionES ) 
	         {
	            excepcionES.printStackTrace();
	            System.exit(1);
	         } // fin de catch
	      } // fin de for

	      bloqueoJuego.lock(); // bloquea el juego para avisar al subproceso del jugador Uno

	      try
	      {
	         jugadores[JUGADOR_1].establecerSuspendido(false); // continúa el jugador Uno
	         otroJugadorConectado.signal(); // despierta el subproceso del jugador Uno
	      } // fin de try
	      finally
	      {
	         bloqueoJuego.unlock(); // desbloquea el juego después de avisar al jugador Uno
	      } // fin de finally
	   } // fin del método execute
	   
	   // muestra un mensaje en objeto areaSalida
	   private void mostrarMensaje( final String mensajeAMostrar )
	   {
	      // muestra un mensaje del subproceso de ejecución despachador de eventos
	      SwingUtilities.invokeLater(
	         new Runnable() 
	         {
	            public void run() // actualiza el objeto areaSalida
	            {
	               areaSalida.append( mensajeAMostrar ); // agrega el mensaje
	            } // fin del método run
	         } // fin de la clase interna
	      ); // fin de la llamada a SwingUtilities.invokeLater
	   } // fin del método mostrarMensaje

	   // determina si el movimiento es válido
	   public boolean validarYMover( int ubicacion, String palabra, char orientacion, int jugador )
	   {
	      // mientras no sea el jugador actual, debe esperar su turno
	      while ( jugador != jugadorActual ) 
	      {
	         bloqueoJuego.lock(); // bloquea el juego para esperar a que el otro jugador haga su movmiento

	         try 
	         {
	            turnoOtroJugador.await(); // espera el turno de jugador
	         } // fin de try
	         catch ( InterruptedException excepcion )
	         {
	            excepcion.printStackTrace();
	         } // fin de catch
	         finally
	         {
	            bloqueoJuego.unlock(); // desbloquea el juego después de esperar
	         } // fin de finally
	      } // fin de while

	      // si la ubicación no está ocupada, realiza el movimiento
	      if ( !estaOcupada( ubicacion, palabra, orientacion ) )
	      {
	    	 /* El servidor crea su propio mapa del tablero, con esto puede saber las posiciones que hay disponibles
	    	  * y si es que alguna de ellas, ha sido ocupada, es avisado de los movientos y los mapea, para darle 
	    	  * seguimiento al juego. 
	    	  */
	    	  
	    	  int fila = ubicacion / 15; // calcula la fila
		      int columna = ubicacion % 15; // calcula la columna
			  char marca;
			  
			  if(orientacion == 'H'){
				  // cicla por las columnas para comparar la casilla
				  for (int j = 0; j < palabra.length(); j++) {
					  // en cada casilla compara la palabra completa
					 marca = palabra.charAt(j);  
					 tablero[ fila ][columna + j] = String.valueOf(marca);
				  }//fin del for
			  }else{ //si la orientacion es vertical
				  for (int i = 0; i < palabra.length(); i++) {
					  // en cada casilla compara la palabra completa
					 marca = palabra.charAt(i);  
					 tablero[ fila + i][columna] = String.valueOf(marca); 
				  }//fin del for
			  }//fin del else vertical
	         
	         jugadorActual = ( jugadorActual + 1 ) % 2; // cambia el jugador

	         // deja que el nuevo jugador sepa que se realizó un movimiento
	         jugadores[ jugadorActual ].otroJugadorMovio( ubicacion, palabra, orientacion );

	         bloqueoJuego.lock(); // bloquea el juego para indicar al otro jugador que realice su movimiento

	         try 
	         {
	            turnoOtroJugador.signal(); // indica al otro jugador que debe continuar
	         } // fin de try
	         finally
	         {
	            bloqueoJuego.unlock(); // desbloquea el juego despues de avisar
	         } // fin de finally

	         return true; // notifica al jugador que el movimiento fue válido
	      } // fin de if
	      else // el movimiento no fue válido
	         return false; // notifica al jugador que el movimiento fue inválido
	   } // fin del método validarYMover

	   // determina si la ubicación está ocupada
	   public boolean estaOcupada( int ubicacion, String palabra, char orientacion )
	   {
		  int fila = ubicacion / 15; // calcula la fila
	      int columna = ubicacion % 15; // calcula la columna
		  char marca;
		  //valida que las palabras esten dentro del cuadro
		  if(((columna + palabra.length()) > 15) || ((fila + palabra.length()) > 15))
			  return true;
		  
		  if(orientacion == 'H'){
			  // cicla por las columnas para comparar la casilla
			  for (int j = 0; j < palabra.length(); j++) {
				  // en cada casilla compara la palabra completa
				 marca = palabra.charAt(j);  
				  if ( (tablero[ fila ][columna + j].equals("")) || (tablero[fila][columna + j].equals(marca))){
				    	  continue;
				      } else { //si la casilla esta ocupada
					         return true; // la ubicación está ocupada
				      }//fin del if else
			}//fin del for horizontal para columnas
		  }else{ //si la orientacion es vertical
			  
			  for (int i = 0; i < palabra.length(); i++) {
				  // en cada casilla compara la palabra completa
				 marca = palabra.charAt(i);  
				  if ( (tablero[ fila + i][columna].equals("")) || (tablero[fila][columna + i].equals(marca))){
				    	  continue;
				      } else { //si la casilla esta ocupada
					         return true; // la ubicación está ocupada
				      }//fin del if else
			}//fin del for horizontal para columnas  
		  }//fin del else vertical
		  return false; // la ubicación no está ocuapada

	   } // fin del método estaOcupada

	   // coloca código en este método para determinar si terminó el juego 
	   public boolean seTerminoJuego()
	   {
	      return false; // esto se deja como ejercicio
	   } // fin del método seTerminoJuego

	   // la clase interna privada Jugador maneja a cada Jugador como objeto Runnable
	   private class Jugador implements Runnable 
	   {
	      private Socket conexion; // conexión con el cliente
	      private Scanner entrada; // entrada del cliente
	      private Formatter salida; // salida al cliente
	      private int numeroJugador; // rastrea cuál jugador es el actual
	      private boolean suspendido = true; // indica si el subproceso está suspendido
	      private String palabra; //recibe la palabra dada por el cliente
	      private String marca; // marca para este jugador
	      private char orientacion; //establece la orientacion de la palabra
	      private String in;
	      private StringTokenizer separador; //captura la linea con formato enviada desde el cliente

	      // establece el subproceso Jugador
	      public Jugador( Socket socket, int numero )
	      {
	         numeroJugador = numero; // almacena el número de este jugador
	         marca = MARCAS[ numeroJugador ]; // especifica la marca del jugador
	         conexion = socket; // almacena socket para el cliente
	         
	         try // obtiene los flujos del objeto Socket
	         {
	            entrada = new Scanner( conexion.getInputStream() );
	            salida = new Formatter( conexion.getOutputStream() );
	         } // fin de try
	         catch ( IOException excepcionES ) 
	         {
	            excepcionES.printStackTrace();
	            System.exit( 1 );
	         } // fin de catch
	      } // fin del constructor de Jugador

	      // envía mensaje que indica que el otro jugador hizo un movimiento
	      public void otroJugadorMovio(int ubicacion, String palabra, char orientacion )
	      {
	         salida.format( "El oponente realizo movimiento\n");
	         salida.format( "%d-%s-%c\n", ubicacion, palabra, orientacion ); // envía la ubicación, palabra y orientacion del movimiento
	         salida.flush(); // vacía la salida
	      } // fin del método otroJugadorMovio

	      // controla la informacion que se envia al cliente y la que se recibe
	      public void run()
	      {
	         // envía al cliente su marca (Uno o Dos), procesa los mensajes del cliente
	         try 
	         {
	            mostrarMensaje( "Jugador " + marca + " conectado\n" );
	            /* pasa al cliente el caracter que se colocara en el tablero cuando se
	             * haga un moviento anterior O o Uno 
	             * <----- recibir solo la marca en el cliente, modificar en el cliente mostrar marca 
	             */
	            salida.format("%s\n", marca); // envía la marca del jugador
	            salida.flush(); // vacía la salida y la forza

	            // si es el jugador Uno
	            if ( numeroJugador == JUGADOR_1 ) 
	            {
	               salida.format( "%s\n%s", "Jugador Uno conectado",
	                  "Esperando al otro jugador\n" );
	               salida.flush(); // vacía la salida

	               bloqueoJuego.lock(); // bloquea el juego para esperar al segundo jugador

	               try 
	               {
	                  while( suspendido )
	                  {
		                 //await causa que el hilo actual espere hasta una senial o interrupcion.
	                     otroJugadorConectado.await(); // espera al jugador Dos
	                     /* suspende el subproceso del jugador Uno cuando empieza a ejecutarse, 
	                      * ya que el jugador Uno podrá realizar movimientos sólo hasta después 
	                      * de que el jugador Dos se conecte */
	                  } // fin de while
	               } // fin de try 
	               catch ( InterruptedException excepcion ) 
	               {
	                  excepcion.printStackTrace();
	               } // fin de catch
	               finally
	               {
	                  bloqueoJuego.unlock(); // desbloquea el juego después del segundo jugador
	               } // fin de finally
	               
	               // envía un mensaje que indica que el otro jugador se conectó
	               salida.format( "El otro jugador se conecto.\n" );
	               salida.format( "Ahora es su turno.\n" );
	               //<-- buscar una explicacion en este punto es crucial
	               salida.format( "PLAY\n" );
	               //es necesario el salto de linea para que se pueda leer y procesar el mensaje
	               salida.flush(); // vacía la salida

	            } // fin de if
	            else
	            {
	               salida.format( "Jugador conectado, por favor espere\n" );
	               salida.flush(); // vacía la salida
	            } // fin de else

	            // Cuando hay dos jugadores conectados comienza el juego 
	            // mientras el juego no termine
	            while ( !seTerminoJuego() ) 
	            {
	               int ubicacion = 0; // inicializa la ubicación del movimiento
	               
	               /* En cada iteración de este ciclo se lee un entero que representa
	                * la ubicación en donde el cliente desea colocar una marca */
	               if ( entrada.hasNext() ){
	                  //ubicacion = entrada.nextInt(); // obtiene la ubicación del movimiento
	                  in = entrada.nextLine(); // obtiene la ubicación del movimiento
	  	         	  separador = new StringTokenizer(in, "-"); //indica como recibir el mensaje
	               	  ubicacion = Integer.parseInt(separador.nextToken()); //obtiene la ubicacion
	               	  palabra = separador.nextToken(); //obtiene la palabra
	               	  orientacion = separador.nextToken().charAt(0); //obtiene la orientacion
	               }
	               // comprueba si el movimiento es válido	                  
	               if ( validarYMover( ubicacion, palabra, orientacion, numeroJugador ) ) 
	               {
	                  mostrarMensaje( "\nubicacion: " + ubicacion  +  " palabra: " + palabra );
	                  //mostrarMensaje( "\norientacion: " + orientacion );
	                  salida.format( "Movimiento valido.\n" ); // notifica al cliente
	                  salida.flush(); // vacía la salida
	               } // fin de if
	               else // el movimiento fue inválido
	               {
	                  salida.format( "Movimiento invalido, intente de nuevo\n" );
	                  salida.flush(); // vacía la salida
	               } // fin de else
	            } // fin de while
	         } // fin de try
	         finally
	         {
	            try
	            {
	               conexion.close(); // cierra la conexión con el cliente
	            } // fin de try
	            catch ( IOException excepcionES ) 
	            {
	               excepcionES.printStackTrace();
	               System.exit( 1 );
	            } // fin de catch
	         } // fin de finally
	      } // fin del método run

	      // establece si se suspende el subproceso o no
	      public void establecerSuspendido(boolean estado)
	      {
	         suspendido = estado; // establece el valor de suspendido
	      } // fin del método establecerSuspendido
	   } // fin de la clase Jugador
	} // fin de la clase ServidorScrabbleJ