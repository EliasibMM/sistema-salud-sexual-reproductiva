import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ProyectoSalud {

    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=ProyectoSalud;encrypt=true;trustServerCertificate=true;";
        String user = "usuarioJava";
        String password = "TuContrasena123";

        try (Connection conexion = DriverManager.getConnection(url, user, password)) {
            System.out.println("¡Conectado exitosamente a la base de datos!");
            Scanner sc = new Scanner(System.in);
            int opcion = 0;

            while (opcion != 4) {
                System.out.println("\n=== SISTEMA DE APOYO EDUCATIVO (SPRINT 1) ===");
                System.out.println("1. Consultar Métodos Anticonceptivos (HU1)");
                System.out.println("2. Consultar Información sobre ITS (HU2)");
                System.out.println("3. Realizar Test y Generar Plan (HU3, HU4, HU5)");
                System.out.println("4. Salir");
                System.out.print("Seleccione una opción: ");
                opcion = sc.nextInt();

                switch (opcion) {
                    case 1: mostrarContenidos(conexion, "Metodo"); break;
                    case 2: mostrarContenidos(conexion, "ITS"); break;
                    case 3: realizarTest(conexion); break;
                    case 4: System.out.println("Cerrando sistema..."); break;
                    default: System.out.println("Opción no válida.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }

    public static void mostrarContenidos(Connection conexion, String categoria) {
        String sql = "SELECT nombre, descripcion, prevencion FROM ContenidosEducativos WHERE tipo = ?";
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, categoria);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                System.out.println("\n• " + rs.getString("nombre"));
                System.out.println("  Detalle: " + rs.getString("descripcion"));
                System.out.println("  Prevención: " + rs.getString("prevencion"));
                System.out.println("------------------------------------------");
            }
        } catch (SQLException e) {
            System.out.println("Error al mostrar contenidos: " + e.getMessage());
        }
    }

    public static void realizarTest(Connection conexion) {
        Scanner sc = new Scanner(System.in);
        System.out.print("\nIngrese su nombre para el registro: ");
        String nombreEstudiante = sc.next(); // Captura el nombre para evitar NULL [cite: 16]

        String sql = "SELECT pregunta, opcion_a, opcion_b, opcion_c, correcta, categoria FROM TestConocimientos";
        List<String> temasAFortalecer = new ArrayList<>();
        int puntaje = 0;

        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n--- INICIO DEL TEST DE CONOCIMIENTOS ---");
            while (rs.next()) {
                String categoriaPregunta = rs.getString("categoria");
                String respuestaCorrecta = rs.getString("correcta").trim().toUpperCase();

                System.out.println("\nPregunta: " + rs.getString("pregunta"));
                // Salto de línea para cada opción (Mejora visual)
                System.out.println("A) " + rs.getString("opcion_a"));
                System.out.println("B) " + rs.getString("opcion_b"));
                System.out.println("C) " + rs.getString("opcion_c"));
                System.out.print("Tu respuesta: ");
                String respUsuario = sc.next().toUpperCase();

                // Feedback de respuesta correcta o incorrecta
                if (respUsuario.equals(respuestaCorrecta)) {
                    System.out.println(">> ¡CORRECTO!");
                    puntaje += 10;
                } else {
                    System.out.println(">> INCORRECTO. La respuesta correcta era: " + respuestaCorrecta);
                    // HU4: Análisis de debilidad según la categoría real de la pregunta
                    if (!temasAFortalecer.contains(categoriaPregunta)) {
                        temasAFortalecer.add(categoriaPregunta);
                    }
                }
            }

            System.out.println("\n=========================================");
            System.out.println("RESULTADO FINAL: " + puntaje + " puntos");

            String temasStr = temasAFortalecer.isEmpty() ? "Ninguno" : String.join(", ", temasAFortalecer);
            String recomendacionIA = temasAFortalecer.isEmpty() ?
                    "¡Excelente! Posees sólidos conocimientos." :
                    "Debes reforzar: " + temasStr;

            // Guardar en base de datos sin dejar campos NULL
            guardarResultado(conexion, nombreEstudiante, puntaje, recomendacionIA, temasStr);

            // Generación del Plan Educativo Personalizado (HU5)
            if (!temasAFortalecer.isEmpty()) {
                System.out.println("\n=== TU PLAN EDUCATIVO PERSONALIZADO ===");
                for (String tema : temasAFortalecer) {
                    System.out.println("\nREFORZAMIENTO PARA EL TEMA: " + tema);
                    mostrarContenidos(conexion, tema);
                }
            }
            System.out.println("=========================================");

        } catch (SQLException e) {
            System.out.println("Error en el test: " + e.getMessage());
        }
    }

    public static void guardarResultado(Connection conexion, String nombre, int puntaje, String recIA, String temas) {
        // Asegura que todas las columnas de tu tabla reciban datos
        String sql = "INSERT INTO ResultadosEstudiante (estudiante_nombre, puntaje, recomendacion_ia, fecha_realizacion, puntaje_obtenido, temas_reforzar) VALUES (?, ?, ?, GETDATE(), ?, ?)";

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setInt(2, puntaje);
            pstmt.setString(3, recIA);
            pstmt.setInt(4, puntaje);
            pstmt.setString(5, temas);

            pstmt.executeUpdate();
            System.out.println("\n[SISTEMA] Registro guardado en el historial exitosamente.");
        } catch (SQLException e) {
            System.out.println("Error al guardar en BD: " + e.getMessage());
        }
    }
}
