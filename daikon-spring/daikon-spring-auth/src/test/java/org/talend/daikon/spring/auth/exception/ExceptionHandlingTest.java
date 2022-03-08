package org.talend.daikon.spring.auth.exception;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(classes = { ExceptionHandlingTestApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class ExceptionHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = "VIEW")
    public void testGetAuthenticatedSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/auth/test")).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("auth-result"));
    }

    @Test
    public void testGetNotAuthenticatedSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/no-auth/test")).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("no-auth-result"));
    }

    @Test
    @WithAnonymousUser
    public void testGetAnonymousUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/auth/test")).andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().string("{\"status\":401,\"title\":\"Unauthorized\"}"));
    }

    @Test
    public void testGetUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/auth/test")).andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().string("{\"status\":401,\"title\":\"Unauthorized\"}"));
    }

    @Test
    @WithMockUser(authorities = "INSUFFICIENT")
    public void testGetForbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/auth/test")).andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().string("{\"detail\":\"Access is denied\",\"status\":403}"));
    }

}
