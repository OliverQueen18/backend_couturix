package com.couturix.rapport;
import com.couturix.commande.CommandeRepository;
import com.couturix.paiement.PaiementRepository;
import com.couturix.paie.PaieRepository;
import com.couturix.stock.StockService;
import com.lowagie.text.Document; import com.lowagie.text.Paragraph; import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream; import java.nio.charset.StandardCharsets; import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*; import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
@Service @RequiredArgsConstructor
public class RapportService {
  private final PaiementRepository paiementRepository;
  private final CommandeRepository commandeRepository;
  private final PaieRepository paieRepository;
  private final StockService stockService;

  public byte[] export(String type, String format, Instant from, Instant to) {
    String csv = toCsv(type);
    return switch (format==null?"csv":format.toLowerCase()) {
      case "xlsx" -> toXlsx(type, csv);
      case "pdf" -> toPdf(type, csv);
      default -> csv.getBytes(StandardCharsets.UTF_8);
    };
  }

  private String toCsv(String type) {
    StringBuilder sb = new StringBuilder("type;valeur\n");
    switch (type) {
      case "ventes" -> paiementRepository.findAll().forEach(p -> sb.append("paiement;").append(p.getMontant()).append("\n"));
      case "commandes" -> commandeRepository.findAll().forEach(c -> sb.append(c.getNumero()).append(";").append(c.getMontantTotal()).append("\n"));
      case "stocks" -> sb.append("valorisation;").append(stockService.valorisation()).append("\n");
      case "paie" -> paieRepository.findAll().forEach(p -> sb.append(p.getTailleur().nomComplet()).append(";").append(p.getMontantNet()).append("\n"));
      default -> sb.append(type).append(";ok\n");
    }
    return sb.toString();
  }

  private byte[] toXlsx(String type, String csv) {
    try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = wb.createSheet(type);
      int r=0;
      for (String line : csv.split("\n")) {
        Row row = sheet.createRow(r++); int c=0;
        for (String col : line.split(";")) row.createCell(c++).setCellValue(col);
      }
      wb.write(out); return out.toByteArray();
    } catch (Exception e) { throw new IllegalStateException(e); }
  }

  private byte[] toPdf(String type, String csv) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Document doc = new Document(); PdfWriter.getInstance(doc, out); doc.open();
      doc.add(new Paragraph("Rapport CouturiX - " + type));
      doc.add(new Paragraph(csv.replace(";", " | ")));
      doc.close(); return out.toByteArray();
    } catch (Exception e) { throw new IllegalStateException(e); }
  }
}
