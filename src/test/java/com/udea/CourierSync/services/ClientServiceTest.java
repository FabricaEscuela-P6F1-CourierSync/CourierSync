package com.udea.CourierSync.services;

import com.udea.CourierSync.DTO.ClientDTO;
import com.udea.CourierSync.entity.Client;
import com.udea.CourierSync.exception.BadRequestException;
import com.udea.CourierSync.exception.ResourceNotFoundException;
import com.udea.CourierSync.mapper.ClientMapper;
import com.udea.CourierSync.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

  @Mock
  private ClientRepository clientRepository;

  @Mock
  private ClientMapper clientMapper;

  @InjectMocks
  private ClientService clientService;

  private Client client;
  private ClientDTO clientDTO;

  @BeforeEach
  void setUp() {
    client = new Client();
    client.setId(1L);
    client.setName("John Doe");
    client.setEmail("john@example.com");
    client.setPhone("1234567890");
    client.setAddress("123 Main St");

    clientDTO = new ClientDTO();
    clientDTO.setId(1L);
    clientDTO.setName("John Doe");
    clientDTO.setEmail("john@example.com");
    clientDTO.setPhone("1234567890");
    clientDTO.setAddress("123 Main St");
  }

  @Test
  void testCreateClient_Success() {
    when(clientMapper.toEntity(clientDTO)).thenReturn(client);
    when(clientRepository.save(any(Client.class))).thenReturn(client);
    when(clientMapper.toDTO(client)).thenReturn(clientDTO);

    ClientDTO result = clientService.createClient(clientDTO);

    assertNotNull(result);
    assertEquals(clientDTO.getId(), result.getId());
    assertEquals(clientDTO.getName(), result.getName());
    verify(clientRepository, times(1)).save(any(Client.class));
  }

  @Test
  void testCreateClient_NullDTO_ThrowsException() {
    assertThrows(BadRequestException.class, () -> {
      clientService.createClient(null);
    });
    verify(clientRepository, never()).save(any(Client.class));
  }

  @Test
  void testFindById_Success() {
    when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
    when(clientMapper.toDTO(client)).thenReturn(clientDTO);

    Optional<ClientDTO> result = clientService.findById(1L);

    assertTrue(result.isPresent());
    assertEquals(clientDTO.getId(), result.get().getId());
  }

  @Test
  void testFindById_NotFound() {
    when(clientRepository.findById(999L)).thenReturn(Optional.empty());

    Optional<ClientDTO> result = clientService.findById(999L);

    assertFalse(result.isPresent());
  }

  @Test
  void testFindAll_Success() {
    Client client2 = new Client();
    client2.setId(2L);
    client2.setName("Jane Doe");

    ClientDTO clientDTO2 = new ClientDTO();
    clientDTO2.setId(2L);
    clientDTO2.setName("Jane Doe");

    when(clientRepository.findAll()).thenReturn(Arrays.asList(client, client2));
    when(clientMapper.toDTO(client)).thenReturn(clientDTO);
    when(clientMapper.toDTO(client2)).thenReturn(clientDTO2);

    List<ClientDTO> result = clientService.findAll();

    assertEquals(2, result.size());
    verify(clientRepository, times(1)).findAll();
  }

  @Test
  void testUpdate_Success() {
    ClientDTO updatedDTO = new ClientDTO();
    updatedDTO.setName("John Updated");
    updatedDTO.setEmail("john.updated@example.com");

    when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
    when(clientMapper.toEntity(updatedDTO)).thenReturn(client);
    when(clientRepository.save(any(Client.class))).thenReturn(client);
    when(clientMapper.toDTO(client)).thenReturn(updatedDTO);

    ClientDTO result = clientService.update(1L, updatedDTO);

    assertNotNull(result);
    verify(clientRepository, times(1)).findById(1L);
    verify(clientRepository, times(1)).save(any(Client.class));
  }

  @Test
  void testUpdate_NotFound_ThrowsException() {
    ClientDTO updatedDTO = new ClientDTO();
    when(clientRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> {
      clientService.update(999L, updatedDTO);
    });
  }

  @Test
  void testUpdate_NullDTO_ThrowsException() {
    assertThrows(BadRequestException.class, () -> {
      clientService.update(1L, null);
    });
  }

  @Test
  void testDeleteById_Success() {
    when(clientRepository.existsById(1L)).thenReturn(true);
    doNothing().when(clientRepository).deleteById(1L);

    assertDoesNotThrow(() -> {
      clientService.deleteById(1L);
    });

    verify(clientRepository, times(1)).deleteById(1L);
  }

  @Test
  void testDeleteById_NotFound_ThrowsException() {
    when(clientRepository.existsById(999L)).thenReturn(false);

    assertThrows(ResourceNotFoundException.class, () -> {
      clientService.deleteById(999L);
    });
  }
}
