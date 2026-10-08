package com.cybersentinel.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Plain MockMvc (no Spring context, no database) exercising the handler through a throwaway controller. */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProbeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void invalidBodyReturnsValidationErrorWithFieldDetails() throws Exception {
        mockMvc.perform(post("/probe/body").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details[0].field").value("name"));
    }

    @Test
    void malformedJsonReturnsValidationError() throws Exception {
        mockMvc.perform(post("/probe/body").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void missingParameterReturnsValidationError() throws Exception {
        mockMvc.perform(get("/probe/param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details[0].field").value("n"));
    }

    @Test
    void wrongParameterTypeReturnsValidationError() throws Exception {
        mockMvc.perform(get("/probe/param").param("n", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details[0].field").value("n"));
    }

    @Test
    void apiExceptionKeepsItsCodeAndStatus() throws Exception {
        mockMvc.perform(get("/probe/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CONFLICT"))
                .andExpect(jsonPath("$.error.message").value("Already exists"));
    }

    @Test
    void unexpectedExceptionIsGenericAndDoesNotLeakInternals() throws Exception {
        mockMvc.perform(get("/probe/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                .andExpect(content().string(Matchers.not(Matchers.containsString("secret internal detail"))));
    }

    @RestController
    @RequestMapping("/probe")
    public static class ProbeController {

        public record Body(@NotBlank String name) {
        }

        @PostMapping("/body")
        public String body(@Valid @RequestBody Body body) {
            return "ok";
        }

        @GetMapping("/param")
        public String param(@RequestParam("n") int n) {
            return "ok";
        }

        @GetMapping("/conflict")
        public String conflict() {
            throw ApiException.conflict("Already exists");
        }

        @GetMapping("/boom")
        public String boom() {
            throw new IllegalStateException("secret internal detail");
        }
    }
}
