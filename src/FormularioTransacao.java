import javax.swing.*;
import java.awt.*;
import java.text.*;
import java.util.Date;

public class FormularioTransacao extends JDialog {
    private JComboBox<String> cbTipo;
    private JTextField txtDescricao;
    private JFormattedTextField txtValor;
    private JComboBox<String> cbFormaPagamento;
    private JButton btnSalvar;
    private JButton btnCancelar;

    private boolean salvou = false;
    private Transacao transacao;

    public FormularioTransacao(JFrame parent, String titulo, Transacao transacao) {
        super(parent, titulo, true);
        this.transacao = transacao == null ? new Transacao() : transacao;

        configurarComponentes();
        configurarLayout();
        configurarEventos();

        if (transacao != null) {
            preencherFormulario();
        }

        setSize(400, 300);
        setLocationRelativeTo(parent);
    }

    private void configurarComponentes() {
        // Configuração dos componentes
        String[] tipos = {"ENTRADA", "SAIDA"};
        cbTipo = new JComboBox<>(tipos);

        txtDescricao = new JTextField(20);

        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        txtValor = new JFormattedTextField(format);
        txtValor.setColumns(10);

        String[] formasPagamento = {"Dinheiro", "Cartão Débito", "Cartão Crédito", "PIX", "Transferência"};
        cbFormaPagamento = new JComboBox<>(formasPagamento);

        btnSalvar = new JButton("Salvar");
        btnCancelar = new JButton("Cancelar");
    }

    private void configurarLayout() {
        JPanel painel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        painel.add(new JLabel("Tipo:"), gbc);

        gbc.gridx = 1;
        painel.add(cbTipo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        painel.add(new JLabel("Descrição:"), gbc);

        gbc.gridx = 1;
        painel.add(txtDescricao, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        painel.add(new JLabel("Valor:"), gbc);

        gbc.gridx = 1;
        painel.add(txtValor, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        painel.add(new JLabel("Forma de Pagamento:"), gbc);

        gbc.gridx = 1;
        painel.add(cbFormaPagamento, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnCancelar);
        painel.add(painelBotoes, gbc);

        add(painel);
    }

    private void configurarEventos() {
        btnSalvar.addActionListener(e -> {
            if (validarFormulario()) {
                salvarDados();
                salvou = true;
                dispose();
            }
        });

        btnCancelar.addActionListener(e -> {
            salvou = false;
            dispose();
        });
    }

    private void preencherFormulario() {
        cbTipo.setSelectedItem(transacao.getTipo());
        txtDescricao.setText(transacao.getDescricao());
        txtValor.setValue(transacao.getValor());
        cbFormaPagamento.setSelectedItem(transacao.getFormaPagamento());
    }

    private boolean validarFormulario() {
        if (txtDescricao.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe a descrição", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            double valor = Double.parseDouble(txtValor.getText().replace(",", "."));
            if (valor <= 0) {
                JOptionPane.showMessageDialog(this, "Valor deve ser maior que zero", "Erro", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Valor inválido", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private void salvarDados() {
        transacao.setTipo((String) cbTipo.getSelectedItem());
        transacao.setDescricao(txtDescricao.getText().trim());
        transacao.setValor(Double.parseDouble(txtValor.getText().replace(",", ".")));
        transacao.setFormaPagamento((String) cbFormaPagamento.getSelectedItem());
        transacao.setData(new Date()); // Atualiza a data para agora
    }

    public Transacao getTransacao() {
        return transacao;
    }

    public boolean isSalvou() {
        return salvou;
    }
}