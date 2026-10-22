package com.couturix.rapport;
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
      @RequestParam(required=false) String from, @RequestParam(required=false) String to) {
    var range = com.couturix.common.time.QueryInstants.parse(from, to);
    byte[] data = service.export(type, format, range.start(), range.endExclusive());
    String ext = switch (format) { case "xlsx" -> "xlsx"; case "pdf" -> "pdf"; default -> "csv"; };
    MediaType mt = ext.equals("pdf")?MediaType.APPLICATION_PDF:MediaType.APPLICATION_OCTET_STREAM;
    return ResponseEntity.ok().contentType(mt).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rapport-"+type+"."+ext).body(data);
  }
}
