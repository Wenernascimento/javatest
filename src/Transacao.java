import java.io.Serializable;
import java.util.Date;

public class Transacao implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private Date data;
    private String tipo;
    private String descricao;
    private double valor;
    private String formaPagamento;
    private String obs; // 🆕 NOVO CAMPO

    public Transacao() {
        this.data = new Date();
    }

    public Transacao(int id, Date data, String tipo, String descricao, double valor, String formaPagamento) {
        this.id = id;
        this.data = data;
        this.tipo = tipo;
        this.descricao = descricao;
        this.valor = valor;
        this.formaPagamento = formaPagamento;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Date getData() { return data; }
    public void setData(Date data) { this.data = data; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }

    public String getObs() { return obs; }              // 🆕 getter
    public void setObs(String obs) { this.obs = obs; }  // 🆕 setter

    @Override
    public String toString() {
        return "Transacao{" +
                "id=" + id +
                ", data=" + data +
                ", tipo='" + tipo + '\'' +
                ", descricao='" + descricao + '\'' +
                ", valor=" + valor +
                ", formaPagamento='" + formaPagamento + '\'' +
                ", obs='" + obs + '\'' +
                '}';
    }
}
