package org.talend.dsel.functions.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import org.talend.dataquality.semantic.api.CategoryRegistryManager;
import org.talend.dataquality.semantic.api.DeletableDictionarySnapshotOpener;
import org.talend.dataquality.semantic.api.SemanticProperties;
import org.talend.dataquality.semantic.snapshot.DeletableDictionarySnapshot;
import org.talend.dsel.exception.FunctionException;
import org.talend.maplang.el.interpreter.api.DselHPathStore;
import org.talend.maplang.el.interpreter.api.ExprInterpreter;
import org.talend.maplang.el.interpreter.api.ExprInterpreterFactory;
import org.talend.maplang.el.interpreter.api.ExprLangContext;
import org.talend.maplang.hpath.HPathStore;

public abstract class FunctionTest {

    static protected ExprInterpreter interpreter;

    static protected ExprLangContext context;

    @TempDir
    static Path tempDir;

    protected static DeletableDictionarySnapshot dictionarySnapshot;

    @BeforeAll
    protected static void init() {
        SemanticProperties properties = new SemanticProperties(tempDir.toString());
        CategoryRegistryManager categoryRegistryManager = new CategoryRegistryManager(properties);
        DeletableDictionarySnapshotOpener opener = new DeletableDictionarySnapshotOpener(properties,
                categoryRegistryManager.getSharedDictionary());
        dictionarySnapshot = opener.openDeletableDictionarySnapshot("fakeTenantId");

        HPathStore store = new DselHPathStore();
        store.put("dictionarySnapshot", dictionarySnapshot);

        context = new ExprLangContext();
        context.setStore(store);

        interpreter = ExprInterpreterFactory.create(context);
    }

    protected void testEvalExpressionThrowsFunctionException(String expression) {
        assertThrows(FunctionException.class, () -> {
            interpreter.setExpression(expression);
            interpreter.eval();
        });
    }

    protected void testEvalExpression(boolean expectedResult, String expression) {
        interpreter.setExpression(expression);
        assertThat(interpreter.evalAsBoolean()).isEqualTo(expectedResult);
    }

}
