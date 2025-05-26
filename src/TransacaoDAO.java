import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class TransacaoDAO {
    private List<Transacao> transacoes;
    private int proximoId = 1;
    private static final String ARQUIVO_DADOS = "transacoes.dat";

    public TransacaoDAO() {
        transacoes = carregarDados();
        if (!transacoes.isEmpty()) {
            proximoId = transacoes.stream().mapToInt(Transacao::getId).max().orElse(0) + 1;
        }
    }

    // CRUD: Create, Read, Update, Delete
    public synchronized void inserir(Transacao transacao) {
        transacao.setId(proximoId++);
        transacoes.add(transacao);
        salvarDados();
    }

    public List<Transacao> listarTodos() {
        return new ArrayList<>(transacoes);
    }

    public Transacao buscarPorId(int id) {
        return transacoes.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public synchronized void atualizar(Transacao transacaoAtualizada) {
        for (int i = 0; i < transacoes.size(); i++) {
            Transacao t = transacoes.get(i);
            if (t.getId() == transacaoAtualizada.getId()) {
                transacoes.set(i, transacaoAtualizada);
                salvarDados();
                break;
            }
        }
    }

    public synchronized void excluir(int id) {
        transacoes.removeIf(t -> t.getId() == id);
        salvarDados();
    }

    // Métodos de persistência
    @SuppressWarnings("unchecked")
    private List<Transacao> carregarDados() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ARQUIVO_DADOS))) {
            return (List<Transacao>) ois.readObject();
        } catch (FileNotFoundException e) {
            return new ArrayList<>(); // Arquivo não existe ainda
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao carregar dados: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void salvarDados() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARQUIVO_DADOS))) {
            oos.writeObject(transacoes);
        } catch (IOException e) {
            System.err.println("Erro ao salvar dados: " + e.getMessage());
        }
    }

    // Métodos auxiliares para cálculos
    public double calcularTotalPorTipo(String tipo) {
        return transacoes.stream()
                .filter(t -> t.getTipo().equalsIgnoreCase(tipo))
                .mapToDouble(Transacao::getValor)
                .sum();
    }

    public double calcularTotalPorTipoEMes(String tipo, int mes) {
        return transacoes.stream()
                .filter(t -> t.getTipo().equalsIgnoreCase(tipo))
                .filter(t -> {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(t.getData());
                    return (cal.get(Calendar.MONTH) + 1) == mes;
                })
                .mapToDouble(Transacao::getValor)
                .sum();
    }

    // Filtro por intervalo de datas
    public List<Transacao> filtrarPorData(Date inicio, Date fim) {
        List<Transacao> resultado = new ArrayList<>();
        for (Transacao t : transacoes) {
            if (!t.getData().before(inicio) && !t.getData().after(fim)) {
                resultado.add(t);
            }
        }
        return resultado;
    }

    // Filtro por forma de pagamento
    public List<Transacao> filtrarPorFormaPagamento(String formaPagamento) {
        List<Transacao> resultado = new ArrayList<>();
        for (Transacao t : transacoes) {
            if (t.getFormaPagamento().equalsIgnoreCase(formaPagamento)) {
                resultado.add(t);
            }
        }
        return resultado;
    }

    // Exportar dados para CSV
    public void exportarParaCSV(String nomeArquivo) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivo))) {
            writer.println("ID,Data,Tipo,Descrição,Valor,FormaPagamento");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            for (Transacao t : transacoes) {
                String linha = String.format("%d,%s,%s,%s,%.2f,%s",
                        t.getId(),
                        sdf.format(t.getData()),
                        t.getTipo(),
                        t.getDescricao().replace(",", " "),
                        t.getValor(),
                        t.getFormaPagamento());
                writer.println(linha);
            }

            System.out.println("Exportado com sucesso para " + nomeArquivo);
        } catch (IOException e) {
            System.err.println("Erro ao exportar CSV: " + e.getMessage());
        }
    }
}
