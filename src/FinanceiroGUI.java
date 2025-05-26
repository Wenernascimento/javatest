import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FinanceiroGUI extends JFrame {

    // Cores modernas e harmonizadas
    private static final Color PRIMARY_COLOR = new Color(0, 119, 182);
    private static final Color SECONDARY_COLOR = new Color(240, 243, 245);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Color INFO_COLOR = new Color(23, 162, 184);
    private static final Color CARD_BACKGROUND = new Color(255, 255, 255);
    private static final Color BORDER_COLOR = new Color(220, 220, 220);

    // Fontes modernas
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font TABLE_HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font TABLE_CELL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font COMBO_BOX_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font VALUE_FONT = new Font("Segoe UI", Font.BOLD, 18);

    // Componentes da interface
    private final TransacaoDAO transacaoDAO;
    private final JTable tabela;
    private final DefaultTableModel modeloTabela;

    private final JButton btnNovaTransacao;
    private final JButton btnEditarTransacao;
    private final JButton btnExcluirTransacao;
    private final JButton btnExcluirMultiplas;
    private final JButton btnGrafico;
    private final JButton btnImportarExcel;
    private final JButton btnExportarPDF;

    private final JLabel lblSaldo;
    private final JLabel lblEntradas;
    private final JLabel lblSaidas;
    private final JComboBox<String> cmbFiltroMes;
    private final JComboBox<String> cmbFiltroAno;

    public FinanceiroGUI() {
        try {
            transacaoDAO = new TransacaoDAO();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao inicializar o banco de dados: " + e.getMessage(),
                    "Erro Fatal", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            throw new RuntimeException("Falha na inicialização", e);
        }

        configurarJanelaPrincipal();

        modeloTabela = criarModeloTabela();
        tabela = criarTabela();

        btnNovaTransacao = criarBotao("Nova Transação", PRIMARY_COLOR);
        btnEditarTransacao = criarBotao("Editar", WARNING_COLOR);
        btnExcluirTransacao = criarBotao("Excluir", DANGER_COLOR);
        btnExcluirMultiplas = criarBotao("Excluir Múltiplas", new Color(176, 58, 46));
        btnGrafico = criarBotao("Gráfico", SUCCESS_COLOR);
        btnImportarExcel = criarBotao("Importar Excel", INFO_COLOR);
        btnExportarPDF = criarBotao("Exportar PDF", new Color(108, 117, 125));

        cmbFiltroMes = criarComboMes();
        cmbFiltroAno = criarComboAno();

        lblSaldo = criarLabelValor("0.00", PRIMARY_COLOR);
        lblEntradas = criarLabelValor("0.00", SUCCESS_COLOR);
        lblSaidas = criarLabelValor("0.00", DANGER_COLOR);

        configurarListeners();
        organizarLayout();
        carregarTransacoesNaTabela(null, -1);
    }

    private void configurarJanelaPrincipal() {
        setTitle("Sistema Financeiro Pessoal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        setBackground(SECONDARY_COLOR);
    }

    private DefaultTableModel criarModeloTabela() {
        return new DefaultTableModel(
                new Object[] {"ID", "Tipo", "Data", "Descrição", "Valor", "Forma Pagamento"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTable criarTabela() {
        JTable tabela = new JTable(modeloTabela);
        tabela.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tabela.setFont(TABLE_CELL_FONT);
        tabela.setRowHeight(30);
        tabela.getTableHeader().setFont(TABLE_HEADER_FONT);
        tabela.getTableHeader().setBackground(PRIMARY_COLOR);
        tabela.getTableHeader().setForeground(Color.WHITE);
        tabela.setFillsViewportHeight(true);
        tabela.setGridColor(BORDER_COLOR);
        tabela.setSelectionBackground(new Color(220, 240, 255));
        tabela.setSelectionForeground(Color.BLACK);
        tabela.setBorder(null);
        return tabela;
    }

    private JButton criarBotao(String texto, Color corFundo) {
        JButton botao = new JButton(texto);
        botao.setBackground(corFundo);
        botao.setForeground(Color.WHITE);
        botao.setFont(BUTTON_FONT);
        botao.setFocusPainted(false);
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(corFundo.darker(), 1),
                new EmptyBorder(8, 15, 8, 15)
        ));
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));

        botao.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                botao.setBackground(corFundo.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                botao.setBackground(corFundo);
            }
        });
        return botao;
    }

    private JLabel criarLabelValor(String valor, Color cor) {
        JLabel label = new JLabel("R$ " + valor, SwingConstants.RIGHT);
        label.setFont(VALUE_FONT);
        label.setForeground(cor);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }

    private JComboBox<String> criarComboMes() {
        JComboBox<String> combo = new JComboBox<>(
                new String[] {"Todos", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"});
        combo.setFont(COMBO_BOX_FONT);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        });
        return combo;
    }

    private JComboBox<String> criarComboAno() {
        JComboBox<String> combo = new JComboBox<>();
        combo.setFont(COMBO_BOX_FONT);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        });

        int anoAtual = Calendar.getInstance().get(Calendar.YEAR);
        combo.addItem("Todos");
        for (int i = anoAtual - 5; i <= anoAtual + 5; i++) {
            combo.addItem(String.valueOf(i));
        }
        combo.setSelectedItem(String.valueOf(anoAtual));
        return combo;
    }

    private void configurarListeners() {
        btnNovaTransacao.addActionListener(e -> abrirCadastroTransacao(null));
        btnEditarTransacao.addActionListener(e -> editarTransacaoSelecionada());
        btnExcluirTransacao.addActionListener(e -> excluirTransacaoSelecionada());
        btnExcluirMultiplas.addActionListener(e -> excluirTransacoesSelecionadas());
        btnGrafico.addActionListener(e -> abrirGraficoComparativo());
        btnImportarExcel.addActionListener(e -> importarExcel());
        btnExportarPDF.addActionListener(e -> exportarPDF());

        ActionListener filtroListener = e -> {
            try {
                int mesSelecionado = cmbFiltroMes.getSelectedIndex();
                int anoSelecionado = "Todos".equals(cmbFiltroAno.getSelectedItem()) ? -1 :
                        Integer.parseInt((String) cmbFiltroAno.getSelectedItem());
                carregarTransacoesNaTabela(mesSelecionado == 0 ? null : mesSelecionado, anoSelecionado);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao aplicar filtro: " + ex.getMessage(),
                        "Erro de Filtro", JOptionPane.ERROR_MESSAGE);
            }
        };
        cmbFiltroMes.addActionListener(filtroListener);
        cmbFiltroAno.addActionListener(filtroListener);
    }

    private void organizarLayout() {
        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(SECONDARY_COLOR);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Painel do cabeçalho
        JPanel panelHeader = new JPanel(new BorderLayout(0, 10));
        panelHeader.setOpaque(false);

        // Título
        JLabel titleLabel = new JLabel("Controle Financeiro");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        panelHeader.add(titleLabel, BorderLayout.NORTH);

        // Painel de filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelFiltros.setOpaque(false);
        panelFiltros.add(new JLabel("Filtrar por:"));
        panelFiltros.add(cmbFiltroMes);
        panelFiltros.add(cmbFiltroAno);
        panelHeader.add(panelFiltros, BorderLayout.CENTER);

        // Painel de resumo financeiro
        JPanel panelResumo = criarPainelResumo();
        panelHeader.add(panelResumo, BorderLayout.SOUTH);

        // Adicionar cabeçalho ao painel principal
        mainPanel.add(panelHeader, BorderLayout.NORTH);

        // Painel da tabela
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JScrollPane scrollPane = new JScrollPane(tabela);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        // Painel de botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelBotoes.setOpaque(false);
        panelBotoes.add(btnNovaTransacao);
        panelBotoes.add(btnEditarTransacao);
        panelBotoes.add(btnExcluirTransacao);
        panelBotoes.add(btnExcluirMultiplas);
        panelBotoes.add(btnGrafico);
        panelBotoes.add(btnImportarExcel);
        panelBotoes.add(btnExportarPDF);
        mainPanel.add(panelBotoes, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel criarPainelResumo() {
        JPanel panelResumo = new JPanel(new GridLayout(1, 3, 10, 0));
        panelResumo.setOpaque(false);
        panelResumo.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Card Saldo
        JPanel cardSaldo = criarCard("Saldo Atual", lblSaldo, PRIMARY_COLOR);
        panelResumo.add(cardSaldo);

        // Card Entradas
        JPanel cardEntradas = criarCard("Entradas", lblEntradas, SUCCESS_COLOR);
        panelResumo.add(cardEntradas);

        // Card Saídas
        JPanel cardSaidas = criarCard("Saídas", lblSaidas, DANGER_COLOR);
        panelResumo.add(cardSaidas);

        return panelResumo;
    }

    private JPanel criarCard(String titulo, JLabel valorLabel, Color cor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JLabel tituloLabel = new JLabel(titulo);
        tituloLabel.setFont(LABEL_FONT);
        tituloLabel.setForeground(Color.GRAY);
        card.add(tituloLabel, BorderLayout.NORTH);

        valorLabel.setFont(VALUE_FONT);
        valorLabel.setForeground(cor);
        card.add(valorLabel, BorderLayout.CENTER);

        return card;
    }

    // Métodos de negócio (mantidos como no código original)
    private void abrirCadastroTransacao(Transacao transacaoParaEditar) {
        CadastroTransacaoDialog dialog = new CadastroTransacaoDialog(this, PRIMARY_COLOR, SECONDARY_COLOR);

        if (transacaoParaEditar != null) {
            dialog.preencherDados(transacaoParaEditar);
        }

        dialog.setVisible(true);

        if (dialog.isSalvou()) {
            try {
                Transacao t = dialog.getTransacao();

                if (transacaoParaEditar == null) {
                    transacaoDAO.inserir(t);
                    JOptionPane.showMessageDialog(this,
                            "Nova transação registrada com sucesso!",
                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    transacaoDAO.atualizar(t);
                    JOptionPane.showMessageDialog(this,
                            "Transação atualizada com sucesso!",
                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                }

                atualizarTabelaComFiltrosAtuais();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao salvar transação: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editarTransacaoSelecionada() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma transação na tabela para editar.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (tabela.getSelectedRowCount() > 1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione apenas uma transação para editar.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = (int) modeloTabela.getValueAt(linha, 0);
            Transacao t = transacaoDAO.buscarPorId(id);

            if (t != null) {
                abrirCadastroTransacao(t);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Transação não encontrada.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar transação para edição: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirTransacaoSelecionada() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma transação na tabela para excluir.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (tabela.getSelectedRowCount() > 1) {
            JOptionPane.showMessageDialog(this,
                    "Para excluir múltiplas transações, use o botão 'Excluir Múltiplas'.",
                    "Atenção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirmar = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir a transação selecionada?",
                "Confirmar Exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirmar == JOptionPane.YES_OPTION) {
            try {
                int id = (int) modeloTabela.getValueAt(linha, 0);
                transacaoDAO.excluir(id);
                atualizarTabelaComFiltrosAtuais();
                JOptionPane.showMessageDialog(this,
                        "Transação excluída com sucesso.",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao excluir transação: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void excluirTransacoesSelecionadas() {
        int[] linhasSelecionadas = tabela.getSelectedRows();

        if (linhasSelecionadas.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecione pelo menos uma transação para excluir.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmar = JOptionPane.showConfirmDialog(this,
                "Confirma a exclusão de " + linhasSelecionadas.length + " transações selecionadas?",
                "Confirmar Exclusão Múltipla", JOptionPane.YES_NO_OPTION);

        if (confirmar == JOptionPane.YES_OPTION) {
            try {
                List<Integer> idsComErro = new ArrayList<>();
                List<Integer> idsExcluidosComSucesso = new ArrayList<>();

                List<Integer> idsParaExcluir = new ArrayList<>();
                for (int linha : linhasSelecionadas) {
                    idsParaExcluir.add((int) modeloTabela.getValueAt(linha, 0));
                }

                for (int id : idsParaExcluir) {
                    try {
                        transacaoDAO.excluir(id);
                        idsExcluidosComSucesso.add(id);
                    } catch (Exception e) {
                        idsComErro.add(id);
                        System.err.println("Erro ao excluir transação ID " + id + ": " + e.getMessage());
                    }
                }

                atualizarTabelaComFiltrosAtuais();

                if (idsComErro.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            idsExcluidosComSucesso.size() + " transações foram excluídas com sucesso.",
                            "Exclusão Concluída", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            idsExcluidosComSucesso.size() + " transações foram excluídas com sucesso.\n" +
                                    idsComErro.size() + " transações não puderam ser excluídas devido a erros.",
                            "Exclusão Parcial", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Erro inesperado ao excluir transações: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void abrirGraficoComparativo() {
        List<Transacao> transacoes = transacaoDAO.listarTodos();
        if (transacoes.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Não há transações cadastradas para gerar o gráfico.",
                    "Dados Insuficientes", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        GraficoComparativo.exibirGrafico(transacoes);
    }

    private void importarExcel() {
        File arquivoExcel = selecionarArquivoExcel();

        if (arquivoExcel == null) {
            return;
        }

        int confirmar = JOptionPane.showConfirmDialog(this,
                "Deseja importar as transações do arquivo:\n" + arquivoExcel.getName() + "?",
                "Confirmar Importação de Excel", JOptionPane.YES_NO_OPTION);

        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            List<Transacao> transacoesImportadas = ExcelImporter.importarTransacoes(arquivoExcel);

            if (transacoesImportadas.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Nenhuma transação válida foi encontrada no arquivo selecionado.",
                        "Importação Vazia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int visualizar = JOptionPane.showConfirmDialog(this,
                    "Foram encontradas " + transacoesImportadas.size() + " transações.\n" +
                            "Deseja visualizá-las antes de salvar no banco de dados?",
                    "Visualizar Transações Importadas", JOptionPane.YES_NO_OPTION);

            if (visualizar == JOptionPane.YES_OPTION) {
                exibirTransacoesImportadas(transacoesImportadas);
            }

            int salvar = JOptionPane.showConfirmDialog(this,
                    "Deseja salvar as " + transacoesImportadas.size() + " transações importadas no sistema?",
                    "Salvar Transações", JOptionPane.YES_NO_OPTION);

            if (salvar == JOptionPane.YES_OPTION) {
                int salvas = salvarTransacoesImportadas(transacoesImportadas);
                atualizarTabelaComFiltrosAtuais();
                JOptionPane.showMessageDialog(this,
                        salvas + " transações foram importadas com sucesso!",
                        "Importação Concluída", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao importar arquivo Excel: " + e.getMessage() +
                            "\nVerifique se o arquivo está no formato correto e sem erros de leitura.",
                    "Erro de Importação", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro inesperado durante a importação: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private File selecionarArquivoExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecionar Arquivo Excel para Importação");
        fileChooser.setFileFilter(
                new FileNameExtensionFilter("Arquivos Excel (*.xlsx, *.xls)", "xlsx", "xls"));

        int result = fileChooser.showOpenDialog(this);
        return (result == JFileChooser.APPROVE_OPTION) ? fileChooser.getSelectedFile() : null;
    }

    private void exibirTransacoesImportadas(List<Transacao> transacoes) {
        String[] colunas = {"Tipo", "Data", "Descrição", "Valor", "Forma Pagamento"};
        Object[][] dados = new Object[transacoes.size()][5];

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        for (int i = 0; i < transacoes.size(); i++) {
            Transacao t = transacoes.get(i);
            dados[i][0] = t.getTipo();
            dados[i][1] = sdf.format(t.getData());
            dados[i][2] = t.getDescricao();
            dados[i][3] = String.format("R$ %.2f", t.getValor());
            dados[i][4] = t.getFormaPagamento();
        }

        JTable tabelaVisualizacao = new JTable(dados, colunas);
        tabelaVisualizacao.setEnabled(false);
        tabelaVisualizacao.setFont(TABLE_CELL_FONT);
        tabelaVisualizacao.setRowHeight(28);
        tabelaVisualizacao.getTableHeader().setFont(TABLE_HEADER_FONT);
        tabelaVisualizacao.getTableHeader().setBackground(PRIMARY_COLOR);
        tabelaVisualizacao.getTableHeader().setForeground(Color.WHITE);
        tabelaVisualizacao.setGridColor(BORDER_COLOR);

        JScrollPane scrollPane = new JScrollPane(tabelaVisualizacao);
        scrollPane.setPreferredSize(new Dimension(800, 300));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JOptionPane.showMessageDialog(this, scrollPane,
                "Pré-visualização das Transações Importadas", JOptionPane.PLAIN_MESSAGE);
    }

    private int salvarTransacoesImportadas(List<Transacao> transacoes) {
        int salvas = 0;
        for (Transacao t : transacoes) {
            try {
                transacaoDAO.inserir(t);
                salvas++;
            } catch (Exception e) {
                System.err.println("Erro ao salvar transação importada (ID): " + t.getId() + " - " + e.getMessage());
            }
        }
        return salvas;
    }

    private void exportarPDF() {
        List<Transacao> transacoesExibidas = obterTransacoesExibidas();

        if (transacoesExibidas.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Não há transações para exportar com o filtro atual.",
                    "Exportação Vazia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Relatório PDF");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos PDF (*.pdf)", "pdf"));

        String nomeSugerido = "relatorio_financeiro";
        int mesSelecionado = cmbFiltroMes.getSelectedIndex();
        if (mesSelecionado > 0) {
            nomeSugerido += "_" + cmbFiltroMes.getSelectedItem().toString().toLowerCase();
        }
        String anoSelecionado = (String) cmbFiltroAno.getSelectedItem();
        if (!"Todos".equals(anoSelecionado)) {
            nomeSugerido += "_" + anoSelecionado;
        }
        fileChooser.setSelectedFile(new File(nomeSugerido + ".pdf"));

        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            String caminho = arquivo.getAbsolutePath();
            if (!caminho.toLowerCase().endsWith(".pdf")) {
                caminho += ".pdf";
                arquivo = new File(caminho);
            }

            if (arquivo.exists()) {
                int confirmar = JOptionPane.showConfirmDialog(this,
                        "O arquivo '" + arquivo.getName() + "' já existe. Deseja sobrescrevê-lo?",
                        "Confirmar Sobrescrita", JOptionPane.YES_NO_OPTION);
                if (confirmar != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            boolean sucesso = PDFExporter.exportar(transacoesExibidas, caminho);

            if (sucesso) {
                int abrirArquivo = JOptionPane.showConfirmDialog(this,
                        "PDF gerado com sucesso! Deseja abrir o arquivo agora?",
                        "PDF Gerado", JOptionPane.YES_NO_OPTION);
                if (abrirArquivo == JOptionPane.YES_OPTION) {
                    try {
                        Desktop.getDesktop().open(arquivo);
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(this,
                                "Não foi possível abrir o arquivo: " + e.getMessage(),
                                "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Ocorreu um erro ao gerar o PDF. Verifique se o arquivo não está em uso.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private List<Transacao> obterTransacoesExibidas() {
        List<Transacao> transacoes = new ArrayList<>();
        List<Transacao> todasTransacoes = transacaoDAO.listarTodos();

        Integer mesFiltro = cmbFiltroMes.getSelectedIndex();
        if (mesFiltro == 0) mesFiltro = null;

        int anoFiltro = "Todos".equals(cmbFiltroAno.getSelectedItem()) ? -1 :
                Integer.parseInt((String) cmbFiltroAno.getSelectedItem());

        for (Transacao t : todasTransacoes) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(t.getData());

            boolean incluir = true;

            if (mesFiltro != null && (cal.get(Calendar.MONTH) + 1) != mesFiltro) {
                incluir = false;
            }

            if (anoFiltro != -1 && cal.get(Calendar.YEAR) != anoFiltro) {
                incluir = false;
            }

            if (incluir) {
                transacoes.add(t);
            }
        }
        return transacoes;
    }

    private void atualizarTabelaComFiltrosAtuais() {
        int mesSelecionado = cmbFiltroMes.getSelectedIndex();
        int anoSelecionado = "Todos".equals(cmbFiltroAno.getSelectedItem()) ? -1 :
                Integer.parseInt((String) cmbFiltroAno.getSelectedItem());
        carregarTransacoesNaTabela(mesSelecionado == 0 ? null : mesSelecionado, anoSelecionado);
    }

    public void carregarTransacoesNaTabela(Integer mesFiltro, int anoFiltro) {
        modeloTabela.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        try {
            List<Transacao> transacoes = transacaoDAO.listarTodos();

            int transacoesFiltradasCount = 0;
            double totalEntrada = 0;
            double totalSaida = 0;

            for (Transacao t : transacoes) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(t.getData());

                boolean incluir = true;

                if (mesFiltro != null) {
                    int mesTransacao = cal.get(Calendar.MONTH) + 1;
                    if (mesTransacao != mesFiltro) {
                        incluir = false;
                    }
                }

                if (anoFiltro != -1) {
                    int anoTransacao = cal.get(Calendar.YEAR);
                    if (anoTransacao != anoFiltro) {
                        incluir = false;
                    }
                }

                if (incluir) {
                    transacoesFiltradasCount++;
                    if ("ENTRADA".equalsIgnoreCase(t.getTipo())) {
                        totalEntrada += t.getValor();
                    } else {
                        totalSaida += t.getValor();
                    }

                    modeloTabela.addRow(
                            new Object[] {
                                    t.getId(),
                                    t.getTipo(),
                                    sdf.format(t.getData()),
                                    t.getDescricao(),
                                    String.format("R$ %.2f", t.getValor()),
                                    t.getFormaPagamento()
                            });
                }
            }

            double saldo = totalEntrada - totalSaida;
            lblSaldo.setText(String.format("R$ %.2f", saldo));
            lblEntradas.setText(String.format("R$ %.2f", totalEntrada));
            lblSaidas.setText(String.format("R$ %.2f", totalSaida));

            // Atualizar cores dos valores
            lblSaldo.setForeground(saldo >= 0 ? SUCCESS_COLOR : DANGER_COLOR);
            lblEntradas.setForeground(SUCCESS_COLOR);
            lblSaidas.setForeground(DANGER_COLOR);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar transações: " + e.getMessage(),
                    "Erro de Carregamento", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("Falha ao inicializar o FlatLaf");
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            FinanceiroGUI gui = new FinanceiroGUI();
            gui.setVisible(true);
        });
    }
}