import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ManejadorCliente - Hilo que atiende a un cliente individual
 * Autor: Andres Fernando RIVEROS
 * Descripcion: Cada instancia corre en un hilo separado. Procesa los comandos
 *              del cliente y coordina con el Servidor para mensajeria entre clientes.
 *
 * Comandos disponibles:
 *   FECHA            -> Consulta la fecha y hora actual del servidor
 *   LISTA            -> Lista los clientes conectados
 *   CALC <expr>      -> Resuelve una expresion matematica (ej: CALC 2+3*4)
 *   *<nombre> <msg>  -> Envia mensaje privado a un cliente por nombre
 *   *ALL <msg>       -> Envia mensaje a todos los clientes conectados
 *   SALIR            -> Desconecta al cliente del servidor
 */
public class ManejadorCliente implements Runnable {

    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;
    private String nombreUsuario;

    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida  = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            // Solicitar nombre de usuario
            salida.println("Ingrese su nombre de usuario:");
            String nombreBase = entrada.readLine();

            // Registrar con nombre unico
            nombreUsuario = Servidor.registrarCliente(nombreBase, this);

            // Notificar a todos que se conecto
            Servidor.enviarATodos(nombreUsuario + " se ha conectado al chat.", "SERVIDOR");

            // Menu de bienvenida
            salida.println("\n==========================================");
            salida.println(" Bienvenido al Chat del Servidor, " + nombreUsuario + "!");
            salida.println("==========================================");
            salida.println("Comandos disponibles:");
            salida.println("  FECHA              -> Fecha y hora del servidor");
            salida.println("  LISTA              -> Ver clientes conectados");
            salida.println("  CALC <expresion>   -> Calcular expresion matematica");
            salida.println("  *<nombre> <msg>    -> Mensaje privado a un usuario");
            salida.println("  *ALL <msg>         -> Mensaje a todos los usuarios");
            salida.println("  SALIR              -> Desconectarse");
            salida.println("==========================================\n");

            // Ciclo principal de atencion al cliente
            String linea;
            while ((linea = entrada.readLine()) != null) {
                Servidor.log("["+ nombreUsuario +"] Recibido: " + linea);
                procesarComando(linea.trim());
            }

        } catch (IOException e) {
            Servidor.log("Conexion interrumpida con " + nombreUsuario + ": " + e.getMessage());
        } finally {
            desconectar();
        }
    }

    /**
     * Procesa el comando recibido del cliente y ejecuta la accion correspondiente.
     */
    private void procesarComando(String cmd) {
        if (cmd.equalsIgnoreCase("FECHA")) {
            // Consultar fecha y hora del servidor
            String fecha = new SimpleDateFormat("EEEE dd/MM/yyyy HH:mm:ss").format(new Date());
            enviarMensaje("Fecha y hora del servidor: " + fecha);

        } else if (cmd.equalsIgnoreCase("LISTA")) {
            // Listar clientes conectados
            enviarMensaje(Servidor.listarClientes());

        } else if (cmd.equalsIgnoreCase("SALIR")) {
            // Desconectar cliente
            enviarMensaje("Hasta luego, " + nombreUsuario + ". Chau!");
            desconectar();

        } else if (cmd.toUpperCase().startsWith("*ALL ")) {
            // Broadcast a todos
            String msg = cmd.substring(5).trim();
            if (msg.isEmpty()) {
                enviarMensaje("Uso: *ALL <mensaje>");
            } else {
                Servidor.enviarATodos(msg, nombreUsuario);
                enviarMensaje("[Tu -> TODOS]: " + msg);
            }

        } else if (cmd.startsWith("*") && cmd.contains(" ")) {
            // Mensaje privado: *<nombre> <mensaje>
            int espacio = cmd.indexOf(" ");
            String destino = cmd.substring(1, espacio).trim();
            String msg = cmd.substring(espacio + 1).trim();
            if (destino.equalsIgnoreCase(nombreUsuario)) {
                enviarMensaje("No puedes enviarte mensajes a ti mismo.");
            } else if (msg.isEmpty()) {
                enviarMensaje("Uso: *<nombre_usuario> <mensaje>");
            } else if (Servidor.enviarA(destino, msg, nombreUsuario)) {
                enviarMensaje("[Tu -> " + destino + "]: " + msg);
            } else {
                enviarMensaje("Usuario '" + destino + "' no encontrado. Usa LISTA para ver usuarios conectados.");
            }

        } else if (cmd.toUpperCase().startsWith("CALC ")) {
            // Resolver expresion matematica simple
            String expr = cmd.substring(5).trim();
            String resultado = calcular(expr);
            enviarMensaje("CALC " + expr + " = " + resultado);

        } else if (cmd.isEmpty()) {
            // Ignorar lineas vacias

        } else {
            enviarMensaje("Comando no reconocido: '" + cmd + "'. Ver comandos disponibles en el menu de bienvenida.");
        }
    }

    /**
     * Evalua una expresion matematica basica (suma, resta, multiplicacion, division).
     * Solo soporta operaciones con numeros enteros y decimales.
     */
    private String calcular(String expr) {
        try {
            // Evaluar la expresion usando ScriptEngine de Java
            javax.script.ScriptEngine engine =
                new javax.script.ScriptEngineManager().getEngineByName("JavaScript");
            Object resultado = engine.eval(expr);
            return String.valueOf(resultado);
        } catch (Exception e) {
            return "Error: expresion invalida. Ej: CALC 5+3*2";
        }
    }

    /**
     * Envia un mensaje al cliente de este hilo.
     */
    public void enviarMensaje(String msg) {
        if (salida != null) {
            salida.println(msg);
        }
    }

    /**
     * Desconecta al cliente, lo elimina del mapa y notifica a los demas.
     */
    private void desconectar() {
        try {
            if (nombreUsuario != null) {
                Servidor.eliminarCliente(nombreUsuario);
                Servidor.enviarATodos(nombreUsuario + " se ha desconectado.", "SERVIDOR");
                nombreUsuario = null;
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            Servidor.log("Error al cerrar socket: " + e.getMessage());
        }
    }
}
