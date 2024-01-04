package io.github.feluzan.GenericAPI.util;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paging {

    private int current = 1;
    private int last;
    private int size = 10;
    private Long count;

    @JsonIgnore
    private boolean isUsed = true;

}
