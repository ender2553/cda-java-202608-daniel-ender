package org.example;


public class TryWithResourcesDemo {

    static class DatabaseConnection implements AutoCloseable {
        String connectionName;

        DatabaseConnection(String connectionName) {
            this.connectionName = connectionName;
            System.out.println("Opened connection: " + connectionName);
        }

        void runQuery(String query) {
            System.out.println("Running query on " + connectionName + ": " + query);
            if (query.contains("BAD")) {
                throw new RuntimeException("Query failed: " + query);
            }
        }

        @Override
        public void close() {
            System.out.println("Closed connection: " + connectionName);
        }
    }

    public static void main(String[] args) {

        System.out.println("--- Successful query - connection still closes ---");
        try (DatabaseConnection conn = new DatabaseConnection("conn-1")) {
            conn.runQuery("SELECT * FROM alerts");
        }

        System.out.println();
        System.out.println("--- Failing query - connection STILL closes automatically ---");
        try (DatabaseConnection conn = new DatabaseConnection("conn-2")) {
            conn.runQuery("BAD QUERY");
        } catch (RuntimeException e) {
            System.out.println("Caught: " + e.getMessage());
        }
    }

}

