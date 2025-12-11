package Repository;

import java.util.Collection;
import java.util.Optional;

/**
 * Generic repository interface (very small).
 * ID: type of the identifier (String in our models)
 * T: entity type
 */
public interface Repository<ID, T> {
    T save(T entity);
    Optional<T> findById(ID id);
    Collection<T> findAll();
    boolean delete(ID id);
}
