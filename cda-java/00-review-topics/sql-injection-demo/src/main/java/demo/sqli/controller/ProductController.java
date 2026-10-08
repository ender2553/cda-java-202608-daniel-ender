package demo.sqli.controller;

import demo.sqli.model.Product;
import demo.sqli.service.ProductService;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoint for the search box.
 *
 * <p>It hands the raw {@code q} parameter straight to the service (which hands
 * it straight to the repository). The response includes the SQL that was run,
 * so the web page can show the class exactly what their input turned into.
 */
@RestController
public class ProductController {

    private final ProductService products;

    public ProductController(ProductService products) {
        this.products = products;
    }

    /** Response for the page: the executed SQL, the rows, and any DB error. */
    public record SearchResponse(String sql, List<Product> results, String error) {
    }

    @GetMapping("/api/search")
    public SearchResponse search(
            @RequestParam(name = "q", defaultValue = "") String q,
            @RequestParam(name = "secure", defaultValue = "false") boolean secure) {
        // secure=true runs the parameterized path (the fix); default is the
        // vulnerable concatenated path. The UI's "Secure mode" switch sets it.
        String sql = secure ? products.previewSecureSql(q) : products.previewSql(q);
        try {
            List<Product> rows = secure ? products.searchSecure(q) : products.search(q);
            return new SearchResponse(sql, rows, null);
        } catch (DataAccessException ex) {
            // The input reached the database as code and the DB pushed back
            // (a syntax error, or e.g. a dropped table). Showing it is the lesson.
            Throwable root = ex;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            return new SearchResponse(sql, List.of(), root.getMessage());
        }
    }
}
