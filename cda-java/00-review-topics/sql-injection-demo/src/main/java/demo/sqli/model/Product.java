package demo.sqli.model;

/**
 * A row shown in the search results.
 *
 * <p>Normally these three fields hold a product's name, category and price.
 * But when a UNION attack appends rows from another table, whatever that query
 * selected lands in these same three fields — so you may see a username in
 * {@code name} and a password in {@code category}. That mismatch is itself the
 * lesson: the data is coming from a table the search box was never meant to read.
 *
 * <p>{@code price} is a String (not a number) so that stolen text columns can be
 * displayed here without a type error.
 */
public class Product {

    private String name;
    private String category;
    private String price;

    public Product() {
    }

    public Product(String name, String category, String price) {
        this.name = name;
        this.category = category;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
