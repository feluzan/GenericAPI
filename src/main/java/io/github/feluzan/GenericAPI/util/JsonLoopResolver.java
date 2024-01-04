package io.github.feluzan.GenericAPI.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.github.feluzan.GenericAPI.entity.AbstractEntity;
import io.github.feluzan.GenericAPI.excepction.JsonLoopResolverException;
import lombok.Getter;

public class JsonLoopResolver {

    private Map<Integer,List<Class<?>>> levelMap;

    @Getter
    private Map<String, List<String>> toFilterOut;


    public JsonLoopResolver(){
        levelMap = new HashMap<>();
        toFilterOut = new HashMap<>();
    }

    public JsonLoopResolver(Object object) {
        levelMap = new HashMap<>();
        toFilterOut = new HashMap<>();
            this.resolve(object);
    }

    public void resolve(Object object) {
        this.resolve(object,0,"");
    }

    public void resolve(Object object, int level, String jsonFilterName) {

        if(object instanceof Collection){
            Collection objectColletcion = (Collection)object;
            if( objectColletcion.size() > 0 ){
                Object item = objectColletcion.iterator().next();
                resolve(item, level, jsonFilterName);
            }
            return;
        }

        if(object instanceof AbstractEntity){
            if(!levelMap.containsKey(level)) levelMap.put(level,new ArrayList<Class<?>>());
            if(!levelMap.get(level).contains(object.getClass())) levelMap.get(level).add(object.getClass());
        }


        for(Field f : object.getClass().getDeclaredFields()){
            JsonIgnore annJsonIgnore = f.getAnnotation(JsonIgnore.class);
            if(annJsonIgnore!=null) continue;
            f.setAccessible(true);

            Object item;
            try {
                item = f.get(object);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
                throw new JsonLoopResolverException("Erro no JsonLoopResolver");
            }

            if(item instanceof Collection){
                Collection objectColletcion = (Collection)item;
                if( objectColletcion.size() > 0 ){
                    item = objectColletcion.iterator().next();
                    // resolver(item, level, jsonFilterName);
                    // return;
                }else continue;
            }

            if(item instanceof AbstractEntity){

                //Verifica se a classe está em levels anteriores
                boolean isFiltered = false;
                for(int i = 0;i<level; i++){

                    if(levelMap.get(i).contains(f.getDeclaringClass())){
                        isFiltered = true;
                        if(!toFilterOut.containsKey(jsonFilterName)) toFilterOut.put(jsonFilterName, new ArrayList<>());
                        toFilterOut.get(jsonFilterName).add(f.getName());
                        break;
                    }
                }

                if(!isFiltered){
                    JsonFilter annJsonFilter = f.getAnnotation(JsonFilter.class);
                    if(annJsonFilter==null) {
                        throw new JsonLoopResolverException("Não existe JsonFilter para o atributo " + f.getName() + " da classe " + f.getDeclaringClass());
                    }
                    // System.out.println("Level " + level + "\tAtributo: " + f.getName() + "\tClasse: " + f.getDeclaringClass());
                    resolve(item, level+1,annJsonFilter.value());
                }

            }
        }

    }



}
