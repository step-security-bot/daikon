package org.talend.daikon.spring.ccf.context;

import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.talend.daikon.spring.ccf.context.exception.CcfContextTenantIdNotFoundException;
import org.talend.daikon.spring.ccf.context.utils.ScimUtilities;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class M2MFunctionalContextAspectTest {

    private static final String TENANT_ID = UUID.randomUUID().toString();
    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String PATH_WITH_TENANT = "/my/service/tenant/" + TENANT_ID + "/resource/resourceId";

    private static final String PATH_WITH_TENANT_AND_USER =
            "/my/service/tenant/" + TENANT_ID + "/resource/resourceId" + "/user/" + USER_ID;

    private static final String PATH_WITHOUT_TENANT = "/my/service/resource/resourceId";
    private static final MockedStatic<AttributeProvider> attributeProviderMocked = Mockito.mockStatic(AttributeProvider.class);
    @Mock
    private M2MContextManager m2MContextManager;
    @Mock
    private TenantParameterExtractor tenantParameterExtractor;
    private TenantParameterExtractorImpl tenantParameterExtractorImpl = new TenantParameterExtractorImpl();
    @Mock
    private ScimUtilities scimUtilities;
    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;
    @Mock
    private MethodSignature signature;
    private Method methodSignature;
    @Mock
    private M2MFunctionalContext m2MFunctionalContextAnnotation;
    @Mock
    private ServletRequestAttributes attrs;
    @Mock
    private HttpServletRequest request;
    @InjectMocks
    private M2MFunctionalContextAspect m2MFunctionalContextAspect;

    @BeforeEach
    void setUp() {
        when(attrs.getRequest()).thenReturn(request);
        RequestContextHolder.setRequestAttributes(attrs);
        when(tenantParameterExtractor.extractAccountId(anyString())).then(
                (i) -> tenantParameterExtractorImpl.extractAccountId(i.getArgument(0)));
    }

    @Test
    void whenTenantIdInPathContextPopulated() throws Throwable {
        changeScenario(List.of(UserContextConstant.NONE.getValue()), PATH_WITH_TENANT, true);

        m2MFunctionalContextAspect.buildTenantContext(proceedingJoinPoint);
        verify(scimUtilities, times(0)).getUserWithAttributes(any(), any());
        verify(m2MContextManager).injectContext(eq(TENANT_ID), isNull(), eq(Optional.empty()));
        verify(m2MContextManager).clearContext();
    }

    @Test
    void whenTenantIdNotInPathThrowError() throws Throwable {
        changeScenario(List.of(UserContextConstant.NONE.getValue()), PATH_WITHOUT_TENANT, false);

        Assertions.assertThrows(CcfContextTenantIdNotFoundException.class,
                () -> m2MFunctionalContextAspect.buildTenantContext(proceedingJoinPoint));

    }


    @Test
    void whenTenantIdAndUserIdInPathContextPopulated() throws Throwable {
        changeScenario(List.of(UserContextConstant.GROUPS.getValue()), PATH_WITH_TENANT_AND_USER, true);

        m2MFunctionalContextAspect.buildTenantContext(proceedingJoinPoint);
        verify(scimUtilities, times(1)).getUserWithAttributes(any(), any());
        verify(m2MContextManager).injectContext(eq(TENANT_ID), eq(USER_ID), eq(Optional.empty()));
        verify(m2MContextManager).clearContext();
    }

    private void changeScenario(List<String> userConstants, String uri, boolean mockUserId) {
        attributeProviderMocked.when(() -> AttributeProvider.getAttributes(any()))
                .thenReturn(userConstants);
        if (mockUserId) {
            when(tenantParameterExtractor.extractUserId(anyString())).then(
                    (i) -> tenantParameterExtractorImpl.extractUserId(i.getArgument(0)));
        }
        when(request.getRequestURI()).thenReturn(uri);
    }


}
