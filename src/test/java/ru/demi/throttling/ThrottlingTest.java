package ru.demi.throttling;

import ru.demi.throttling.service.SomeService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import ru.demi.throttling.throttling.ThrottlingException;

import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ThrottlingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SomeService someService;

    private static final int limit = 50;

    @After
    public void afterTest() throws InterruptedException {
        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    public void shouldReturnSuccessIfCallControllerMethod() throws Exception {
        for (int i = 1; i <= limit - 1; ++i) {
            this.mockMvc.perform(get("/test"))
                .andExpect(status().isOk());
        }
    }

    @Test
    public void shouldReturnSuccessIfCallsOfControllerMethodIsMoreAndAfterPauseIsNormal() throws Exception {
        RequestPostProcessor postProcessor1 = request -> {
            request.setRemoteAddr("1.1.0.1");
            return request;
        };

        RequestPostProcessor postProcessor2 = request -> {
            request.setRemoteAddr("1.1.0.2");
            return request;
        };

        for (int i = 1; i <= limit - 1; ++i) {
            this.mockMvc.perform(get("/test").with(postProcessor1))
                .andExpect(status().isOk());
        }

        this.mockMvc.perform(get("/test").with(postProcessor1))
            .andExpect(status().isBadGateway());

        for (int i = 1; i <= limit - 1; ++i) {
            this.mockMvc.perform(get("/test").with(postProcessor2))
                .andExpect(status().isOk());
        }

        this.mockMvc.perform(get("/test").with(postProcessor2))
            .andExpect(status().isBadGateway());

        TimeUnit.SECONDS.sleep(5);
        this.mockMvc.perform(get("/test").with(postProcessor1))
            .andExpect(status().isOk());
        this.mockMvc.perform(get("/test").with(postProcessor2))
            .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnErrorIfCallsOfControllerMethodIsMoreThanAllowed() throws Exception {
        for (int i = 1; i <= limit - 1; ++i) {
            this.mockMvc.perform(get("/test"))
                .andExpect(status().isOk());
        }

        this.mockMvc.perform(get("/test"))
            .andExpect(status().isBadGateway());
    }

    @Test
    public void shouldCallServiceMethodSuccessfully() {
        for (int i = 1; i <= limit - 1; ++i) {
            this.someService.method();
        }
    }

    @Test
    public void shouldCallServiceMethodSuccessfullyAfterPause() throws Exception {
        for (int i = 1; i <= limit - 1; ++i) {
            this.someService.method();
        }

        try {
            this.someService.method();
            Assert.fail();
        } catch (ThrottlingException e) {
        }

        TimeUnit.SECONDS.sleep(5);
        this.someService.method();
    }

    @Test(expected = ThrottlingException.class)
    public void shouldThrowExceptionIfCallsOfServiceMethodIsMoreThanAllowed() {
        for (int i = 1; i <= limit - 1; ++i) {
            this.someService.method();
        }

        this.someService.method();
    }
}