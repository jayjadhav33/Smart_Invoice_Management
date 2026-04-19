package com.smartinvoice.service;

import com.smartinvoice.dto.ClientDTO;
import com.smartinvoice.entity.Client;
import com.smartinvoice.exception.DuplicateResourceException;
import com.smartinvoice.exception.ResourceNotFoundException;
import com.smartinvoice.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;

    // ── Get All ─────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ClientDTO> getAllClients() {
        return clientRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Get By ID ───────────────────────────────────
    @Transactional(readOnly = true)
    public ClientDTO getClientById(Long id) {
        return toDTO(findOrThrow(id));
    }

    // ── Create ──────────────────────────────────────
    public ClientDTO createClient(ClientDTO dto) {

        // Check duplicate email
        if (clientRepository.existsByEmail(
                dto.getEmail().trim())) {
            throw new DuplicateResourceException(
                    "A client with email '"
                    + dto.getEmail()
                    + "' already exists");
        }

        // Check duplicate phone
        if (dto.getPhone() != null
                && !dto.getPhone().isBlank()
                && clientRepository.existsByPhone(
                        dto.getPhone().trim())) {
            throw new DuplicateResourceException(
                    "A client with phone '"
                    + dto.getPhone()
                    + "' already exists");
        }

        Client saved = clientRepository.save(
                toEntity(dto));
        return toDTO(saved);
    }

    // ── Update ──────────────────────────────────────
    public ClientDTO updateClient(Long id, ClientDTO dto) {
        Client client = findOrThrow(id);

        // Check duplicate email (only if changed)
        if (!client.getEmail().equalsIgnoreCase(
                dto.getEmail().trim())
                && clientRepository.existsByEmail(
                        dto.getEmail().trim())) {
            throw new DuplicateResourceException(
                    "A client with email '"
                    + dto.getEmail()
                    + "' already exists");
        }

        // Check duplicate phone (only if changed)
        if (dto.getPhone() != null
                && !dto.getPhone().isBlank()
                && !dto.getPhone().trim().equals(
                        client.getPhone())
                && clientRepository.existsByPhone(
                        dto.getPhone().trim())) {
            throw new DuplicateResourceException(
                    "A client with phone '"
                    + dto.getPhone()
                    + "' already exists");
        }

        client.setName(dto.getName().trim());
        client.setEmail(dto.getEmail().trim());
        client.setPhone(dto.getPhone() != null
                ? dto.getPhone().trim() : null);
        client.setAddress(dto.getAddress());
        client.setGstNumber(
                dto.getGstNumber() != null
                && !dto.getGstNumber().isBlank()
                ? dto.getGstNumber().trim().toUpperCase()
                : null);

        return toDTO(clientRepository.save(client));
    }

    // ── Delete ──────────────────────────────────────
    public void deleteClient(Long id) {
        clientRepository.delete(findOrThrow(id));
    }

    // ── Helpers ─────────────────────────────────────
    private Client findOrThrow(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Client not found with id: "
                                + id));
    }

    private ClientDTO toDTO(Client c) {
        ClientDTO dto = new ClientDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setEmail(c.getEmail());
        dto.setPhone(c.getPhone());
        dto.setAddress(c.getAddress());
        dto.setGstNumber(c.getGstNumber());
        return dto;
    }

    private Client toEntity(ClientDTO dto) {
        Client c = new Client();
        c.setName(dto.getName().trim());
        c.setEmail(dto.getEmail().trim());
        c.setPhone(dto.getPhone() != null
                ? dto.getPhone().trim() : null);
        c.setAddress(dto.getAddress());
        c.setGstNumber(
                dto.getGstNumber() != null
                && !dto.getGstNumber().isBlank()
                ? dto.getGstNumber().trim().toUpperCase()
                : null);
        return c;
    }
}