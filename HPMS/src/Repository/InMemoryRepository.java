package Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Thread-safe in-memory repository that uses an id extractor.
 * Suitable for prototypes and unit tests; replace with DB-backed repo later.
 */
public class InMemoryRepository<ID, T> implements Repository<ID, T> {
    private final Map<ID, T> map = new ConcurrentHashMap<>();
    private final Function<T, ID> idGetter;

    public InMemoryRepository(Function<T, ID> idGetter) {
        this.idGetter = idGetter;
    }

    @Override
    public T save(T entity) {
        ID id = idGetter.apply(entity);
        map.put(id, entity);
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(map.get(id));
    }

    @Override
    public Collection<T> findAll() {
        return map.values();
    }

    @Override
    public boolean delete(ID id) {
        return map.remove(id) != null;
    }
}