package org.talend.dsel.functions.custom;

import static org.talend.dataquality.semantic.model.CategoryType.COMPOUND;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.EnumUtils;
import org.talend.dataquality.semantic.model.DQCategory;
import org.talend.dataquality.semantic.snapshot.DictionarySnapshot;
import org.talend.dataquality.semantic.statistics.SemanticQualityAnalyzer;
import org.talend.dataquality.statistics.quality.DataTypeQualityAnalyzer;
import org.talend.dataquality.statistics.type.DataTypeEnum;
import org.talend.dsel.exception.DQCategoryNotFoundException;
import org.talend.maplang.el.interpreter.api.ExprLangContext;
import org.talend.maplang.hpath.HPathStore;

/**
 * This class is an utility based on the original class located at <a href=
 * "https://github.com/Talend/rule-repository/blob/develop/rule-repository-runtime/src/main/java/org/talend/trr/runtime/function/IsOfType.java">IsOfType</a>
 */
public class IsOfTypeUtility {

    public static boolean evaluate(ExprLangContext exprLangContext, String value, String typeName) {
        try {
            return isDataType(typeName.toUpperCase(), value);
        } catch (IllegalArgumentException e) {
            return isSemanticType(exprLangContext, typeName, value);
        }
    }

    private static boolean isDataType(String dataType, String value) {
        if (EnumUtils.isValidEnum(DataTypeEnum.class, dataType)) {
            try (DataTypeQualityAnalyzer analyzer = new DataTypeQualityAnalyzer(DataTypeEnum.valueOf(dataType))) {
                analyzer.analyze(value);
                return analyzer.getResult().get(0).getInvalidCount() == 0;
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid DQ dataType " + dataType + " for value " + value);
            }
        } else {
            throw new IllegalArgumentException("Invalid DQ dataType " + dataType);
        }
    }

    private static boolean isSemanticType(ExprLangContext exprLangContext, String semanticTypeName, String value) {
        HPathStore store = exprLangContext.getStore();
        DictionarySnapshot dictionarySnapshot = (DictionarySnapshot) store.get("dictionarySnapshot");
        DQCategory category = dictionarySnapshot.getDQCategoryByName(semanticTypeName); // category existence is checked
        // in the executor

        if (category == null)
            throw new DQCategoryNotFoundException(semanticTypeName);

        if (COMPOUND == category.getType()) { // need to have information on children
            category.setChildren(completeChildren(dictionarySnapshot, category.getChildren()));
        }

        SemanticQualityAnalyzer analyzer = new SemanticQualityAnalyzer(dictionarySnapshot);
        return analyzer.isValid(category, value);
    }

    /**
     * Fill information about children
     *
     * @param dictionarySnapshot index content
     * @param children children metadata
     * @return children fully fill
     */
    private static List<DQCategory> completeChildren(DictionarySnapshot dictionarySnapshot, List<DQCategory> children) {
        List<DQCategory> categories = new ArrayList<>();
        for (DQCategory child : children) {
            DQCategory category = dictionarySnapshot.getDQCategoryById(child.getId());
            switch (category.getType()) {
            case DICT:
            case REGEX:
                categories.add(category);
                break;
            case COMPOUND:
                categories.addAll(completeChildren(dictionarySnapshot, category.getChildren()));
                break;
            default:
                break;
            }
        }
        return categories;
    }
}
