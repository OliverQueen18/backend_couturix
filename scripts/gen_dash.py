# -*- coding: utf-8 -*-
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1] / "src" / "main" / "java" / "com" / "couturix"

def w(rel, content):
    p = ROOT / rel
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content.strip() + "\n", encoding="utf-8")

w("dashboard/DashboardController.java", '''
package com.couturix.dashboard;
import com.couturix.caisse.CaisseService;
import com.couturix.client.ClientRepository;
import com.couturix.commande.CommandeRepository;
import com.couturix.domain.enums.CommandeStatut;
import com.couturix.paiement.PaiementRepository;
import com.couturix.stock.StockService;
import java.math.BigDecimal; import java.time.*; import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/dashboard") @RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DashboardController {
  private final PaiementRepository paiementRepository;
  private final CommandeRepository commandeRepository;
  private final ClientRepository clientRepository;
  private final StockService stockService;
  private final CaisseService caisseService;

  @GetMapping
  public Map<String, Object> dashboard(@RequestParam(required=false) Instant from, @RequestParam(required=false) Instant to) {
    Instant f = from==null ? Instant.now().minus(Duration.ofDays(30)) : from;
    Instant t = to==null ? Instant.now() : to;
    BigDecimal ca = paiementRepository.findByCreatedAtBetween(f, t).stream().map(p -> p.getMontant()).reduce(BigDecimal.ZERO, BigDecimal::add);
    long commandes = commandeRepository.count();
    long retards = commandeRepository.countByDatePrevueBeforeAndStatutNot(LocalDate.now(), CommandeStatut.LIVREE);
    var caisse = caisseService.courante();
    BigDecimal solde = caisse==null ? BigDecimal.ZERO : caisse.getSoldeInitial();
    return Map.of(
      "chiffreAffaires", ca,
      "depenses", BigDecimal.ZERO,
      "beneficeEstime", ca,
      "commandes", commandes,
      "commandesEnRetard", retards,
      "clients", clientRepository.count(),
      "stockFaible", stockService.alertes().size(),
      "soldeCaisse", solde
    );
  }
}
''')

w("rapport/RapportService.java", '''
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
    StringBuilder sb = new StringBuilder("type;valeur\\n");
    switch (type) {
      case "ventes" -> paiementRepository.findAll().forEach(p -> sb.append("paiement;").append(p.getMontant()).append("\\n"));
      case "commandes" -> commandeRepository.findAll().forEach(c -> sb.append(c.getNumero()).append(";").append(c.getMontantTotal()).append("\\n"));
      case "stocks" -> sb.append("valorisation;").append(stockService.valorisation()).append("\\n");
      case "paie" -> paieRepository.findAll().forEach(p -> sb.append(p.getTailleur().nomComplet()).append(";").append(p.getMontantNet()).append("\\n"));
      default -> sb.append(type).append(";ok\\n");
    }
    return sb.toString();
  }

  private byte[] toXlsx(String type, String csv) {
    try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = wb.createSheet(type);
      int r=0;
      for (String line : csv.split("\\n")) {
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
''')
w("rapport/RapportController.java", '''
package com.couturix.rapport;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/rapports") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','GERANT','COMPTABLE')")
public class RapportController {
  private final RapportService service;
  @GetMapping("/{type}")
  public ResponseEntity<byte[]> export(@PathVariable String type, @RequestParam(defaultValue="csv") String format,
      @RequestParam(required=false) Instant from, @RequestParam(required=false) Instant to) {
    byte[] data = service.export(type, format, from, to);
    String ext = switch (format) { case "xlsx" -> "xlsx"; case "pdf" -> "pdf"; default -> "csv"; };
    MediaType mt = ext.equals("pdf")?MediaType.APPLICATION_PDF:MediaType.APPLICATION_OCTET_STREAM;
    return ResponseEntity.ok().contentType(mt).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rapport-"+type+"."+ext).body(data);
  }
}
''')

w("admin/AdminController.java", '''
package com.couturix.admin;
import com.couturix.common.api.PageResponse;
import com.couturix.common.audit.HistoriqueAction;
import com.couturix.common.audit.HistoriqueActionRepository;
import java.time.Instant; import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class AdminController {
  private final ParametreRepository parametreRepository;
  private final HistoriqueActionRepository historiqueActionRepository;
  @GetMapping("/api/v1/parametres") public List<Parametre> params() { return parametreRepository.findAll(); }
  @PutMapping("/api/v1/parametres/{cle}") public Parametre update(@PathVariable String cle, @RequestBody Parametre body) {
    Parametre p = parametreRepository.findById(cle).orElseGet(() -> { Parametre n=new Parametre(); n.setCle(cle); return n; });
    p.setValeur(body.getValeur()); p.setDescription(body.getDescription()); p.setUpdatedAt(Instant.now());
    return parametreRepository.save(p);
  }
  @GetMapping("/api/v1/audit") public PageResponse<HistoriqueAction> audit(Pageable pageable) { return PageResponse.of(historiqueActionRepository.findAll(pageable)); }
}
''')

print("dash+rapport+admin ok")
