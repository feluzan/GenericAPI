package io.github.feluzan.GenericAPI.util;

/**
 * eq ou == : operador de igualdade;
 * lt ou < : verifica se o valor de um determinado atributo é menor que um valor
 * informado;
 * gt ou > : verifica se o valor de um determinado atributo é maior que um valor
 * informado;
 * le ou < = : verifica se o valor de um determinado atributo é menor ou igual a
 * um valor informado;
 * ge ou >= : verifica se o valor de um determinado atributo é maior ou igual a
 * um valor informado;
 * in : verifica se o valor de um determinado atributo corresponde a um ou mais
 * itens de uma lista de valores informada;
 * like : verifica se um trecho de texto está contido no valor de um determinado
 * atributo. Deverá ser utilizado asterisco, a fim de especificar se o texto
 * informado deve corresponder à parte inicial, final ou se deve estar contido
 * no conteúdo pesquisado. Exemplos: pessoa.nome like Pedro* (nome deve começar
 * com Pedro) ou pessoa.nome like \*Pedro (nome deve terminar com Pedro) ou
 * pessoa.nome_ like *Pedro* (nome deve conter o valor Pedro);
 * not : operador de negação. Deve ser utilizado em conjunto com os operadores
 * in (not-in), like (not-like) e eq (ne ou !=).
 */

public enum SearchOperation {
    EQUAL,
    EQUAL_IGNORE_CASE,
    LESS_THAN,
    GREATER_THEN,
    LESS_OR_EQUAL,
    GREATER_OR_EQUAL,
    IN,
    CONTAINS,
    NOT,
    ORDER,
    IS_NULL,
    IS_NOT_NULL;

    public static SearchOperation getSearchOpearation(String token) {
        String unformatedToken = token.toLowerCase().replaceAll("\\s+", "");
        if (unformatedToken.equals("==") || unformatedToken.equals("eq")) {
            return EQUAL;
        }
        if (unformatedToken.equals("lt") || unformatedToken.equals("<")) {
            return LESS_THAN;
        }
        if (unformatedToken.equals("gt") || unformatedToken.equals(">")) {
            return GREATER_THEN;
        }
        if (unformatedToken.equals("le") || unformatedToken.equals("<=")) {
            return LESS_OR_EQUAL;
        }
        if (unformatedToken.equals("ge") || unformatedToken.equals(">=")) {

            return GREATER_OR_EQUAL;
        }
        if (unformatedToken.equals("in")) {
            return IN;
        }
        if (unformatedToken.equals("contains")) {
            return CONTAINS;
        }
        if (unformatedToken.equals("not")) {
            return NOT;
        }
        if (unformatedToken.equals("order")) {
            return ORDER;
        }
        if(unformatedToken.equals("eqic")){
            return EQUAL_IGNORE_CASE;
        }
        return null;
    }
}
