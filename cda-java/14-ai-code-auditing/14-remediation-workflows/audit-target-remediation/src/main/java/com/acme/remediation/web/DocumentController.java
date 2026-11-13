package com.acme.remediation.web;

import com.acme.remediation.service.DocumentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/search")
    public List<Map<String, Object>> searchByOwner(@RequestParam String owner,
                                                   @RequestParam String title) {
        return documentService.search(owner, title);
    }

    @GetMapping(value = "/{id}/raw", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> download(@PathVariable("id") String id,
                                           @RequestParam String filename) {
        String body = documentService.readDocumentFile(filename);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getOne(@PathVariable("id") long id) {
        return documentService.getById(id);
    }
}
