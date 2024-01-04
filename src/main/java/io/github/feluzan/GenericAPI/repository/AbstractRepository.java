package io.github.feluzan.GenericAPI.repository;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.ObjectNotFoundException;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import io.github.feluzan.GenericAPI.entity.AbstractEntity;
import io.github.feluzan.GenericAPI.excepction.InvalidFilterException;
import io.github.feluzan.GenericAPI.util.Paging;
import io.github.feluzan.GenericAPI.util.SearchCriteria;
import io.github.feluzan.GenericAPI.util.SearchOperation;
import io.github.feluzan.GenericAPI.util.SearchTerm;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@NoRepositoryBean
@RequiredArgsConstructor
public abstract class AbstractRepository <K, E extends AbstractEntity<K>, JR extends JpaRepository<E, K>> {

    protected final JR jpaRepository;

    protected final E entityClass;

    @Autowired
    private EntityManager entityManager;


    public E save(E object) {
        return jpaRepository.save(object);
    }

    public E saveAndFlush(E object) {
        return jpaRepository.saveAndFlush(object);
    }

    public Iterable<E> saveAll(Iterable<E> objects) {
        return jpaRepository.saveAll(objects);
    }

    public E findById(K id) throws ObjectNotFoundException {
        E e = jpaRepository.findById(id).orElse(null);
        if (e == null || e.isDeleted())
            throw new ObjectNotFoundException(e, null);
        return e;
    }

    public void softDelete(E object) {
        jpaRepository.delete(object);
    }

    private Predicate getPredicate(Root<E> modelRoot, CriteriaBuilder cb, SearchCriteria sc) {
        Path key = null;
        if (sc.getKey().contains(".")) {
            String[] keys = sc.getKey().split("\\.");
            Join childJoin = null;

            childJoin = modelRoot.join(keys[0], JoinType.LEFT);
            if (childJoin != null) {
                for (int i = 1; i + 1 < keys.length; i++) {
                    childJoin = childJoin.join(keys[i], JoinType.LEFT);
                }
                key = childJoin.get(keys[keys.length - 1]);

            }
        } else {
            key = modelRoot.get(sc.getKey());
        }

        if(sc.getValue() instanceof JSONArray && sc.getOperation().equals(SearchOperation.IN)){
            JSONArray array = (JSONArray) sc.getValue();
            int size = array.length();
            if(key.getJavaType() == Date.class){
                Date[] values = new Date[size];
                for(int i = 0; i<size; i++){
                    values[i] = Date.valueOf(array.get(i).toString());
                }
                return key.in(values);
            }
            if(key.getJavaType().isEnum()){
                Object[] values = new Object[size];
                for(int i = 0; i<size; i++){
                    values[i] = java.lang.Enum.valueOf(key.getJavaType(), array.get(i).toString());
                }
                return key.in(values);
            }
            if(key.getJavaType() == boolean.class || key.getJavaType() == Boolean.class){
                Object[] values = new Object[size];
                for(int i = 0; i<size; i++){
                    values[i] = Boolean.parseBoolean(array.get(i).toString());
                }
                return key.in(values);
            }
            Object[] values = new Object[size];
            for(int i = 0; i<size; i++){
                values[i] = array.get(i);
            }
            return key.in(values);
        }



        Object value = sc.getValue();
        if (key.getJavaType() == Date.class) {
            // A data deve ser no formato YYYY-MM-DD
            value = Date.valueOf(sc.getValue().toString());
        }
        if(key.getJavaType().isEnum()){
            value = java.lang.Enum.valueOf(key.getJavaType(), sc.getValue().toString());
        }
        if(key.getJavaType() == boolean.class || key.getJavaType() == Boolean.class){
            value = Boolean.parseBoolean(sc.getValue().toString());
        }
        Predicate p = null;

        if (sc.getOperation().equals(SearchOperation.IS_NULL)) {
            p = cb.isNull(key);
        }
        if (sc.getOperation().equals(SearchOperation.IS_NOT_NULL)) {
            p = cb.isNotNull(key);
        }

        if (sc.getOperation().equals(SearchOperation.EQUAL)) {
            p = cb.equal(key, value);
        }
        if (sc.getOperation().equals(SearchOperation.LESS_THAN)) {
            p = cb.lessThan(key, (Comparable) value);
        }
        if (sc.getOperation().equals(SearchOperation.GREATER_THEN)) {
            p = cb.greaterThan(key, (Comparable) value);
        }
        if (sc.getOperation().equals(SearchOperation.LESS_OR_EQUAL)) {
            p = cb.lessThanOrEqualTo(key, (Comparable) value);
        }
        if (sc.getOperation().equals(SearchOperation.GREATER_OR_EQUAL)) {
            p = cb.greaterThanOrEqualTo(key, (Comparable) value);
        }
        if (sc.getOperation().equals(SearchOperation.CONTAINS)) {
            p = cb.like(cb.function("unaccent", String.class, cb.lower(key)),
                    "%" + StringUtils.stripAccents(value.toString().toLowerCase()) + "%");
        }
        if (sc.getOperation().equals(SearchOperation.EQUAL_IGNORE_CASE)) {
            p = cb.like(cb.function("unaccent", String.class, cb.lower(key)),
                    StringUtils.stripAccents(value.toString().toLowerCase()));
        }
        if (sc.getOperation().equals(SearchOperation.NOT)) {
            p = cb.notEqual(key, (Comparable) value);
        }
        return p;
    }

    public Order getOrder(Root<E> modelRoot, CriteriaBuilder cb, SearchCriteria sc) {
        Order o = null;
        Path key = null;

        if (sc.getOperation().equals(SearchOperation.ORDER)) {
            String attrString = sc.getKey();
            if (attrString.contains(".")){
                String[] keys = attrString.split("\\.");
                Join childJoin = null;

                childJoin = modelRoot.join(keys[0], JoinType.LEFT);
                if (childJoin != null) {
                    for (int i = 1; i + 1 < keys.length; i++) {
                        //Verificando se é enum
                        if(childJoin.get(keys[i]).getJavaType().isEnum()){
                            if(!keys[i+1].equals("id")){
                                throw new InvalidFilterException( String.format("A propriedade %s é um enum e não pode ser buscada por %s, apenas por id",keys[i],keys[i+1]));
                            }
                        }else{
                            childJoin = childJoin.join(keys[i], JoinType.LEFT);
                        }

                    }
                    key = childJoin.get(keys[keys.length - 1]);

                }
            } else{
                key = modelRoot.get(attrString);
            }
            if (sc.getValue().toString().toUpperCase().equals("DESC"))
                o = cb.desc(key);
            else
                o = cb.asc(key);
        }

        return o;
    }

    private Predicate getSearchPredicate(Root<E> modelRoot, CriteriaBuilder cb, String attrString, String value) {
        Path key = null;
        if (attrString.contains(".")) {
            String[] keys = attrString.split("\\.");
            Join childJoin = null;

            childJoin = modelRoot. join(keys[0], JoinType.LEFT);
            if (childJoin != null) {
                for (int i = 1; i + 1 < keys.length; i++) {
                    childJoin = childJoin.join(keys[i], JoinType.LEFT);
                }
                key = childJoin.get(keys[keys.length - 1]);

            }
        } else {
            key = modelRoot.get(attrString);
        }
        Predicate p = cb.like( cb.function("unaccent",String.class,cb.lower(key.as(String.class))) , "%" + StringUtils.stripAccents(value.toString().toLowerCase()) + "%");
        return p;
    }

    public List<E> searchAndList(SearchTerm searchTerm, Paging paging, ArrayList<SearchCriteria> searchCriterias){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery selectCriteriaQuery = cb.createQuery(entityClass.getClass());
        Root<E> modelRoot = selectCriteriaQuery.from(entityClass.getClass());

        List<Predicate> andPredicateList = new ArrayList<>();

        /** Realizando operações para o SEARCH */
        if (searchTerm != null) {
            for (String value : searchTerm.getValues()) {
                List<Predicate> listPredicate = new ArrayList<>();

                for (String attrString : searchTerm.getAttributes()) {
                    Predicate p = null;
                    try {
                        p = getSearchPredicate(modelRoot, cb, attrString, value);
                    } catch (IllegalArgumentException e) {
                        throw new InvalidFilterException("A propriedade " + attrString + " não está correta.");
                    }
                    if (p == null)
                        continue;
                    listPredicate.add(p);
                }

                andPredicateList.add(cb.or(listPredicate.toArray(new Predicate[0])));
            }
        }

        /** Realizando operações para o LIST com SEARCHCRITERIA */
        for (SearchCriteria sc : searchCriterias) {
            Predicate p = null;
            try {
                p = getPredicate(modelRoot, cb, sc);
            } catch (IllegalArgumentException e) {
                throw new InvalidFilterException("A propriedade " + sc.getKey() + " não está correta.");
            }
            if (p == null)
                continue;
            andPredicateList.add(p);
        }

        List<Order> orderList = new ArrayList<>();
        for (SearchCriteria sc : searchCriterias) {
            Order o = getOrder(modelRoot, cb, sc);
            if (o != null)
                orderList.add(o);
        }
        selectCriteriaQuery.orderBy(orderList);


        andPredicateList.add(cb.equal(modelRoot.get("deleted"), false));
        selectCriteriaQuery.where(andPredicateList.toArray(new Predicate[0]));

        TypedQuery<E> countQuery = entityManager.createQuery(selectCriteriaQuery);
        List<E> fullList = countQuery.getResultList();
        if (paging.isUsed()) {
            int offset = paging.getSize() * (paging.getCurrent() - 1);
            TypedQuery<E> resultQuery = entityManager.createQuery(selectCriteriaQuery);
            resultQuery.setFirstResult(offset);
            resultQuery.setMaxResults(paging.getSize());
            Long totalCount = Long.valueOf(fullList.size());
            paging.setCount(totalCount);
            paging.setLast((int) Math.ceil((double) totalCount / paging.getSize()));
            return resultQuery.getResultList();
        } else {
            return fullList;
        }
    }

    public void deleteAllInBatch() {
        jpaRepository.deleteAllInBatch();
    }

    public List<E> findByAttribute(HashMap<String, Object> map){
        return findByAttribute(map, null);
    }

    public List<E> findByAttribute(HashMap<String, Object> map, HashMap<String, SearchOperation> mapOperations){
        if(mapOperations==null) mapOperations=new HashMap<>();

        ArrayList<SearchCriteria> searchCriterias = new ArrayList<>();
        for (String key : map.keySet()) {
            SearchOperation op = null;
            if(mapOperations.containsKey(key)) op = mapOperations.get(key);
            else op = SearchOperation.EQUAL;
            searchCriterias.add(new SearchCriteria(key, op, map.get(key)));
        }

        Paging p = new Paging();
        p.setUsed(false);
        return searchAndList(null, p, searchCriterias);
    }

}
