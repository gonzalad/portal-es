package com.example.portal;

public class Data {

    public static Client newValidClient() {
        Client client = new Client();
        client.setFirstName("firstName");
        client.setLastName("lastName");
        return client;
    }
}
