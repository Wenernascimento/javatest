import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PDFExporter {

    public static boolean exportar(List<Transacao> transacoes, String filePath) {
        Document document = new Document(PageSize.A4.rotate());

        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));

            document.open();
            document.addAuthor("Sistema Financeiro");
            document.addCreator("Java Application");

            Map<String, List<Transacao>> grouped = groupByMonthYear(transacoes);

            for (Map.Entry<String, List<Transacao>> entry : grouped.entrySet()) {
                addMonthSection(document, entry.getKey(), entry.getValue());
                document.newPage();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (document != null && document.isOpen()) {
                document.close();
            }
        }
    }

    private static Map<String, List<Transacao>> groupByMonthYear(List<Transacao> transacoes) {
        Map<String, List<Transacao>> map = new TreeMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");

        for (Transacao t : transacoes) {
            String key = sdf.format(t.getData());
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }

        return map;
    }

    private static void addMonthSection(Document document, String monthYear, List<Transacao> transacoes)
            throws DocumentException {

        Paragraph title = new Paragraph(getMonthYearTitle(monthYear),
                new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        addTableHeader(table);
        addTableData(table, transacoes);
        document.add(table);

        document.add(createSummary(transacoes));
    }

    private static void addTableHeader(PdfPTable table) {
        String[] headers = {"Data", "Descrição", "Valor", "Pagamento", "Obs"};
        Font font = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(new BaseColor(220, 220, 220));
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private static void addTableData(PdfPTable table, List<Transacao> transacoes) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Font font = new Font(Font.FontFamily.HELVETICA, 10);

        for (Transacao t : transacoes) {
            table.addCell(new Phrase(sdf.format(t.getData()), font));
            table.addCell(new Phrase(t.getDescricao(), font));
            table.addCell(new Phrase(String.format("R$ %.2f", t.getValor()), font));
            table.addCell(new Phrase(t.getFormaPagamento(), font));
            table.addCell(new Phrase(t.getObs() != null ? t.getObs() : "", font));
        }
    }

    private static Paragraph createSummary(List<Transacao> transacoes) {
        double income = 0, expense = 0;

        for (Transacao t : transacoes) {
            if ("ENTRADA".equalsIgnoreCase(t.getTipo())) {
                income += t.getValor();
            } else {
                expense += t.getValor();
            }
        }

        Paragraph p = new Paragraph();
        p.setFont(new Font(Font.FontFamily.HELVETICA, 12));
        p.add("Resumo:\n");
        p.add(String.format("Entradas: R$ %.2f\n", income));
        p.add(String.format("Saídas: R$ %.2f\n", expense));
        p.add(String.format("Saldo: R$ %.2f", (income - expense)));
        p.setSpacingBefore(15);

        return p;
    }

    private static String getMonthYearTitle(String monthYear) {
        String[] months = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};

        String[] parts = monthYear.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]);

        return months[month - 1] + " " + year;
    }
}
