package com.example.portal;

import com.example.portal.testutil.ValidatorUtil;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import java.util.Set;

import static com.example.portal.Data.newValidClient;
import static org.assertj.core.api.Assertions.assertThat;

public class ClientTest {

    @Test
    public void givenValidClientWhenValidateThenNoError() {
        Client client = newValidClient();

        Set<ConstraintViolation<Client>> constraintViolations = ValidatorUtil.getValidator().validate(client);

        assertThat(constraintViolations).isEmpty();
    }

    @Test
    public void givenFirstnameEmptyWhenValidateThenError() {
        Client client = newValidClient();
        client.setFirstName("");

        Set<ConstraintViolation<Client>> constraintViolations = ValidatorUtil.getValidator().validate(client);

        assertThat(constraintViolations).hasSize(1);
    }

    @Test
    public void givenLastnameEmptyWhenValidateThenError() {
        Client client = newValidClient();
        client.setLastName("");

        Set<ConstraintViolation<Client>> constraintViolations = ValidatorUtil.getValidator().validate(client);

        assertThat(constraintViolations).hasSize(1);
    }
}
