package org.talend.dsel.functions.custom;

import static org.talend.dataquality.semantic.model.CategoryType.COMPOUND;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.talend.dataquality.semantic.model.DQCategory;
import org.talend.dataquality.semantic.snapshot.DictionarySnapshot;
import org.talend.dataquality.semantic.statistics.SemanticQualityAnalyzer;
import org.talend.dataquality.statistics.type.TypeInferenceUtils;
import org.talend.dsel.exception.DQCategoryNotFoundException;
import org.talend.dsel.exception.FunctionException;
import org.talend.dsel.model.NativeType;
import org.talend.maplang.el.interpreter.api.ExprLangContext;
import org.talend.maplang.el.interpreter.api.ExprLangFunction;
import org.talend.maplang.hpath.HPathStore;

/**
 * This class is a copy of the original class located at <a href=
 * "https://github.com/Talend/rule-repository/blob/develop/rule-repository-runtime/src/main/java/org/talend/trr/runtime/function/IsOfType.java">IsOfType</a>
 */
public class IsOfType implements ExprLangFunction {

    @Override
    public String getName() {
        return "isOfType";
    }

    @Override
    public Object call(ExprLangContext exprLangContext, Object... params) {
        if (params.length != 2) {
            throw new FunctionException(String.format("Wrong number of parameters (expected: 2, found: %d)", params.length));
        }

        Object value = params[0];
        if (value == null)
            return false;

        String typeName = params[1].toString();

        HPathStore store = exprLangContext.getStore();
        if (store.get(typeName) != null) // the semantic type was a variable
            typeName = store.get(typeName).toString();

        Boolean isSemanticTypeNameAColumn = (Boolean) store.get("semanticTypeNameAsColumn");
        if (isSemanticTypeNameAColumn != null && isSemanticTypeNameAColumn)
            typeName = getDataTypeNameFromLabel(store, typeName);

        try {
            NativeType nativeType = NativeType.valueOf(typeName.toUpperCase());
            return isNativeType(nativeType, value);
        } catch (IllegalArgumentException e) {
            return isSemanticType(exprLangContext, typeName, value.toString(), isSemanticTypeNameAColumn);

        }
    }

    /**
     * Get Semantic Name from Label
     *
     * @param store store containing the dictionary snapshot
     * @param typeLabel label to search
     * @return the corresponding name
     */
    private String getDataTypeNameFromLabel(HPathStore store, String typeLabel) {
        DictionarySnapshot dictionarySnapshot = (DictionarySnapshot) store.get("dictionarySnapshot");
        Optional<DQCategory> category = dictionarySnapshot.getMetadata().values().stream()
                .filter(cat -> cat.getLabel().equals(typeLabel)).findFirst();
        if (category.isPresent())
            return category.get().getName();
        else
            return typeLabel; // it could be a Native type
    }

    @SuppressWarnings({ "unchecked" })
    private boolean isNativeType(NativeType nativeType, Object value) {
        Set<String> possibleTypes = nativeType.getEquivalentJavaTypes();
        String dataType = value.getClass().getSimpleName().toUpperCase();

        if ("STRING".equals(dataType)) {
            dataType = TypeInferenceUtils.getDataType((String) value).name();
        }

        return possibleTypes.contains(dataType);
    }

    private Object isSemanticType(ExprLangContext exprLangContext, String semanticTypeName, String value,
            Boolean isSemanticTypeNameAColumn) {
        HPathStore store = exprLangContext.getStore();
        DictionarySnapshot dictionarySnapshot = (DictionarySnapshot) store.get("dictionarySnapshot");
        DQCategory category = dictionarySnapshot.getDQCategoryByName(semanticTypeName); // category existence is checked
        // in the executor

        if (category == null)
            if (isSemanticTypeNameAColumn != null && isSemanticTypeNameAColumn)
                return null;
            else
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
    private List<DQCategory> completeChildren(DictionarySnapshot dictionarySnapshot, List<DQCategory> children) {
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
