package cliente;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Estudiante {

    static final String HOST = "localhost";
    static final int PUERTO = 5000;

    public Estudiante() throws IOException {
        try {
            // Conectar al servidor
            Socket skCliente = new Socket(HOST, PUERTO);

            // Flujo de entrada desde el servidor
            InputStream auxEntrada = skCliente.getInputStream();
            DataInputStream flujoEntrada = new DataInputStream(auxEntrada);

            // Flujo de salida hacia el servidor
            OutputStream auxSalida = skCliente.getOutputStream();
            DataOutputStream flujoSalida = new DataOutputStream(auxSalida);

            // Interactuar con el usuario (autenticación)
            Scanner scanner = new Scanner(System.in);

            System.out.print("Ingrese nombre de estudiante: ");
            String codigo = scanner.nextLine();
            System.out.print("Ingrese su clave: ");
            String clave = scanner.nextLine();

            // Enviar solicitud de autenticación
            flujoSalida.writeUTF("Autenticación:" + codigo + ":" + clave);

            // Recibir respuesta del servidor
            String respuesta = flujoEntrada.readUTF();
            System.out.println("Respuesta del servidor: " + respuesta);

            if (respuesta.equals("Autenticación exitosa")) {
                boolean salir = false;

                while (!salir) {
                    System.out.println("\nMenú:");
                    System.out.println("1. Consultar libros");
                    System.out.println("2. Reservar un libro");
                    System.out.println("3. Devolver un libro");
                    System.out.println("4. Cambiar clave");
                    System.out.println("5. Salir");
                    System.out.print("Seleccione una opción: ");
                    int opcion = scanner.nextInt();
                    scanner.nextLine(); // Limpiar el buffer

                    switch (opcion) {
                        case 1:
                            // Submenú para consultar libros
                            System.out.println("¿Cómo desea consultar los libros?");
                            System.out.println("1. Por nombre");
                            System.out.println("2. Por autor");
                            System.out.println("3. Por tema");
                            System.out.print("Seleccione una opción: ");
                            int subOpcion = scanner.nextInt();
                            scanner.nextLine(); // Limpiar el buffer

                            String criterio = "";
                            if (subOpcion == 1) {
                                System.out.print("Ingrese el nombre del libro: ");
                                String nombreLibro = "Nombre:" + scanner.nextLine();
                                flujoSalida.writeUTF("ConsultarLibros:" + nombreLibro);
                                respuesta = flujoEntrada.readUTF();
                                System.out.println("Libros encontrados:\n" + respuesta);
                                break;

                            } else if (subOpcion == 2) {
                                System.out.print("Ingrese el autor del libro: ");
                                String nombreLibro = "Autor:" + scanner.nextLine();
                                flujoSalida.writeUTF("ConsultarLibros:" + nombreLibro);
                                respuesta = flujoEntrada.readUTF();
                                System.out.println("Libros encontrados:\n" + respuesta);
                                break;
                            } else if (subOpcion == 3) {
                                System.out.print("Ingrese el tema del libro: ");
                                String nombreLibro = "Tema:" + scanner.nextLine();
                                flujoSalida.writeUTF("ConsultarLibros:" + nombreLibro);
                                respuesta = flujoEntrada.readUTF();
                                System.out.println("Libros encontrados:\n" + respuesta);
                                break;
                            } else {
                                System.out.println("Opción no válida.");
                            }
                            break;
                        case 2:
                            System.out.print("Ingrese el título del libro que desea prestar: ");
                            String tituloPrestar = scanner.nextLine();
                            flujoSalida.writeUTF("PrestarLibro:" + tituloPrestar);
                            respuesta = flujoEntrada.readUTF();
                            System.out.println(respuesta);
                            break;

                        case 3:
                            System.out.print("Ingrese el título del libro que desea devolver: ");
                            String tituloDevolver = scanner.nextLine();
                            flujoSalida.writeUTF("DevolverLibro:" + tituloDevolver);
                            respuesta = flujoEntrada.readUTF();
                            System.out.println(respuesta);
                            break;

                        case 4:
                            System.out.print("Ingrese su nueva clave: ");
                            String nuevaClave = scanner.nextLine();
                            flujoSalida.writeUTF("CambiarClave:" + codigo + ":" + nuevaClave);
                            respuesta = flujoEntrada.readUTF();
                            System.out.println(respuesta);
                            break;
                        case 5:
                            salir = true;
                            break;

                        default:
                            System.out.println("Opción no válida.");
                            break;
                    }
                }
            } else {
                System.out.println("Error de autenticación.");
            }

            // Cerrar conexión
            skCliente.close();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] arg) throws IOException {
        new Estudiante();
    }
}
