import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProyectoSalud {

    private static int usuarioLogueadoId = -1;
    private static String usuarioLogueadoNombre = "";

    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=ProyectoSalud;encrypt=true;trustServerCertificate=true;";
        String user = "usuarioJava";
        String password = "TuContrasena123";

        try (Connection conexion = DriverManager.getConnection(url, user, password)) {

            // LOGIN / REGISTRO INTELIGENTE
            while (usuarioLogueadoId == -1) {
                gestionarAcceso(conexion);
            }

            String[] opciones = {"Biblioteca", "Realizar Test (IA)", "Glosario", "Duda al Experto", "Salir"};
            int seleccion = 0;
            while (seleccion != 4 && seleccion != -1) {
                seleccion = JOptionPane.showOptionDialog(null, "Usuario: " + usuarioLogueadoNombre,
                        "MENÚ PRINCIPAL", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opciones, opciones[0]);

                switch (seleccion) {
                    case 0: mostrarContenidos(conexion, "Metodo"); break;
                    case 1: ejecutarTestConIA(conexion); break;
                    case 2: consultarGlosario(conexion); break;
                    case 3: enviarDuda(conexion); break;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error de conexión: " + e.getMessage());
        }
    }

    // --- SISTEMA DE ACCESO (REGISTRO AUTOMÁTICO) ---
    // --- SISTEMA DE ACCESO ESTÉTICO (REGISTRO AUTOMÁTICO) ---
    private static void gestionarAcceso(Connection conexion) {
        // Panel con diseño un poco más limpio para la entrada
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        Object[] message = {
                "Nombre de Usuario:", userField,
                "Contraseña:", passField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "BIENVENIDO AL SISTEMA", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option != JOptionPane.OK_OPTION) System.exit(0);

        String user = userField.getText();
        String pass = new String(passField.getPassword());

        if (user.trim().isEmpty() || pass.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Campos vacíos. Intente de nuevo.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Buscamos si el usuario ya existe
            String sqlBusqueda = "SELECT id_usuario FROM Usuarios WHERE nombre_usuario = ?";
            PreparedStatement psB = conexion.prepareStatement(sqlBusqueda);
            psB.setString(1, user);
            ResultSet rs = psB.executeQuery();

            if (rs.next()) {
                // Existe -> Validamos contraseña
                String sqlLogin = "SELECT id_usuario, nombre_usuario FROM Usuarios WHERE nombre_usuario = ? AND contrasena = ?";
                PreparedStatement psL = conexion.prepareStatement(sqlLogin);
                psL.setString(1, user);
                psL.setString(2, pass);
                ResultSet rsL = psL.executeQuery();

                if (rsL.next()) {
                    usuarioLogueadoId = rsL.getInt("id_usuario");
                    usuarioLogueadoNombre = rsL.getString("nombre_usuario");
                    // Mensaje de éxito estético
                    JOptionPane.showMessageDialog(null,
                            "¡Hola de nuevo, " + usuarioLogueadoNombre + "!\nSesión iniciada correctamente.",
                            "ACCESO CONCEDIDO", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "La contraseña no coincide con el usuario.", "Contraseña Incorrecta", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                // No existe -> Registro estético
                int confirm = JOptionPane.showConfirmDialog(null,
                        "El usuario no existe. ¿Deseas registrarte con estos datos?",
                        "NUEVO REGISTRO", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    String sqlIns = "INSERT INTO Usuarios (nombre_usuario, contrasena) VALUES (?, ?)";
                    PreparedStatement psI = conexion.prepareStatement(sqlIns, Statement.RETURN_GENERATED_KEYS);
                    psI.setString(1, user);
                    psI.setString(2, pass);
                    psI.executeUpdate();
                    ResultSet rsI = psI.getGeneratedKeys();

                    if (rsI.next()) {
                        usuarioLogueadoId = rsI.getInt(1);
                        usuarioLogueadoNombre = user;
                        JOptionPane.showMessageDialog(null,
                                "¡Bienvenido a bordo!\nTu cuenta ha sido creada con éxito.",
                                "REGISTRO COMPLETADO", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error en la base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- TEST CON EXAMEN FORMATEADO (HU4) ---
    private static void ejecutarTestConIA(Connection conexion) {
        String sql = "SELECT pregunta, opcion_a, opcion_b, opcion_c, correcta, categoria FROM TestConocimientos";
        List<String> debilidades = new ArrayList<>();
        int aciertos = 0;

        try (Statement stmt = conexion.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String preguntaExamen = rs.getString("pregunta") + "\n\n" +
                        "A) " + rs.getString("opcion_a") + "\n" +
                        "B) " + rs.getString("opcion_b") + "\n" +
                        "C) " + rs.getString("opcion_c");

                String[] botones = {"A", "B", "C"};
                int r = JOptionPane.showOptionDialog(null, preguntaExamen, "EXAMEN",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, botones, botones[0]);

                String resp = (r == 0) ? "A" : (r == 1) ? "B" : "C";
                if (resp.equals(rs.getString("correcta").trim().toUpperCase())) {
                    aciertos++;
                } else {
                    String cat = rs.getString("categoria");
                    if (!debilidades.contains(cat)) debilidades.add(cat);
                }
            }

            int puntaje = aciertos * 10;
            String temas = debilidades.isEmpty() ? "Ninguno" : String.join(", ", debilidades);
            JOptionPane.showMessageDialog(null, "Puntaje: " + puntaje + "\nFortalecer: " + temas);
            guardarResultado(conexion, puntaje, temas);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- ENVIAR DUDA AL EXPERTO (HU10) ---
    private static void enviarDuda(Connection conexion) {
        JTextArea areaDuda = new JTextArea(5, 30);
        areaDuda.setLineWrap(true);
        areaDuda.setWrapStyleWord(true);
        int r = JOptionPane.showConfirmDialog(null, new JScrollPane(areaDuda), "Escribe tu duda para el experto:", JOptionPane.OK_CANCEL_OPTION);

        if (r == JOptionPane.OK_OPTION && !areaDuda.getText().trim().isEmpty()) {
            String sql = "INSERT INTO ConsultasExpertos (id_usuario, duda) VALUES (?, ?)";
            try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
                pstmt.setInt(1, usuarioLogueadoId);
                pstmt.setString(2, areaDuda.getText().trim());
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Duda enviada al profesional.");
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // --- MÉTODOS ADICIONALES ---
    private static void guardarResultado(Connection conexion, int pts, String rec) {
        String sql = "INSERT INTO ResultadosEstudiante (id_usuario, puntaje, recomendacion_ia, fecha_realizacion) VALUES (?, ?, ?, GETDATE())";
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, usuarioLogueadoId);
            pstmt.setInt(2, pts);
            pstmt.setString(3, rec);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private static void consultarGlosario(Connection conexion) {
        String busq = JOptionPane.showInputDialog("Término a buscar:");
        if (busq != null) {
            String sql = "SELECT nombre, descripcion FROM ContenidosEducativos WHERE nombre LIKE ?";
            try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
                pstmt.setString(1, "%" + busq + "%");
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) JOptionPane.showMessageDialog(null, rs.getString("nombre") + ":\n" + rs.getString("descripcion"));
                else JOptionPane.showMessageDialog(null, "No encontrado.");
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private static void mostrarContenidos(Connection conexion, String cat) {
        JOptionPane.showMessageDialog(null, "Biblioteca cargada.");
    }
}
