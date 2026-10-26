package ru.mirea.movingcompany.abdykalykov.services;

import ru.mirea.movingcompany.abdykalykov.enums.ClientStatus;
import ru.mirea.movingcompany.abdykalykov.exceptions.ClientTitleException;
import ru.mirea.movingcompany.abdykalykov.models.ClientModel;
import ru.mirea.movingcompany.abdykalykov.repositories.ClientRepository;

import java.util.List;
import java.util.Optional;

public class ClientService {
    private ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<ClientModel> getAllClients() {
        return clientRepository.getAllClients();
    }

    public Optional<ClientModel> getClientById(int id) {
        return clientRepository.getClientById(id);
    }

    public List<ClientModel> getClientsByStatus(ClientStatus status) {
        return clientRepository.getClientsByStatus(status);
    }

    public Optional<ClientModel> getClientByPhone(String phone) {
        return clientRepository.getClientByPhone(phone);
    }

    public List<ClientModel> getClientsByName(String name) {
        return clientRepository.getClientsByName(name);
    }

    public boolean createClient(ClientModel client) {
        if (client.getName() == null || client.getName().isBlank()) {
            throw new ClientTitleException("Имя клиента не может быть пустым!");
        }
        return clientRepository.createClient(client);
    }

    public ClientModel updateClient(ClientModel client) {
        if (client.getName() == null || client.getName().isBlank()) {
            throw new ClientTitleException("Имя клиента не может быть пустым!");
        }
        return clientRepository.updateClient(client);
    }

    public ClientModel deleteClientById(int id) {
        return clientRepository.deleteClientById(id);
    }

    public ClientModel updateStatus(int id, ClientStatus status) {
        Optional<ClientModel> clientModelOptional = clientRepository.getClientById(id);
        if (clientModelOptional.isEmpty()) {
            return null;
        }
        ClientModel clientModel = clientModelOptional.get();
        clientModel.setStatus(status);
        return this.updateClient(clientModel);
    }
}
