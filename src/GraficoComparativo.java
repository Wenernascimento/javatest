import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.util.*;

public class GraficoComparativo {

    public static void exibirGrafico(List<Transacao> transacoes) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Usamos um Map para somar os valores por (tipo, mês/ano)
        Map<String, Double> entradaPorMesAno = new HashMap<>();
        Map<String, Double> saidaPorMesAno = new HashMap<>();

        for (Transacao t : transacoes) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(t.getData());
            String chave = cal.get(Calendar.YEAR) + " - " + String.format("%02d", cal.get(Calendar.MONTH) + 1);

            if (t.getTipo().equalsIgnoreCase("Entrada")) {
                entradaPorMesAno.put(chave, entradaPorMesAno.getOrDefault(chave, 0.0) + t.getValor());
            } else if (t.getTipo().equalsIgnoreCase("Saída") || t.getTipo().equalsIgnoreCase("Saida")) {
                saidaPorMesAno.put(chave, saidaPorMesAno.getOrDefault(chave, 0.0) + t.getValor());
            }
        }

        // Adiciona os valores ao dataset
        for (String chave : entradaPorMesAno.keySet()) {
            dataset.addValue(entradaPorMesAno.get(chave), "Entrada", chave);
        }

        for (String chave : saidaPorMesAno.keySet()) {
            dataset.addValue(saidaPorMesAno.get(chave), "Saída", chave);
        }

        // Criação do gráfico
        JFreeChart barChart = ChartFactory.createBarChart(
                "Entradas x Saídas por Mês e Ano",
                "Mês/Ano",
                "Valor (R$)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        JFrame frame = new JFrame("Gráfico Financeiro");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(new ChartPanel(barChart));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
