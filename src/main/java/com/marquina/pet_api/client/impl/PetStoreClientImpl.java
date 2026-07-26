package com.marquina.pet_api.client.impl;

import com.marquina.pet_api.client.PetStoreClient;
import com.marquina.pet_api.config.PetStoreProperties;
import com.marquina.pet_api.exception.ExternalApiException;
import com.marquina.pet_api.exception.ExternalApiTimeoutException;
import com.marquina.pet_api.exception.PetNotFoundException;
import com.marquina.pet_api.model.external.PetStorePet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class PetStoreClientImpl implements PetStoreClient {

    private static final Logger log = LoggerFactory.getLogger(PetStoreClientImpl.class);

    private static final String TIMEOUT_MESSAGE = "PetStore no respondió a tiempo";
    private static final String SERVER_ERROR_MESSAGE = "PetStore respondió con un error";
    private static final String TRANSPORT_ERROR_MESSAGE = "No fue posible comunicarse con PetStore";

    private final RestTemplate restTemplate;
    private final PetStoreProperties properties;

    public PetStoreClientImpl(RestTemplate petStoreRestTemplate, PetStoreProperties properties) {
        this.restTemplate = petStoreRestTemplate;
        this.properties = properties;
    }

    @Override
    public PetStorePet findPetById(Long petId) {
        log.debug("Consultando mascota en PetStore | petId={}", petId);
        try {
            return restTemplate.getForObject(properties.petByIdUrl(), PetStorePet.class, petId);
        } catch (HttpClientErrorException.NotFound ex) {
            log.info("PetStore no encontró la mascota | petId={}", petId);
            throw new PetNotFoundException(petId);
        } catch (ResourceAccessException ex) {
            log.warn("Timeout consultando PetStore | petId={}", petId, ex);
            throw new ExternalApiTimeoutException(TIMEOUT_MESSAGE, ex);
        } catch (HttpServerErrorException ex) {
            log.warn("PetStore respondió con error de servidor | petId={} status={}", petId, ex.getStatusCode(), ex);
            throw new ExternalApiException(SERVER_ERROR_MESSAGE, ex);
        } catch (RestClientException ex) {
            log.warn("Fallo de comunicación con PetStore | petId={}", petId, ex);
            throw new ExternalApiException(TRANSPORT_ERROR_MESSAGE, ex);
        }
    }

    @Override
    public PetStorePet createPet(PetStorePet pet) {
        log.debug("Registrando mascota en PetStore | petId={}", pet.id());
        try {
            return restTemplate.postForObject(properties.petUrl(), pet, PetStorePet.class);
        } catch (ResourceAccessException ex) {
            log.warn("Timeout registrando en PetStore | petId={}", pet.id(), ex);
            throw new ExternalApiTimeoutException(TIMEOUT_MESSAGE, ex);
        } catch (HttpServerErrorException ex) {
            log.warn("PetStore respondió con error de servidor | petId={} status={}", pet.id(), ex.getStatusCode(), ex);
            throw new ExternalApiException(SERVER_ERROR_MESSAGE, ex);
        } catch (RestClientException ex) {
            log.warn("Fallo de comunicación con PetStore | petId={}", pet.id(), ex);
            throw new ExternalApiException(TRANSPORT_ERROR_MESSAGE, ex);
        }
    }
}
