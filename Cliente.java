import java.io.*;
import java.net.*;

/**
 * Cliente de Chat con Hilos - Programacion Avanzada 2026
 * Autor: Andres Fernando RIVEROS
 * Descripcion: Cliente TCP de consola que se conecta al servidor de chat.
 *              Usa un hilo separado para recibir mensajes, permitiendo
 *              enviar y recibir de forma concurrente (sin bloquearse).
 *
 * Uso: ejecutar luego de iniciar Servidor.java
 *      Se puede correr multiples instancias para simular varios clientes.
 */
public class Cliente {

    public static void main(String[] args) {
        String host = "localhost";
        int puerto = 5000;

        System.out.println("==========================================");
        System.out.println("    Cliente de Chat - RIVEROS            ");
        System.out.println("==========================================");
        System.out.println("Conectando a " + host + ":" + puerto + "...");

        try (Socket socket = new Socket(host, puerto);
             BufferedReader entradaServidor = new BufferedReader(
                 new InputStreamReader(socket.getInputStream()));
             PrintWriter salidaServidor = new PrintWriter(
                 new OutputStreamWriter(socket.getOutputStream()), true);
             BufferedReader teclado = new BufferedReader(
                 new InputStreamReader(System.in))) {

            System.out.println("Conectado exitosamente al servidor!\n");

            // Hilo receptor: escucha mensajes del servidor en segundo plano
            Thread receptor = new Thread(() -> {
                try {
                    String linea;
                    while ((linea = entradaServidor.readLine()) != null) {
                        System.out.println(linea);
                    }
                } catch (IOException e) {
                    System.out.println("\n[Conexion cerrada por el servidor]");
                }
            });
            receptor.setDaemon(true);  // El hilo muere cuando termina el main
            receptor.start();

            // Hilo principal: lee del teclado y envia al servidor
            String linea;
            while ((linea = teclado.readLine()) != null) {
                salidaServidor.println(linea);
                if (linea.equalsIgnoreCase("SALIR")) {
                    System.out.println("Desconectandose...");
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("[ERROR] No se pudo conectar al servidor: " + e.getMessage());
            System.err.println("Asegurese de que el Servidor este corriendo en " + host + ":" + puerto);
        }

        System.out.println("Cliente finalizado.");
    }
}
