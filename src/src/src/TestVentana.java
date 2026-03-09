import javax.swing.*;

public class TestVentana {

    public TestVentana() {

        int score = 0;

        String p1 = JOptionPane.showInputDialog(
                "¿El condón previene ITS?\n1. Sí\n2. No");

        if(p1 != null && p1.equals("1")) score++;

        String p2 = JOptionPane.showInputDialog(
                "¿Las ITS solo afectan a adultos?\n1. Sí\n2. No");

        if(p2 != null && p2.equals("2")) score++;

        AnalizadorIA ia = new AnalizadorIA();
        PlanEducativo plan = new PlanEducativo();

        String analisis = ia.analizar(score);
        String planEducativo = plan.generarPlan(score);

        JOptionPane.showMessageDialog(null,
                "Resultado: "+score+"/2\n\n"+analisis+"\n\n"+planEducativo);
    }
}
