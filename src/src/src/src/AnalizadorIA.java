public class AnalizadorIA {

    public String analizar(int score){

        if(score == 2){
            return "Nivel alto de conocimiento.\nContinúa reforzando tus conocimientos.";
        }

        if(score == 1){
            return "Nivel medio.\nSe recomienda estudiar más sobre prevención de ITS.";
        }

        return "Nivel bajo.\nSe recomienda revisar información sobre métodos anticonceptivos y prevención.";
    }
}
