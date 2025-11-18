package com.udea.CourierSync.services;

import com.udea.CourierSync.DTO.ClientDTO;
import com.udea.CourierSync.DTO.ShipmentDTO;
import com.udea.CourierSync.entity.Client;
import com.udea.CourierSync.entity.Shipment;
import com.udea.CourierSync.enums.ShipmentPriority;
import com.udea.CourierSync.enums.ShipmentStatus;
import com.udea.CourierSync.exception.BadRequestException;
import com.udea.CourierSync.exception.ResourceNotFoundException;
import com.udea.CourierSync.mapper.ShipmentMapper;
import com.udea.CourierSync.repository.ClientRepository;
import com.udea.CourierSync.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

  @Mock
  private ShipmentRepository shipmentRepository;

  @Mock
  private ClientRepository clientRepository;

  @Mock
  private ShipmentMapper shipmentMapper;

  @InjectMocks
  private ShipmentService shipmentService;

  private Shipment shipment;
  private ShipmentDTO shipmentDTO;
  private Client client;

  @BeforeEach
  void setUp() {
    client = new Client();
    client.setId(1L);
    client.setName("Test Client");
    client.setEmail("client@example.com");

    shipment = new Shipment();
    shipment.setId(1L);
    shipment.setTrackingCode("CS1234567");
    shipment.setClient(client);
    shipment.setStatus(ShipmentStatus.PENDIENTE);
    shipment.setPriority(ShipmentPriority.MEDIA);

    ClientDTO clientDTO = new ClientDTO();
    clientDTO.setId(1L);
    clientDTO.setName("Test Client");

    shipmentDTO = new ShipmentDTO();
    shipmentDTO.setId(1L);
    shipmentDTO.setTrackingCode("CS1234567");
    shipmentDTO.setClient(clientDTO);
    shipmentDTO.setStatus(ShipmentStatus.PENDIENTE);
    shipmentDTO.setPriority(ShipmentPriority.MEDIA);
  }

  @Test
  void testCreateShipment_Success() {
    when(shipmentMapper.toEntity(shipmentDTO)).thenReturn(shipment);
    when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
    when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);
    when(shipmentMapper.toDTO(shipment)).thenReturn(shipmentDTO);

    ShipmentDTO result = shipmentService.createShipment(shipmentDTO);

    assertNotNull(result);
    assertNotNull(result.getTrackingCode());
    assertTrue(result.getTrackingCode().startsWith("CS"));
    verify(shipmentRepository, times(1)).save(any(Shipment.class));
  }

  @Test
  void testCreateShipment_NullDTO_ThrowsException() {
    assertThrows(BadRequestException.class, () -> {
      shipmentService.createShipment(null);
    });
    verify(shipmentRepository, never()).save(any(Shipment.class));
  }

  @Test
  void testCreateShipment_ClientNotFound_ThrowsException() {
    when(shipmentMapper.toEntity(shipmentDTO)).thenReturn(shipment);
    when(clientRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> {
      shipmentService.createShipment(shipmentDTO);
    });
  }

  @Test
  void testFindById_Success() {
    when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
    when(shipmentMapper.toDTO(shipment)).thenReturn(shipmentDTO);

    Optional<ShipmentDTO> result = shipmentService.findById(1L);

    assertTrue(result.isPresent());
    assertEquals(shipmentDTO.getId(), result.get().getId());
  }

  @Test
  void testUpdate_Success() {
    ShipmentDTO updatedDTO = new ShipmentDTO();
    updatedDTO.setStatus(ShipmentStatus.EN_TRANSITO);

    when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
    when(shipmentMapper.toEntity(updatedDTO)).thenReturn(shipment);
    when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);
    when(shipmentMapper.toDTO(shipment)).thenReturn(updatedDTO);

    ShipmentDTO result = shipmentService.update(1L, updatedDTO);

    assertNotNull(result);
    verify(shipmentRepository, times(1)).findById(1L);
    verify(shipmentRepository, times(1)).save(any(Shipment.class));
  }

  @Test
  void testUpdate_NotFound_ThrowsException() {
    ShipmentDTO updatedDTO = new ShipmentDTO();
    when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> {
      shipmentService.update(999L, updatedDTO);
    });
  }

  @Test
  void testIsShipmentPending_True() {
    shipment.setStatus(ShipmentStatus.PENDIENTE);
    when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

    boolean result = shipmentService.isShipmentPending(1L);

    assertTrue(result);
  }

  @Test
  void testIsShipmentPending_False() {
    shipment.setStatus(ShipmentStatus.EN_TRANSITO);
    when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

    boolean result = shipmentService.isShipmentPending(1L);

    assertFalse(result);
  }

  @Test
  void testCanDriverUpdateStatus_ValidTransition() {
    shipment.setStatus(ShipmentStatus.PENDIENTE);
    shipmentDTO.setStatus(ShipmentStatus.EN_TRANSITO);

    when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

    boolean result = shipmentService.canDriverUpdateStatus(1L, shipmentDTO);

    assertTrue(result);
  }

  @Test
  void testCanDriverUpdateStatus_InvalidTransition() {
    shipment.setStatus(ShipmentStatus.ENTREGADO);
    shipmentDTO.setStatus(ShipmentStatus.PENDIENTE);

    when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

    boolean result = shipmentService.canDriverUpdateStatus(1L, shipmentDTO);

    assertFalse(result);
  }

  @Test
  void testDeleteById_Success() {
    when(shipmentRepository.existsById(1L)).thenReturn(true);
    doNothing().when(shipmentRepository).deleteById(1L);

    assertDoesNotThrow(() -> {
      shipmentService.deleteById(1L);
    });

    verify(shipmentRepository, times(1)).deleteById(1L);
  }

  @Test
  void testDeleteById_NotFound_ThrowsException() {
    when(shipmentRepository.existsById(999L)).thenReturn(false);

    assertThrows(ResourceNotFoundException.class, () -> {
      shipmentService.deleteById(999L);
    });
  }
}
