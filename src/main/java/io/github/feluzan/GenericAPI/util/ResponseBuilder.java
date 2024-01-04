package io.github.feluzan.GenericAPI.util;


import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJacksonValue;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class ResponseBuilder {

    public static MappingJacksonValue buildResponseDeleteOperation() {
        ResponseHandler<Boolean> response = new ResponseHandler<Boolean>(
                ResponseHandler.Status.OK,
                HttpStatus.valueOf(HttpStatus.OK.value()),
                true);
        SimpleBeanPropertyFilter pageInfo = SimpleBeanPropertyFilter.serializeAllExcept("page-info");

        SimpleFilterProvider simpleFilterProvider = new SimpleFilterProvider()
                .addFilter("responseHandlerCustomFilter", pageInfo)
                .setFailOnUnknownId(false);

        MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(response);
        mappingJacksonValue.setFilters((FilterProvider) simpleFilterProvider);
        return mappingJacksonValue;
    }

    // public static ResponseHandler<?> buildResponseModelOperation(Object model) {
    //     return new ResponseHandler<Object>(
    //             ResponseHandler.Status.OK,
    //             HttpStatus.valueOf(HttpStatus.OK.value()),
    //             model);
    // }

    public static MappingJacksonValue buildResponseWithoutPageInfo(ResponseHandler<?> response,
            HashMap<String, SimpleBeanPropertyFilter> aditionalPropertyFilters) {
        SimpleBeanPropertyFilter pageInfo = SimpleBeanPropertyFilter.serializeAllExcept("page-info");

        SimpleFilterProvider simpleFilterProvider = new SimpleFilterProvider()
                .addFilter("responseHandlerCustomFilter", pageInfo)
                .setFailOnUnknownId(false);

        if (aditionalPropertyFilters != null) {
            for (HashMap.Entry<String, SimpleBeanPropertyFilter> propertyFilter : aditionalPropertyFilters.entrySet()) {
                simpleFilterProvider.addFilter(propertyFilter.getKey(), propertyFilter.getValue());
            }
        }

        JsonLoopResolver jsonLoopResolver = new JsonLoopResolver(response.getResult());

        for (var entry : jsonLoopResolver.getToFilterOut().entrySet()) {
            simpleFilterProvider.addFilter(entry.getKey(), SimpleBeanPropertyFilter.serializeAllExcept(entry.getValue().toArray(new String[0])));
        }

        MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(response);
        mappingJacksonValue.setFilters((FilterProvider) simpleFilterProvider);
        return mappingJacksonValue;
    }

    public static MappingJacksonValue buildResponseWithoutPageInfo(ResponseHandler<?> response) {
        return buildResponseWithoutPageInfo(response, null);
    }

    public static MappingJacksonValue buildResponseWithPageInfo(ResponseHandler<?> response,
            HashMap<String, SimpleBeanPropertyFilter> aditionalPropertyFilters) {

        SimpleFilterProvider simpleFilterProvider = new SimpleFilterProvider()
                .setFailOnUnknownId(false);

        if (!response.getPageInfo().isUsed()) {
            SimpleBeanPropertyFilter pageInfo = SimpleBeanPropertyFilter.serializeAllExcept("page-info");
            simpleFilterProvider.addFilter("responseHandlerCustomFilter", pageInfo);
        }

        if (aditionalPropertyFilters != null) {
            for (HashMap.Entry<String, SimpleBeanPropertyFilter> propertyFilter : aditionalPropertyFilters.entrySet()) {
                simpleFilterProvider.addFilter(propertyFilter.getKey(), propertyFilter.getValue());
            }
        }

        JsonLoopResolver jsonLoopResolver = new JsonLoopResolver(response.getResult());

        for (var entry : jsonLoopResolver.getToFilterOut().entrySet()) {
            simpleFilterProvider.addFilter(entry.getKey(), SimpleBeanPropertyFilter.serializeAllExcept(entry.getValue().toArray(new String[0])));
        }

        MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(response);
        mappingJacksonValue.setFilters((FilterProvider) simpleFilterProvider);

        return mappingJacksonValue;
    }

    public static MappingJacksonValue buildResponseWithPageInfo(ResponseHandler<?> response) {
        return buildResponseWithPageInfo(response, null);
    }

}

