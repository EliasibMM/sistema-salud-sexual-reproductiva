import javax.swing.*;

public class VentanaPrincipal extends JFrame {
    private static final long serialVersionUID = 1L;

    public VentanaPrincipal() {

        setTitle("Sistema de Salud Sexual");
        setSize(400,300);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JButton infoMetodos = new JButton("Métodos Anticonceptivos");
        infoMetodos.setBounds(80,30,220,30);
        add(infoMetodos);

        JButton infoITS = new JButton("Información ITS");
        infoITS.setBounds(80,80,220,30);
        add(infoITS);

        JButton test = new JButton("Realizar Test");
        test.setBounds(80,130,220,30);
        add(test);

        infoMetodos.addActionListener(e ->
                JOptionPane.showMessageDialog(null,
                        "Métodos Anticonceptivos:\n\nCondón\nPastillas\nDIU\nImplante"));
        
        infoITS.addActionListener(e ->
                JOptionPane.showMessageDialog(null,
                        "ITS comunes:\n\nVIH\nSífilis\nGonorrea\nClamidia"));

        test.addActionListener(e -> new TestVentana());

        setVisible(true);
    }
}
