import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProyectoSalud {

    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=ProyectoSalud;encrypt=true;trustServerCertificate=true;";
        String user = "usuarioJava";
        String password = "TuContrasena123";

        try (Connection conexion = DriverManager.getConnection(url, user, password)) {
            JOptionPane.showMessageDialog(null, "¡Conexión Exitosa!\nBienvenido al Sistema de Apoyo Educativo", "SISTEMA PI", JOptionPane.INFORMATION_MESSAGE);

            String[] opciones = {
                    "Métodos Anticonceptivos (HU1)",
                    "Información ITS (HU2)",
                    "Realizar Test (HU3-5)",
                    "Salir"
            };

            int seleccion = 0;
            while (seleccion != 3 && seleccion != -1) {
                seleccion = JOptionPane.showOptionDialog(null,
                        "Seleccione una categoría para consultar:",
                        "MENÚ PRINCIPAL - SPRINT 1",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, opciones, opciones[0]);

                switch (seleccion) {
                    case 0: mostrarContenidos(conexion, "Metodo"); break;
                    case 1: mostrarContenidos(conexion, "ITS"); break;
                    case 2: realizarTest(conexion); break;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error de conexión: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void mostrarContenidos(Connection conexion, String categoria) {
        String sql = "SELECT nombre, descripcion, prevencion FROM ContenidosEducativos WHERE tipo = ?";
        StringBuilder reporte = new StringBuilder("--- INFORMACIÓN DE " + categoria.toUpperCase() + " ---\n\n");

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, categoria);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                reporte.append("• ").append(rs.getString("nombre")).append("\n");
                reporte.append("  Detalle: ").append(rs.getString("descripcion")).append("\n");
                reporte.append("  Prevención: ").append(rs.getString("prevencion")).append("\n");
                reporte.append("------------------------------------------\n");
            }

            JTextArea textArea = new JTextArea(reporte.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new java.awt.Dimension(500, 400));

            JOptionPane.showMessageDialog(null, scrollPane, "Contenidos Educativos", JOptionPane.PLAIN_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al mostrar contenidos: " + e.getMessage());
        }
    }

    public static void realizarTest(Connection conexion) {
        String nombreEstudiante = JOptionPane.showInputDialog(null, "Ingrese su nombre para el registro:", "Identificación", JOptionPane.QUESTION_MESSAGE);
        if (nombreEstudiante == null || nombreEstudiante.isEmpty()) return;

        String sql = "SELECT pregunta, opcion_a, opcion_b, opcion_c, correcta, categoria FROM TestConocimientos";
        List<String> temasAFortalecer = new ArrayList<>();
        int puntaje = 0;

        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String categoriaPregunta = rs.getString("categoria");
                String respuestaCorrecta = rs.getString("correcta").trim().toUpperCase();

                String mensajePregunta = rs.getString("pregunta") + "\n\n" +
                        "A) " + rs.getString("opcion_a") + "\n" +
                        "B) " + rs.getString("opcion_b") + "\n" +
                        "C) " + rs.getString("opcion_c");

                String respUsuario = JOptionPane.showInputDialog(null, mensajePregunta, "Test de Conocimientos", JOptionPane.QUESTION_MESSAGE);

                if (respUsuario != null && respUsuario.toUpperCase().equals(respuestaCorrecta)) {
                    JOptionPane.showMessageDialog(null, "¡CORRECTO!");
                    puntaje += 10;
                } else {
                    JOptionPane.showMessageDialog(null, "INCORRECTO.\nLa respuesta era: " + respuestaCorrecta);
                    if (!temasAFortalecer.contains(categoriaPregunta)) {
                        temasAFortalecer.add(categoriaPregunta);
                    }
                }
            }

            // Análisis Inteligente (HU4)
            String temasStr = temasAFortalecer.isEmpty() ? "Ninguno" : String.join(", ", temasAFortalecer);
            String recomendacionIA = temasAFortalecer.isEmpty() ?
                    "¡Excelente! Posees sólidos conocimientos." :
                    "Tu análisis indica que debes reforzar: " + temasStr;

            JOptionPane.showMessageDialog(null, "RESULTADO FINAL: " + puntaje + " puntos\n\n" + recomendacionIA, "Análisis de la IA", JOptionPane.INFORMATION_MESSAGE);

            guardarResultado(conexion, nombreEstudiante, puntaje, recomendacionIA, temasStr);

            // Plan Educativo Personalizado (HU5)
            if (!temasAFortalecer.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Generando tu ruta de aprendizaje basada en tus debilidades...", "Plan Personalizado", JOptionPane.INFORMATION_MESSAGE);
                for (String tema : temasAFortalecer) {
                    mostrarContenidos(conexion, tema);
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error en el test: " + e.getMessage());
        }
    }

    public static void guardarResultado(Connection conexion, String nombre, int puntaje, String recIA, String temas) {
        String sql = "INSERT INTO ResultadosEstudiante (estudiante_nombre, puntaje, recomendacion_ia, fecha_realizacion, puntaje_obtenido, temas_reforzar) VALUES (?, ?, ?, GETDATE(), ?, ?)";
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setInt(2, puntaje);
            pstmt.setString(3, recIA);
            pstmt.setInt(4, puntaje);
            pstmt.setString(5, temas);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }
}
