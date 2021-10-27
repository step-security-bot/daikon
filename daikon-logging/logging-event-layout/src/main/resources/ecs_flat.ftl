package org.talend.daikon.logging.ecs;

<#assign className = output.file.name?keep_before(".")>
<#assign fields = model.content>

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
* Enumeration of all ECS fields based on ecs_flat.yaml file
*/
public enum ${className} {
<#list fields?keys as key>
    /**
    * ${fields[key].short}
    * Type: ${fields[key].type}
    <#if fields[key].example??>
        <#if fields[key].example?is_date>
    * Example: ${fields[key].example?datetime_if_unknown?iso_utc}
        <#elseif fields[key].example?is_number>
    * Example: ${fields[key].example?c}
        <#else>
    <#-- Remove all non ASCII characters -->
    * Example: ${fields[key].example?replace("[^\\p{ASCII}]", "", "r")}
        </#if>
    </#if>
    */
    <#assign numericTypes = ["byte", "short", "integer", "float", "double", "long"]>
    <#assign isNumber = numericTypes?seq_contains(fields[key].type)?string("true", "false")>
    ${fields[key].dashed_name?replace('-', '_')?upper_case}("${key}", ${isNumber})<#if key?is_last>;<#else>,</#if>
</#list>

    private static final Map<String, ${className}> FIELDS = Arrays.stream(${className}.values())
            .collect(Collectors.toMap(it -> it.fieldName, it -> it));

    public final String fieldName;

    public final boolean isNumber;

    private ${className}(String fieldName, boolean isNumber) {
        this.fieldName = fieldName;
        this.isNumber = isNumber;
    }

    public static boolean containsName(String fieldName) {
        return FIELDS.containsKey(fieldName.toLowerCase(Locale.ROOT));
    }

    public static ${className} getByName(String fieldName) {
        return FIELDS.get(fieldName.toLowerCase(Locale.ROOT));
    }

    public static boolean isNumber(String fieldName) {
        return FIELDS.containsKey(fieldName.toLowerCase(Locale.ROOT))
            && FIELDS.get(fieldName.toLowerCase(Locale.ROOT)).isNumber;
    }
}
