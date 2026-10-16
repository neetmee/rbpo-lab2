package com.rbpo.rbpo_lab2.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import com.rbpo.rbpo_lab2.model.Identifiable;

public abstract class InMemoryRepository<T extends Identifiable> {
    private final Map<Long, T> store = new ConcurrentSkipListMap<>();
    private final AtomicLong seq = new AtomicLong();

    public Long nextId() { return seq.incrementAndGet(); }
    public List<T> findAll() { return List.copyOf(store.values()); }
    public Optional<T> findById(Long id) { return Optional.ofNullable(store.get(id)); }
    public boolean existsById(Long id) { return store.containsKey(id); }
    public T save(T entity) { store.put(entity.id(), entity); return entity; }
    public void deleteById(Long id) { store.remove(id); }

    protected Stream<T> stream() { return store.values().stream(); }
}
