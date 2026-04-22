import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servidor de Chat con Hilos - Programacion Avanzada 2026
 * Autor: Andres Fernando RIVEROS
 * Descripcion: Servidor TCP que atiende multiples clientes mediante hilos (threads).
 *              Permite mensajeria individual y broadcast, consulta de fecha,
 *              calculo de expresiones, listado de clientes y log completo en consola.
 */
public class Servidor {

    // Mapa thread-safe de clientes conectados: nombre -> handler
    private static ConcurrentHashMap<String, ManejadorCliente> clientes = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        int puerto = 5000;
        System.out.println("==========================================");
        System.out.println("  Servidor de Chat con Hilos - RIVEROS  ");
        System.out.println("==========================================");
        System.out.println("Escuchando en puerto " + puerto + "...\n");

        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            while (true) {
                Socket socket = serverSocket.accept();
                log("Nueva conexion entrante desde: " + socket.getInetAddress());
                // Cada cliente corre en su propio hilo
                ManejadorCliente handler = new ManejadorCliente(socket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Error en el servidor: " + e.getMessage());
        }
    }

    /**
     * Registra un cliente con nombre unico.
     * Si el nombre ya existe, agrega un sufijo numerico.
     */
    public static synchronized String registrarCliente(String nombreBase, ManejadorCliente handler) {
        String nombre = (nombreBase == null || nombreBase.trim().isEmpty()) ? "Usuario" : nombreBase.trim();
        String base = nombre;
        int sufijo = 1;
        while (clientes.containsKey(nombre)) {
            nombre = base + sufijo;
            sufijo++;
        }
        clientes.put(nombre, handler);
        log("Cliente registrado como: " + nombre);
        return nombre;
    }

    /**
     * Elimina un cliente del mapa al desconectarse.
     */
    public static synchronized void eliminarCliente(String nombre) {
        clientes.remove(nombre);
        log("Cliente desconectado: " + nombre);
    }

    /**
     * Retorna la lista de todos los clientes conectados.
     */
    public static synchronized String listarClientes() {
        if (clientes.isEmpty()) return "No hay otros clientes conectados.";
        StringBuilder sb = new StringBuilder("Clientes conectados:\n");
        for (String nombre : clientes.keySet()) {
            sb.append("  - ").append(nombre).append("\n");
        }
        return sb.toString().trim();
    }

    /**
     * Envia un mensaje a un cliente especifico por su nombre de usuario.
     */
    public static synchronized boolean enviarA(String destino, String mensaje, String origen) {
        ManejadorCliente handler = clientes.get(destino);
        if (handler != null) {
            handler.enviarMensaje("[MSG de " + origen + " -> " + destino + "]: " + mensaje);
            log("Mensaje privado de " + origen + " para " + destino + ": " + mensaje);
            return true;
        }
        return false;
    }

    /**
     * Envia un mensaje broadcast a todos los clientes menos al emisor.
     */
    public static synchronized void enviarATodos(String mensaje, String origen) {
        for (Map.Entry<String, ManejadorCliente> entry : clientes.entrySet()) {
            if (!entry.getKey().equals(origen)) {
                entry.getValue().enviarMensaje("[BROADCAST de " + origen + "]: " + mensaje);
            }
        }
        if (!origen.equals("SERVIDOR")) {
            log("Broadcast de " + origen + ": " + mensaje);
        }
    }

    /**
     * Log con timestamp: [yyyy-MM-dd HH:mm:ss] mensaje
     */
    public static void log(String msg) {
        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        System.out.println("[" + ts + "] " + msg);
    }
}
