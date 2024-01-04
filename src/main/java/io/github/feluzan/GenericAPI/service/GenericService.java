package io.github.feluzan.GenericAPI.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import io.github.feluzan.GenericAPI.entity.AbstractEntity;
import io.github.feluzan.GenericAPI.excepction.RelationExistenceException;
import io.github.feluzan.GenericAPI.repository.AbstractRepository;
import io.github.feluzan.GenericAPI.util.Paging;
import io.github.feluzan.GenericAPI.util.SearchCriteria;
import io.github.feluzan.GenericAPI.util.SearchOperation;
import io.github.feluzan.GenericAPI.util.SearchTerm;
// import br.edu.ifes.sigex.repository.GenericRepository;
// import br.edu.ifes.sigex.service.exception.NegocioException;
// import br.edu.ifes.sigex.service.exception.ObjectNotFoundException;
// import br.edu.ifes.sigex.utils.Paging;
// import br.edu.ifes.sigex.utils.SearchCriteria;
// import br.edu.ifes.sigex.utils.SearchOperation;
// import br.edu.ifes.sigex.utils.SearchTerm;
import lombok.Data;

@Data
@Service
public abstract class GenericService<K,E extends AbstractEntity<K>, JR extends JpaRepository<E, K>, R extends AbstractRepository<K, E, JR>> {

    @Autowired
    protected R repository;

    protected String humanReadableName;

    private String customFilterBaseName;

    private Map<String, String[]> defaultModelFilter = new HashMap<>();

    @Autowired
    ApplicationContext context;

    // public List<M> findAll(Paging paging, ArrayList<SearchCriteria> searchCriterias) {
    //     return repository.findAll(paging, searchCriterias);
    // }

    public E save(E object) throws ObjectNotFoundException {

        // checkRelationsExistences(model);
        // saveValidation(model);

        return this.repository.saveAndFlush(object);
    }

    public E findById(K id) throws ObjectNotFoundException {
        try {
            return this.repository.findById(id);
        } catch (ObjectNotFoundException e) {
            throw new ObjectNotFoundException(this.humanReadableName + " " + id + " does not exist.", e);
        }

    }

    public void softDelete(K id) throws ObjectNotFoundException {
        try {
            E object = this.findById(id);
            this.repository.softDelete(object);
        } catch (ObjectNotFoundException e) {
            throw new ObjectNotFoundException(this.humanReadableName + " " + id + " does not exist.",e);
        }

    }

    public abstract void saveValidation(E object) throws ObjectNotFoundException;

    // public List<M> search(ArrayList<String> values, ArrayList<String> attrList) {
    //     return this.repository.search(values, attrList);
    // }

    public List<E> searchAndList(SearchTerm searchTerm, Paging paging, ArrayList<SearchCriteria> searchCriterias){
        return this.repository.searchAndList(searchTerm, paging, searchCriterias);
    }

    @SuppressWarnings("unchecked")
    public void checkRelationsExistences(E object)
            throws ObjectNotFoundException {

        Class<?> clazz = object.getClass();
        List<Field> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        try {

            for (int i = 0; i < fields.size(); i++) {
                Field field = fields.get(i);
                field.setAccessible(true);
                Object obj;

                obj = field.get(object);

                if (obj == null)
                    continue;
                if (obj instanceof AbstractEntity) {
                    Object id = ((AbstractEntity) obj).getId();

                    Class<?> fieldModelClass = field.getType();

                    String classFullName = fieldModelClass.getName();
                    String[] classArrayName = classFullName.split("\\.");
                    String classModelName = classArrayName[classArrayName.length - 1];

                    char c[] = classModelName.toCharArray();
                    c[0] += 32;
                    String beanName = new String(c) + "Service";

                    Object bean = context.getBean(beanName);
                    Object dbObject = ((GenericService) bean).findById((K) id);

                    field.set(object, dbObject);
                }

            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RelationExistenceException();
        }
    }

    public List<E> findByAttribute(HashMap<String, Object> map){
        return this.repository.findByAttribute(map);
    }

    public E findFirstByAttribute(HashMap<String, Object> map){
        List<E> list= this.repository.findByAttribute(map);
        if(list!=null && list.size()>0) return list.get(0);
        return null;
    }

    public List<E> findByAttribute(HashMap<String, Object> map, HashMap<String, SearchOperation> mapOperations){
        return this.repository.findByAttribute(map, mapOperations);
    }

    public E findFirstByAttribute(HashMap<String, Object> map,  HashMap<String, SearchOperation> mapOperations){
        List<E> list= this.repository.findByAttribute(map, mapOperations);
        if(list!=null && list.size()>0) return list.get(0);
        return null;
    }

    private FilterProvider getFilterProvider(){
        SimpleFilterProvider simpleFilterProvider = new SimpleFilterProvider()
                .setFailOnUnknownId(false);

        for (var entry : this.getDefaultModelFilter().entrySet()) {
            simpleFilterProvider.addFilter(entry.getKey(), SimpleBeanPropertyFilter.serializeAllExcept(entry.getValue()));
        }
        return (FilterProvider) simpleFilterProvider;
    }

    public String getAsJsonString(E object) throws JsonProcessingException{
        ObjectMapper m = new ObjectMapper();
        m.setFilterProvider(getFilterProvider());
        return m.writeValueAsString(object);
    }
}

