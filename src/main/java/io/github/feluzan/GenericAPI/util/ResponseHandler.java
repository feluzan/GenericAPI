package io.github.feluzan.GenericAPI.util;


import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@JsonFilter("responseHandlerCustomFilter")
public class ResponseHandler<T> {

    @Getter
    @AllArgsConstructor
    public enum Status {
        OK("ok"),
        ERROR("error"),
        IN_PROGRESS("in-progress");

        private final String status;
    }

    @Getter
    private String status;

    @Getter
    private String code;

    @Getter
    private List<String> messages = new ArrayList<>();

    @Getter
    @Setter
    @JsonProperty("page-info")
    private Paging pageInfo = new Paging();

    @Getter
    @Setter
    private T result;


    //Contrutor simplificado
    public ResponseHandler(T result){
        this.status = Status.OK.getStatus();
        this.code = HttpStatus.valueOf(HttpStatus.OK.value()) + "";
        this.result = result;
    }

    public ResponseHandler(Status status, HttpStatus httpStatus, List<String> messages, T result) {
        this.status = status.getStatus();
        this.code = httpStatus.value() + "";
        this.messages = messages;
        this.result = result;
    }

    public ResponseHandler(Status status, HttpStatus httpStatus, T result) {
        this.status = status.getStatus();
        this.code = httpStatus.value() + "";
        this.result = result;
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }

    public void setStatus(Status status) {
        this.status = status.getStatus();
    }

    public void setCode(HttpStatus httpStatus) {
        this.code = httpStatus.value() + "";
    }

}

