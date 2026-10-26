package ru.mirea.movingcompany.abdykalykov.repositories;

import ru.mirea.movingcompany.abdykalykov.enums.ClientStatus;
import ru.mirea.movingcompany.abdykalykov.models.ClientModel;

import java.util.List;
import java.util.Optional;

public interface ClientRepository {
    List<ClientModel> getAllClients();
    Optional<ClientModel> getClientById(int id);
    List<ClientModel> getClientsByStatus(ClientStatus status);
    Optional<ClientModel> getClientByPhone(String phone);
    List<ClientModel> getClientsByName(String name);

    boolean createClient(ClientModel client);
    ClientModel updateClient(ClientModel client);

    ClientModel deleteClientById(int id);
}
