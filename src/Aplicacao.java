import java.util.List;

public class Aplicacao {

    public static void main(String[] args) {
        Aplicacao app = new Aplicacao();
        app.abrirGraficoComparativo();
    }

    private void abrirGraficoComparativo() {
        TransacaoDAO transacaoDAO = new TransacaoDAO();
        List<Transacao> transacoes = transacaoDAO.listarTodos();
        GraficoComparativo.exibirGrafico(transacoes);
    }
}
