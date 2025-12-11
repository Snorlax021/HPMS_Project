package UI;

import java.util.Map;
import javax.swing.JTable;

/**
 * Contract for dashboard panels that support a global search/filter initiated from the parent frame.
 * Implementations should expose their tables and apply RowFilters accordingly.
 */
public interface GlobalSearchable {
    /** Returns a mapping of logical table names to table instances for filter targeting. */
    Map<String, JTable> getSearchableTables();
    /** Applies a global search query across all tables. Empty/null should clear search. */
    void applyGlobalSearch(String query);
    /** Clears any global search applied. */
    void clearGlobalSearch();
    /** Applies a column-specific filter to a single table. Empty/null value should clear that filter. */
    void applyGlobalFilter(String tableName, String columnName, String value);
    /** Clears any column-specific filters applied via applyGlobalFilter. */
    void clearGlobalFilter();
}