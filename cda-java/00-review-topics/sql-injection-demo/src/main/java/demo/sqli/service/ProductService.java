package demo.sqli.service;

import demo.sqli.model.Product;
import demo.sqli.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business layer for products.
 *
 * <p>Note what it does with the search term: nothing. It passes the raw text
 * straight from the controller to the repository. No validation, no escaping.
 * This is how injectable input travels "blindly" through a layered app — each
 * layer trusts the one before it, so the untrusted string reaches the SQL
 * untouched.
 */
@Service
public class ProductService {

    private final ProductRepository products;

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    public List<Product> search(String term) {
        return products.search(term);
    }

    public String previewSql(String term) {
        return products.buildSearchSql(term);
    }

    // Secure (parameterized) counterparts, for the live "Secure mode" switch.

    public List<Product> searchSecure(String term) {
        return products.searchSecure(term);
    }

    public String previewSecureSql(String term) {
        return products.describeSecureSql(term);
    }
}
