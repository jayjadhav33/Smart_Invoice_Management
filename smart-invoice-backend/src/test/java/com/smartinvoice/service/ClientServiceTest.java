package com.smartinvoice.service;

import com.smartinvoice.dto.ClientDTO;
import com.smartinvoice.entity.Client;
import com.smartinvoice.exception.DuplicateResourceException;
import com.smartinvoice.exception.ResourceNotFoundException;
import com.smartinvoice.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Client Service Tests")
class ClientServiceTest {

    // Create mock of repository
    @Mock
    private ClientRepository clientRepository;

    // Inject mocks into service
    @InjectMocks
    private ClientService clientService;

    // Test data
    private Client client1;
    private Client client2;
    private ClientDTO clientDTO;

    // Runs before each test method
    @BeforeEach
    void setUp() {
        client1 = new Client();
        client1.setId(1L);
        client1.setName("Raj Enterprises");
        client1.setEmail("raj@rajenterprises.com");
        client1.setPhone("9876543210");
        client1.setAddress("Pune, Maharashtra");
        client1.setGstNumber("27AAPFU0939F1ZV");

        client2 = new Client();
        client2.setId(2L);
        client2.setName("Tech Solutions");
        client2.setEmail("tech@solutions.com");
        client2.setPhone("9123456789");

        clientDTO = new ClientDTO();
        clientDTO.setName("New Client");
        clientDTO.setEmail("new@client.com");
        clientDTO.setPhone("9000000000");
        clientDTO.setAddress("Mumbai");
        clientDTO.setGstNumber("GST123");
    }

    // ─────────────────────────────────────────────
    // TEST 1: Get All Clients — Happy Path
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("Should return all clients successfully")
    void testGetAllClients_Success() {

        // ARRANGE: mock repository to return 2 clients
        when(clientRepository.findAll())
                .thenReturn(Arrays.asList(client1, client2));

        // ACT: call the service
        List<ClientDTO> result =
                clientService.getAllClients();

        // ASSERT: verify results
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Raj Enterprises",
                result.get(0).getName());
        assertEquals("Tech Solutions",
                result.get(1).getName());

        // Verify repository was called exactly once
        verify(clientRepository, times(1)).findAll();
    }

    // ─────────────────────────────────────────────
    // TEST 2: Get Client By ID — Happy Path
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("Should return client by ID")
    void testGetClientById_Success() {

        // ARRANGE
        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(client1));

        // ACT
        ClientDTO result =
                clientService.getClientById(1L);

        // ASSERT
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Raj Enterprises", result.getName());
        assertEquals("raj@rajenterprises.com",
                result.getEmail());

        verify(clientRepository, times(1)).findById(1L);
    }

    // ─────────────────────────────────────────────
    // TEST 3: Get Client By ID — Not Found
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("Should throw exception when client not found")
    void testGetClientById_NotFound() {

        // ARRANGE: mock returns empty
        when(clientRepository.findById(999L))
                .thenReturn(Optional.empty());

        // ACT + ASSERT: expect exception
        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> clientService
                                .getClientById(999L));

        assertEquals(
                "Client not found with id: 999",
                exception.getMessage());

        verify(clientRepository, times(1))
                .findById(999L);
    }

    // ─────────────────────────────────────────────
    // TEST 4: Create Client — Happy Path
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("Should create client successfully")
    void testCreateClient_Success() {

        // ARRANGE
        when(clientRepository.existsByEmail(anyString()))
                .thenReturn(false);

        Client savedClient = new Client();
        savedClient.setId(3L);
        savedClient.setName(clientDTO.getName());
        savedClient.setEmail(clientDTO.getEmail());
        savedClient.setPhone(clientDTO.getPhone());
        savedClient.setAddress(clientDTO.getAddress());
        savedClient.setGstNumber(clientDTO.getGstNumber());

        when(clientRepository.save(any(Client.class)))
                .thenReturn(savedClient);

        // ACT
        ClientDTO result =
                clientService.createClient(clientDTO);

        // ASSERT
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("New Client", result.getName());
        assertEquals("new@client.com", result.getEmail());

        verify(clientRepository, times(1))
                .existsByEmail("new@client.com");
        verify(clientRepository, times(1))
                .save(any(Client.class));
    }

    // ─────────────────────────────────────────────
    // TEST 5: Create Client — Duplicate Email
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("Should throw exception for duplicate email")
    void testCreateClient_DuplicateEmail() {

        // ARRANGE: email already exists
        when(clientRepository.existsByEmail(anyString()))
                .thenReturn(true);

        // ACT + ASSERT
        DuplicateResourceException exception =
                assertThrows(
                        DuplicateResourceException.class,
                        () -> clientService
                                .createClient(clientDTO));

        assertTrue(exception.getMessage()
                .contains("already exists"));

        // Save should NEVER be called
        verify(clientRepository, never())
                .save(any(Client.class));
    }


    // ─────────────────────────────────────────────
    // TEST 6: Delete Client — Happy Path
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("Should delete client successfully")
    void testDeleteClient_Success() {

        // ARRANGE
        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(client1));
        doNothing().when(clientRepository)
                .delete(any(Client.class));

        // ACT
        assertDoesNotThrow(() ->
                clientService.deleteClient(1L));

        // ASSERT
        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, times(1))
                .delete(client1);
    }

    // ─────────────────────────────────────────────
    // TEST 7: Delete Client — Not Found
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("Should throw exception when deleting non-existent client")
    void testDeleteClient_NotFound() {

        // ARRANGE
        when(clientRepository.findById(999L))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(
                ResourceNotFoundException.class,
                () -> clientService.deleteClient(999L));

        // Delete should NEVER be called
        verify(clientRepository, never())
                .delete(any(Client.class));
    }
}