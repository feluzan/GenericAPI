package io.github.feluzan.GenericAPI.util;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchTerm {

    private ArrayList<String> values;
    private ArrayList<String> attributes;

}
