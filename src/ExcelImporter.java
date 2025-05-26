import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class ExcelImporter {
    // Formatos de data com Locale para garantir formatação correta
    private static final SimpleDateFormat DATE_FORMAT_DDMMYYYY =
            new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
    private static final SimpleDateFormat DATE_FORMAT_YYYYMMDD =
            new SimpleDateFormat("yyyy-MM-dd", new Locale("pt", "BR"));

    // Conjuntos para identificação de linhas especiais
    private static final Set<String> MESES_RESUMO = new HashSet<>(Arrays.asList(
            "MÊS", "JAN", "FEV", "MAR", "ABR", "MAI", "JUN",
            "JUL", "AGO", "SET", "OUT", "NOV", "DEZ", "TOTAL"
    ));

    private static final Set<String> PALAVRAS_CHAVE_CABECALHO = new HashSet<>(Arrays.asList(
            "DATA", "TIPO", "DESCRIÇÃO", "DESCRIÇAO", "DESCRICAO", "VALOR",
            "PAGAMENTO", "FORMA", "OBSERVAÇÃO", "OBSERVACAO", "OBS",
            "ENTRADA", "SAÍDA", "SAIDA", "RECEITA", "DESPESA", "HEADER"
    ));

    // Configurações de colunas (ajuste conforme seu arquivo)
    private static final int COLUNA_DATA = 0;
    private static final int COLUNA_TIPO = 2;
    private static final int COLUNA_DESCRICAO = 3;
    private static final int COLUNA_VALOR = 4;
    private static final int COLUNA_PAGAMENTO = 5;
    private static final int COLUNA_OBS = 6;
    private static final int MINIMO_CELULAS = 3; // Reduzido para 3 colunas

    public static List<Transacao> importarTransacoes(File arquivoExcel) throws IOException {
        List<Transacao> transacoes = new ArrayList<>();
        int linhasIgnoradas = 0;
        int linhasProcessadas = 0;
        int linhasCabecalho = 0;

        try (FileInputStream fis = new FileInputStream(arquivoExcel);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean primeiroCabecalhoEncontrado = false;
            Date ultimaDataValida = null; // Para linhas que podem continuar a transação anterior

            for (Row row : sheet) {
                linhasProcessadas++;
                try {
                    if (row == null || isLinhaVazia(row)) {
                        System.out.printf("Linha %d: Ignorando linha vazia.%n", row.getRowNum() + 1);
                        continue;
                    }

                    // Debug: Mostrar conteúdo da linha
                    System.out.printf("Conteúdo linha %d: %s%n", row.getRowNum()+1, getConteudoLinha(row));

                    // Verificação de cabeçalho
                    if (!primeiroCabecalhoEncontrado && isLinhaCabecalhoPrincipal(row)) {
                        System.out.printf("Linha %d: Cabeçalho principal identificado.%n", row.getRowNum() + 1);
                        primeiroCabecalhoEncontrado = true;
                        linhasCabecalho++;
                        continue;
                    }

                    // Verificação de resumo
                    if (isLinhaResumo(row)) {
                        System.out.printf("Linha %d: Ignorando linha de resumo.%n", row.getRowNum() + 1);
                        continue;
                    }

                    // Processar transação com tratamento especial para linhas problemáticas
                    Transacao transacao = parseTransacaoComTolerancia(row, ultimaDataValida);
                    if (transacao != null) {
                        transacoes.add(transacao);
                        ultimaDataValida = transacao.getData(); // Armazena a última data válida
                    }
                } catch (Exception e) {
                    linhasIgnoradas++;
                    System.err.printf("Erro na linha %d: %s%n", row.getRowNum() + 1, e.getMessage());
                }
            }
        }

        System.out.println("\n--- RESUMO DA IMPORTAÇÃO ---");
        System.out.printf("Total de linhas processadas: %d%n", linhasProcessadas);
        System.out.printf("Linhas de cabeçalho: %d%n", linhasCabecalho);
        System.out.printf("Transações importadas: %d%n", transacoes.size());
        System.out.printf("Linhas ignoradas: %d%n", linhasIgnoradas);
        System.out.println("----------------------------");

        return transacoes;
    }

    private static Transacao parseTransacaoComTolerancia(Row row, Date ultimaDataValida) throws ParseException {
        // Verificação flexível de células
        if (row.getPhysicalNumberOfCells() < MINIMO_CELULAS) {
            System.out.printf("Aviso: Linha %d tem apenas %d células. Tentando processar mesmo assim.%n",
                    row.getRowNum() + 1, row.getPhysicalNumberOfCells());
        }

        Transacao transacao = new Transacao();

        // Data - tratamento mais tolerante
        Cell dataCell = row.getCell(COLUNA_DATA, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (dataCell == null) {
            if (ultimaDataValida != null) {
                // Usa a última data válida se a célula estiver vazia
                transacao.setData(ultimaDataValida);
                System.out.printf("Linha %d: Usando data da transação anterior.%n", row.getRowNum() + 1);
            } else {
                throw new ParseException("Célula de data não encontrada e não há data anterior disponível", COLUNA_DATA);
            }
        } else {
            transacao.setData(parseDataFlexivel(dataCell));
        }

        // Tipo - com valor padrão
        transacao.setTipo(parseTipo(row.getCell(COLUNA_TIPO, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)));

        // Descrição - nunca nula
        transacao.setDescricao(parseDescricao(row.getCell(COLUNA_DESCRICAO, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)));

        // Valor - com tratamento de erro específico
        Cell valorCell = row.getCell(COLUNA_VALOR, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (valorCell == null) {
            transacao.setValor(0.0);
        } else {
            transacao.setValor(parseValor(valorCell));
        }

        // Forma de Pagamento - com valor padrão
        transacao.setFormaPagamento(parseFormaPagamento(row.getCell(COLUNA_PAGAMENTO, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)));

        // Observação - opcional
        transacao.setObs(parseObservacao(row.getCell(COLUNA_OBS, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)));

        return transacao;
    }

    private static Date parseDataFlexivel(Cell cell) throws ParseException {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            throw new ParseException("Célula de data vazia", 0);
        }

        // 1. Tentar como data numérica do Excel
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue();
        }

        // 2. Tentar como string em vários formatos
        String dataStr = cell.toString().trim();
        if (dataStr.isEmpty()) {
            throw new ParseException("Texto de data vazio", 0);
        }

        // Remover possíveis espaços ou caracteres estranhos
        dataStr = dataStr.replaceAll("[^\\d/\\-]", "");

        try {
            // Tentar dd/MM/yyyy
            if (dataStr.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
                return DATE_FORMAT_DDMMYYYY.parse(dataStr);
            }
            // Tentar yyyy-MM-dd
            if (dataStr.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
                return DATE_FORMAT_YYYYMMDD.parse(dataStr);
            }
            // Tentar outros formatos comuns
            if (dataStr.matches("\\d{8}")) { // ddmmyyyy
                String formatted = dataStr.substring(0, 2) + "/" +
                        dataStr.substring(2, 4) + "/" +
                        dataStr.substring(4);
                return DATE_FORMAT_DDMMYYYY.parse(formatted);
            }
        } catch (ParseException e) {
            throw new ParseException("Formato de data inválido: " + dataStr, 0);
        }

        throw new ParseException("Formato de data não reconhecido: " + dataStr, 0);
    }

    private static String getConteudoLinha(Row row) {
        if (row == null) return "[linha nula]";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            sb.append(cell != null ? cell.toString() : "[vazio]").append(" | ");
        }
        return sb.toString();
    }

    private static boolean isLinhaVazia(Row row) {
        if (row == null) return true;

        for (int i = 0; i <= COLUNA_OBS; i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && cell.getCellType() != CellType.BLANK && !cell.toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static boolean isLinhaCabecalhoPrincipal(Row row) {
        // Verifica se é o cabeçalho principal (linha com todos os títulos)
        int colunasCabecalho = 0;
        for (int i = 0; i <= COLUNA_OBS; i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                String valor = cell.toString().trim().toUpperCase();
                if (PALAVRAS_CHAVE_CABECALHO.contains(valor)) {
                    colunasCabecalho++;
                }
            }
        }
        return colunasCabecalho >= 3; // Pelo menos 3 colunas com títulos
    }

    private static boolean isLinhaResumo(Row row) {
        Cell primeiraCelula = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return primeiraCelula != null &&
                MESES_RESUMO.contains(primeiraCelula.toString().trim().toUpperCase());
    }

    private static String parseTipo(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return "SAÍDA"; // Valor padrão
        }

        String valor = cell.toString().trim().toUpperCase();
        return valor.matches(".*(ENTRADA|RECEITA|CR[ÉE]DITO).*") ? "ENTRADA" : "SAÍDA";
    }

    private static String parseDescricao(Cell cell) {
        return cell != null ? cell.toString().trim() : "";
    }

    private static double parseValor(Cell cell) throws NumberFormatException {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return 0.0;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            }

            String valorStr = cell.toString()
                    .replaceAll("[R\\$]", "")
                    .replaceAll("\\.", "")
                    .replaceAll(",", ".")
                    .trim();

            return valorStr.isEmpty() ? 0.0 : Double.parseDouble(valorStr);
        } catch (Exception e) {
            throw new NumberFormatException("Valor inválido: " + cell.toString());
        }
    }

    private static String parseFormaPagamento(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return "OUTROS";
        }

        String valor = cell.toString().toUpperCase().trim();

        if (valor.contains("DÉBITO") || valor.contains("DEBITO")) return "DÉBITO";
        if (valor.contains("CRÉDITO") || valor.contains("CREDITO")) return "CRÉDITO";
        if (valor.contains("PIX")) return "PIX";
        if (valor.contains("DINHEIRO")) return "DINHEIRO";
        if (valor.contains("EM ABERTO")) return "EM ABERTO";
        if (valor.contains("TRANSFERÊNCIA") || valor.contains("TRANSFERENCIA")) return "TRANSFERÊNCIA";

        return "OUTROS";
    }

    private static String parseObservacao(Cell cell) {
        return cell != null ? cell.toString().trim() : "";
    }
}