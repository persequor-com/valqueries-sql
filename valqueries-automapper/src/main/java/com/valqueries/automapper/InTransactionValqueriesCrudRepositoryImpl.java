package com.valqueries.automapper;

import com.valqueries.ITransactionContext;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class InTransactionValqueriesCrudRepositoryImpl<T, K> implements InTransactionValqueriesCrudRepository<T, K> {
    private ValqueriesCrudRepository<T, K> crudRepo;
    private final ITransactionContext tx;

    public InTransactionValqueriesCrudRepositoryImpl(ValqueriesCrudRepository<T, K> crudRepo, ITransactionContext tx) {
        this.crudRepo = crudRepo;
        this.tx = tx;
    }
    @Override
    public Optional<T> get(K k) {
        return crudRepo.get(tx, k);
    }

    @Override
    public Stream<T> getAll() {
        return crudRepo.getAll(tx);
    }

    @Override
    public CrudUpdateResult deleteById(K k) {
        return crudRepo.deleteById(tx, k);
    }

    @Override
    public CrudUpdateResult deleteByIds(Collection<K> collection) {
        return crudRepo.deleteByIds(tx, collection);
    }

    @Override
    public CrudUpdateResult save(T t) {
        return crudRepo.save(tx, t);
    }

    @Override
    public CrudUpdateResult insert(T t) throws ValqueriesInsertFailedException {
        return crudRepo.insert(tx, t);
    }

    @Override
    public CrudUpdateResult save(Collection<T> t) {
        return crudRepo.save(tx, t);
    }

    @Override
    public CrudUpdateResult insert(Collection<T> t) throws ValqueriesInsertFailedException {
        return crudRepo.insert(tx, t);
    }

    @Override
    public <O> CrudUpdateResult saveOther(O t, Class<O> oClass) {
        return crudRepo.saveOther(tx, t, oClass);
    }

    @Override
    public <O> CrudUpdateResult insertOther(O t, Class<O> oClass) throws ValqueriesInsertFailedException {
        return crudRepo.insertOther(tx, t, oClass);
    }

    @Override
    public <O> CrudUpdateResult saveOthers(Collection<O> t, Class<O> oClass) {
        return crudRepo.saveOthers(tx,t, oClass);
    }

    @Override
    public <O> CrudUpdateResult insertOthers(Collection<O> t, Class<O> oClass) throws ValqueriesInsertFailedException {
        return crudRepo.insertOthers(tx, t, oClass);
    }
}
