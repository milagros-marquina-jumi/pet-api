package com.marquina.pet_api;

import com.marquina.pet_api.config.PetStoreProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Recorre controller, service y client juntos. Solo se sustituye la salida de
 * red, de modo que el cableado real entre capas queda verificado.
 */
@SpringBootTest
class PetApiIntegrationTest {

    private static final String PET_BY_ID_URL = "/api/pet/{petId}";
    private static final String PET_URL = "/api/pet";
    private static final Long PET_ID = 10000023L;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RestTemplate petStoreRestTemplate;

    @Autowired
    private PetStoreProperties properties;

    private MockMvc mockMvc;
    private MockRestServiceServer upstream;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        upstream = MockRestServiceServer.createServer(petStoreRestTemplate);
    }

    @Test
    void reducesUpstreamPayloadToContractFieldsEndToEnd() throws Exception {
        upstream.expect(requestTo(properties.baseUrl() + "/pet/" + PET_ID))
                .andRespond(withSuccess("""
                        {"id":10000023,"category":{"id":1,"name":"dogs"},"name":"testingPet1",
                         "photoUrls":["http://photo.test/1.jpg"],"tags":[{"id":1,"name":"tag"}],
                         "status":"available"}
                        """, MediaType.APPLICATION_JSON));

        mockMvc.perform(get(PET_BY_ID_URL, PET_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PET_ID))
                .andExpect(jsonPath("$.name").value("testingPet1"))
                .andExpect(jsonPath("$.status").value("available"))
                .andExpect(jsonPath("$.category").doesNotExist())
                .andExpect(jsonPath("$.photoUrls").doesNotExist())
                .andExpect(jsonPath("$.tags").doesNotExist());

        upstream.verify();
    }

    @Test
    void returnsNotFoundWhenUpstreamHasNoPet() throws Exception {
        upstream.expect(requestTo(properties.baseUrl() + "/pet/999999999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":1,\"type\":\"error\",\"message\":\"Pet not found\"}"));

        mockMvc.perform(get(PET_BY_ID_URL, 999999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        upstream.verify();
    }

    @Test
    void returnsBadGatewayWhenUpstreamFails() throws Exception {
        upstream.expect(requestTo(properties.baseUrl() + "/pet/" + PET_ID))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(get(PET_BY_ID_URL, PET_ID))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502));

        upstream.verify();
    }

    @Test
    void buildsTransactionResponseAndPropagatesCorrelationHeader() throws Exception {
        upstream.expect(requestTo(properties.baseUrl() + "/pet"))
                .andExpect(header("X-Request-ID", org.hamcrest.Matchers.matchesPattern(
                        "[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")))
                .andRespond(withSuccess("""
                        {"id":10000023,"name":"testingPet1","photoUrls":[],"tags":[],
                         "status":"available"}
                        """, MediaType.APPLICATION_JSON));

        mockMvc.perform(post(PET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":10000023,"status":"available","name":"testingPet1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.dateCreated").exists())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.name").value("testingPet1"));

        upstream.verify();
    }

    @Test
    void rejectsInvalidBodyBeforeReachingUpstream() throws Exception {
        mockMvc.perform(post(PET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":-1,"status":"invalido","name":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());

        // Sin llamadas pendientes: la validacion corta antes de salir a la red.
        upstream.verify();
    }
}
