package com.example.portal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;

import static com.example.portal.Data.newValidClient;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ClientServiceTest {

    @Autowired
    private ClientService clientService;

    @MockBean
    private ClientRepository clientRepository;

    @Test
    public void givenInvalidClientWhenSaveThenThrowError() {
        Client client = new Client();

        Throwable thrown = catchThrowable(() -> clientService.save(client));

        assertThat(thrown).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void givenValidClientWhenSaveThenShouldBeSavedInDb() {
        Client client = newValidClient();

        clientService.save(client);

        verify(clientRepository).save(client);
    }
}
