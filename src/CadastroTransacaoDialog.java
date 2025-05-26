import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.Date;

public class CadastroTransacaoDialog extends JDialog {
    private JComboBox<String> cmbTipo;
    private JTextField txtData;
    private JTextField txtDescricao;
    private JTextField txtValor;
    private JComboBox<String> cmbFormaPagamento;
    private JButton btnSalvar;
    private JButton btnCancelar;

    private Transacao transacao;
    private boolean salvou;

    private final Color primaryColor;
    private final Color secondaryColor;
    private final Color dangerColor = new Color(220, 80, 60);
    private final Color successColor = new Color(85, 170, 85);

    public CadastroTransacaoDialog(Frame owner, Color primaryColor, Color secondaryColor) {
        super(owner, "Nova Transação", true);
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;

        initComponents();
        layoutComponents();
        configurarEventos();

        setSize(450, 350);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initComponents() {
        cmbTipo = new JComboBox<>(new String[]{"ENTRADA", "SAIDA"});
        txtData = new JTextField(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        txtDescricao = new JTextField();
        txtValor = new JTextField();
        cmbFormaPagamento = new JComboBox<>(new String[]{"Dinheiro", "Débito", "Crédito", "Pix", "Em aberto", "Outro"});

        btnSalvar = new JButton("Salvar");
        btnCancelar = new JButton("Cancelar");
    }

    private void layoutComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.setBackground(secondaryColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        configurarComponentes(fieldFont);
        adicionarComponentes(panel, gbc, labelFont);

        add(panel);
    }

    private void configurarComponentes(Font fieldFont) {
        cmbTipo.setFont(fieldFont);
        cmbTipo.setBackground(Color.WHITE);
        txtData.setFont(fieldFont);
        txtData.setBackground(Color.WHITE);
        txtDescricao.setFont(fieldFont);
        txtDescricao.setBackground(Color.WHITE);
        txtValor.setFont(fieldFont);
        txtValor.setBackground(Color.WHITE);
        cmbFormaPagamento.setFont(fieldFont);
        cmbFormaPagamento.setBackground(Color.WHITE);

        configurarBotoes();
    }

    private void configurarBotoes() {
        btnSalvar.setBackground(successColor);
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFocusPainted(false);
        btnSalvar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSalvar.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        btnCancelar.setBackground(dangerColor);
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCancelar.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        adicionarEfeitosHover();
    }

    private void adicionarEfeitosHover() {
        btnSalvar.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                btnSalvar.setBackground(successColor.darker());
            }
            public void mouseExited(MouseEvent evt) {
                btnSalvar.setBackground(successColor);
            }
        });

        btnCancelar.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                btnCancelar.setBackground(dangerColor.darker());
            }
            public void mouseExited(MouseEvent evt) {
                btnCancelar.setBackground(dangerColor);
            }
        });
    }

    private void adicionarComponentes(JPanel panel, GridBagConstraints gbc, Font labelFont) {
        adicionarLabelEComponente(panel, gbc, labelFont, "Tipo:", cmbTipo, 0);
        adicionarLabelEComponente(panel, gbc, labelFont, "Data (dd/MM/yyyy):", txtData, 1);
        adicionarLabelEComponente(panel, gbc, labelFont, "Descrição:", txtDescricao, 2);
        adicionarLabelEComponente(panel, gbc, labelFont, "Valor:", txtValor, 3);
        adicionarLabelEComponente(panel, gbc, labelFont, "Forma de Pagamento:", cmbFormaPagamento, 4);
        adicionarBotoes(panel, gbc);
    }

    private void adicionarLabelEComponente(JPanel panel, GridBagConstraints gbc, Font labelFont,
                                           String textoLabel, JComponent componente, int linha) {
        gbc.gridx = 0;
        gbc.gridy = linha;
        JLabel label = new JLabel(textoLabel);
        label.setFont(labelFont);
        panel.add(label, gbc);

        gbc.gridx = 1;
        panel.add(componente, gbc);
    }

    private void adicionarBotoes(JPanel panel, GridBagConstraints gbc) {
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        botoesPanel.setBackground(secondaryColor);
        botoesPanel.add(btnSalvar);
        botoesPanel.add(btnCancelar);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        panel.add(botoesPanel, gbc);
    }

    private void configurarEventos() {
        btnSalvar.addActionListener(this::salvarTransacao);
        btnCancelar.addActionListener(e -> cancelar());
        configurarValidacaoValor();
        configurarValidacaoData();
    }

    private void salvarTransacao(ActionEvent e) {
        try {
            validarCampos();

            Date data = new SimpleDateFormat("dd/MM/yyyy").parse(txtData.getText());
            String tipo = (String) cmbTipo.getSelectedItem();
            String descricao = txtDescricao.getText().trim();
            double valor = Double.parseDouble(txtValor.getText().replace(",", "."));
            String forma = (String) cmbFormaPagamento.getSelectedItem();

            if (transacao == null) {
                // Criar nova transação com ID 0 (será atualizado pelo DAO)
                transacao = new Transacao(0, data, tipo, descricao, valor, forma);
            } else {
                // Atualizar transação existente
                transacao.setTipo(tipo);
                transacao.setDescricao(descricao);
                transacao.setValor(valor);
                transacao.setData(data);
                transacao.setFormaPagamento(forma);
            }

            salvou = true;
            dispose();
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Data inválida! Use o formato dd/MM/yyyy",
                    "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Valor inválido! Use números com ponto decimal",
                    "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao salvar: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void validarCampos() throws Exception {
        if (txtDescricao.getText().trim().isEmpty()) {
            throw new Exception("Informe uma descrição para a transação");
        }

        if (txtData.getText().trim().isEmpty()) {
            throw new Exception("Informe uma data válida");
        }

        try {
            double valor = Double.parseDouble(txtValor.getText().replace(",", "."));
            if (valor <= 0) {
                throw new Exception("O valor deve ser maior que zero");
            }
        } catch (NumberFormatException e) {
            throw new Exception("Valor inválido");
        }
    }

    private void configurarValidacaoValor() {
        txtValor.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!(Character.isDigit(c) || c == '.' || c == ',' ||
                        c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE)) {
                    evt.consume();
                }
            }
        });
    }

    private void configurarValidacaoData() {
        txtData.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                char c = evt.getKeyChar();
                String text = txtData.getText();

                // Permite apenas números e barras
                if (!(Character.isDigit(c) || c == '/' ||
                        c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE)) {
                    evt.consume();
                    return;
                }

                // Auto-insere barras
                if (Character.isDigit(c) && (text.length() == 2 || text.length() == 5)) {
                    txtData.setText(text + "/");
                    txtData.setCaretPosition(text.length() + 1);
                }
            }
        });
    }

    private void cancelar() {
        salvou = false;
        dispose();
    }

    public void preencherDados(Transacao transacao) {
        this.transacao = transacao;
        cmbTipo.setSelectedItem(transacao.getTipo());
        txtData.setText(new SimpleDateFormat("dd/MM/yyyy").format(transacao.getData()));
        txtDescricao.setText(transacao.getDescricao());
        txtValor.setText(String.format("%.2f", transacao.getValor()));
        cmbFormaPagamento.setSelectedItem(transacao.getFormaPagamento());
        setTitle("Editar Transação");
    }

    public void setTipo(String tipo) {
        cmbTipo.setSelectedItem(tipo);
    }

    public Transacao getTransacao() {
        return transacao;
    }

    public boolean isSalvou() {
        return salvou;
    }
}