package servidor;

import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Biblioteca {

    static final int PUERTO = 5000;

    // Rutas relativas desde el directorio raíz del proyecto
    private static final String RUTA_ESTUDIANTES = "src/main/java/datos/estudiantes.txt";
    private static final String RUTA_LIBROS = "src/main/java/datos/libros.txt";

    // Mapa de estudiantes y libros
    private Map<String, String> estudiantes = new HashMap<>(); // Código -> Clave
    private Map<String, Libro> libros = new HashMap<>(); // Título -> Objeto Libro

    public Biblioteca() {
        // Inicializar con estudiantes y libros desde archivos
        inicializarDesdeArchivo();


        try {
            // Crear un socket de servidor que escucha en el puerto especificado
            ServerSocket skServidor = new ServerSocket(PUERTO);
            System.out.println("Servidor iniciado en el puerto " + PUERTO);

            // Ciclo infinito para aceptar múltiples conexiones de clientes
            while (true) {
                // Aceptar la conexión de un cliente
                Socket skCliente = skServidor.accept();

                // Manejar la conexión en un hilo separado
                new Thread(new ManejadorCliente(skCliente)).start();
            }
        } catch (Exception e) {
            System.out.println("Error en el servidor: " + e.getMessage());
        }
    }

    private void inicializarDesdeArchivo() {
        try {
            // Cargar estudiantes
            cargarEstudiantes();
            // Cargar libros
            cargarLibros();

            // Mensaje de confirmación
            System.out.println("Datos cargados correctamente.");
            System.out.println("Total de estudiantes: " + estudiantes.size());
            System.out.println("Total de libros: " + libros.size());
        } catch (Exception e) {
            System.out.println("Error al inicializar datos: " + e.getMessage());
        }
    }

    private void cargarEstudiantes() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(RUTA_ESTUDIANTES));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] partes = line.split(":");
            if (partes.length == 2) {
                estudiantes.put(partes[0].trim(), partes[1].trim());
            } else {
                System.out.println("Línea de estudiante mal formateada: " + line);
            }
        }
        reader.close();
    }

    private void cargarLibros() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(RUTA_LIBROS));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] partes = line.split(":");
            if (partes.length == 4) {
                String titulo = partes[0].trim();
                String autor = partes[1].trim();
                String tema = partes[2].trim();
                boolean disponible = Boolean.parseBoolean(partes[3].trim());
                libros.put(titulo, new Libro(titulo, autor, tema, disponible));
            } else {
                System.out.println("Línea de libro mal formateada: " + line);
            }
        }
        reader.close();
    }

    private class ManejadorCliente implements Runnable {
        private Socket skCliente;

        public ManejadorCliente(Socket skCliente) {
            this.skCliente = skCliente;
        }

        @Override
        public void run() {
            try {
                // Flujo de entrada y salida
                DataInputStream flujoEntrada = new DataInputStream(skCliente.getInputStream());
                DataOutputStream flujoSalida = new DataOutputStream(skCliente.getOutputStream());

                String solicitud;
                do {
                    // Leer solicitud del cliente
                    solicitud = flujoEntrada.readUTF();
                    System.out.println("Solicitud recibida: " + solicitud);

                    // Procesar la solicitud
                    String respuesta = procesarSolicitud(solicitud);
                    System.out.println("Enviando respuesta: " + respuesta);

                    // Enviar respuesta al cliente
                    flujoSalida.writeUTF(respuesta);

                } while (!solicitud.equals("Salir"));

                // Cerrar conexión con el cliente
                skCliente.close();
            } catch (Exception e) {
                System.out.println("Error en la conexión con el cliente: " + e.getMessage());
            }
        }

        private String procesarSolicitud(String solicitud) {
            // Parsear la solicitud y realizar la operación correspondiente
            String[] partes = solicitud.split(":");
            String operacion = partes[0];

            switch (operacion) {
                case "Autenticación":
                    String codigo = partes[1].trim();
                    String clave = partes[2].trim();
                    return autenticarEstudiante(codigo, clave);

                case "ConsultarLibros":
                    String tipoBusqueda = partes[1].trim();
                    String criterioBusqueda = partes.length > 2 ? partes[2].trim() : "";
                    return procesarConsultaLibros(tipoBusqueda, criterioBusqueda);

                case "PrestarLibro":
                    String libroSolicitado = partes[1].trim();
                    return prestarLibro(libroSolicitado);

                case "DevolverLibro":
                    String libroDevuelto = partes[1].trim();
                    return devolverLibro(libroDevuelto);

                case "CambiarClave":
                    return cambiarClave(partes[1].trim(), partes[2].trim());

                case "Salir":
                    return "Conexión terminada por el cliente.";

                default:
                    return "Operación no reconocida";
            }
        }

        private String procesarConsultaLibros(String criterio, String criterioBusqueda) {
            switch (criterio) {
                case "Nombre":
                    return consultarLibrosPorNombre(criterioBusqueda);
                case "Autor":
                    return consultarLibrosPorAutor(criterioBusqueda);
                case "Tema":
                    return consultarLibrosPorTema(criterioBusqueda);
                default:
                    return "Criterio de consulta no válido.";
            }
        }


        private String consultarLibrosPorNombre(String nombre) {
            StringBuilder respuesta = new StringBuilder();
            for (Libro libro : libros.values()) {
                if (libro.getTitulo().equalsIgnoreCase(nombre)) {
                    respuesta.append(libro.toString()).append("\n");
                }
            }
            return respuesta.length() > 0 ? respuesta.toString() : "No se encontraron libros con ese nombre.";
        }

        private String consultarLibrosPorAutor(String autor) {
            StringBuilder respuesta = new StringBuilder();
            for (Libro libro : libros.values()) {
                if (libro.getAutor().equalsIgnoreCase(autor)) {
                    respuesta.append(libro.toString()).append("\n");
                }
            }
            return respuesta.length() > 0 ? respuesta.toString() : "No se encontraron libros de ese autor.";
        }

        private String consultarLibrosPorTema(String tema) {
            StringBuilder respuesta = new StringBuilder();
            for (Libro libro : libros.values()) {
                if (libro.getTema().equalsIgnoreCase(tema)) {
                    respuesta.append(libro.toString()).append("\n");
                }
            }
            return respuesta.length() > 0 ? respuesta.toString() : "No se encontraron libros de ese tema.";
        }

        private String autenticarEstudiante(String codigo, String clave) {
            if (estudiantes.containsKey(codigo) && estudiantes.get(codigo).equals(clave)) {
                return "Autenticación exitosa";
            } else {
                return "Autenticación fallida";
            }
        }

        private String prestarLibro(String titulo) {
            if (libros.containsKey(titulo) && libros.get(titulo).isDisponible()) {
                libros.get(titulo).setDisponible(false);
                return "Libro " + titulo + " reservado correctamente.";
            } else {
                return "El libro no está disponible o no existe.";
            }
        }

        private String devolverLibro(String titulo) {
            if (libros.containsKey(titulo) && !libros.get(titulo).isDisponible()) {
                libros.get(titulo).setDisponible(true);
                return "Libro " + titulo + " devuelto correctamente.";
            } else {
                return "El libro no está prestado o no existe.";
            }
        }

        private String cambiarClave(String codigo, String nuevaClave) {
            if (estudiantes.containsKey(codigo)) {
                estudiantes.put(codigo, nuevaClave);
                return "Clave cambiada exitosamente para " + codigo;
            } else {
                return "Estudiante no encontrado.";
            }
        }
    }

    public static void main(String[] args) {
        new Biblioteca();
    }
}

// Clase auxiliar para representar los libros

class Libro {
    private String titulo;
    private String autor;
    private String tema;
    @Getter
    @Setter
    private boolean disponible;

    public Libro(String titulo, String autor, String tema, boolean disponible) {
        this.titulo = titulo;
        this.autor = autor;
        this.tema = tema;
        this.disponible = disponible;
    }

    public String getAutor() {
        return autor;
    }

    public String getTema() {
        return tema;
    }
    public String getTitulo() {
        return titulo;
    }

    @Override
    public String toString() {
        return titulo + " - " + autor + " - " + tema + " - " + (disponible ? "Disponible" : "Prestado");
    }
}
