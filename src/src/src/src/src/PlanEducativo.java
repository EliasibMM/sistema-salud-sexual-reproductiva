public class PlanEducativo {

    public String generarPlan(int score){

        if(score == 2){
            return "Plan: continuar educación preventiva.";
        }

        if(score == 1){
            return "Plan: revisar temas de ITS y métodos anticonceptivos.";
        }

        return "Plan: estudiar conceptos básicos de salud sexual.";
    }
}
