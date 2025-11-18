package com.udea.CourierSync.services;

import com.udea.CourierSync.DTO.VehicleDTO;
import com.udea.CourierSync.entity.Vehicle;
import com.udea.CourierSync.exception.BadRequestException;
import com.udea.CourierSync.exception.ResourceNotFoundException;
import com.udea.CourierSync.mapper.VehicleMapper;
import com.udea.CourierSync.repository.VehicleRepository;
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
class VehicleServiceTest {

  @Mock
  private VehicleRepository vehicleRepository;

  @Mock
  private VehicleMapper vehicleMapper;

  @InjectMocks
  private VehicleService vehicleService;

  private Vehicle vehicle;
  private VehicleDTO vehicleDTO;

  @BeforeEach
  void setUp() {
    vehicle = new Vehicle();
    vehicle.setId(1L);
    vehicle.setPlate("ABC123");
    vehicle.setModel("Toyota Corolla");
    vehicle.setMaximumCapacity(500.0);
    vehicle.setAvailable(true);

    vehicleDTO = new VehicleDTO();
    vehicleDTO.setId(1L);
    vehicleDTO.setPlate("ABC123");
    vehicleDTO.setModel("Toyota Corolla");
    vehicleDTO.setMaximumCapacity(500.0);
    vehicleDTO.setAvailable(true);
  }

  @Test
  void testCreateVehicle_Success() {
    when(vehicleMapper.toEntity(vehicleDTO)).thenReturn(vehicle);
    when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
    when(vehicleMapper.toDTO(vehicle)).thenReturn(vehicleDTO);

    VehicleDTO result = vehicleService.createVehicle(vehicleDTO);

    assertNotNull(result);
    assertEquals(vehicleDTO.getPlate(), result.getPlate());
    verify(vehicleRepository, times(1)).save(any(Vehicle.class));
  }

  @Test
  void testCreateVehicle_NullDTO_ThrowsException() {
    assertThrows(BadRequestException.class, () -> {
      vehicleService.createVehicle(null);
    });
    verify(vehicleRepository, never()).save(any(Vehicle.class));
  }

  @Test
  void testFindById_Success() {
    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
    when(vehicleMapper.toDTO(vehicle)).thenReturn(vehicleDTO);

    Optional<VehicleDTO> result = vehicleService.findById(1L);

    assertTrue(result.isPresent());
    assertEquals(vehicleDTO.getId(), result.get().getId());
  }

  @Test
  void testFindByPlate_Success() {
    when(vehicleRepository.findByPlate("ABC123")).thenReturn(Optional.of(vehicle));
    when(vehicleMapper.toDTO(vehicle)).thenReturn(vehicleDTO);

    Optional<VehicleDTO> result = vehicleService.findByPlate("ABC123");

    assertTrue(result.isPresent());
    assertEquals(vehicleDTO.getPlate(), result.get().getPlate());
  }

  @Test
  void testFindAll_Success() {
    Vehicle vehicle2 = new Vehicle();
    vehicle2.setId(2L);
    vehicle2.setPlate("XYZ789");

    VehicleDTO vehicleDTO2 = new VehicleDTO();
    vehicleDTO2.setId(2L);
    vehicleDTO2.setPlate("XYZ789");

    when(vehicleRepository.findAll()).thenReturn(Arrays.asList(vehicle, vehicle2));
    when(vehicleMapper.toDTO(vehicle)).thenReturn(vehicleDTO);
    when(vehicleMapper.toDTO(vehicle2)).thenReturn(vehicleDTO2);

    List<VehicleDTO> result = vehicleService.findAll();

    assertEquals(2, result.size());
    verify(vehicleRepository, times(1)).findAll();
  }

  @Test
  void testUpdate_Success() {
    VehicleDTO updatedDTO = new VehicleDTO();
    updatedDTO.setModel("Honda Civic");
    updatedDTO.setAvailable(false);

    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
    when(vehicleMapper.toEntity(updatedDTO)).thenReturn(vehicle);
    when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
    when(vehicleMapper.toDTO(vehicle)).thenReturn(updatedDTO);

    VehicleDTO result = vehicleService.update(1L, updatedDTO);

    assertNotNull(result);
    verify(vehicleRepository, times(1)).findById(1L);
    verify(vehicleRepository, times(1)).save(any(Vehicle.class));
  }

  @Test
  void testUpdate_NotFound_ThrowsException() {
    VehicleDTO updatedDTO = new VehicleDTO();
    when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> {
      vehicleService.update(999L, updatedDTO);
    });
  }

  @Test
  void testDeleteById_Success() {
    when(vehicleRepository.existsById(1L)).thenReturn(true);
    doNothing().when(vehicleRepository).deleteById(1L);

    assertDoesNotThrow(() -> {
      vehicleService.deleteById(1L);
    });

    verify(vehicleRepository, times(1)).deleteById(1L);
  }

  @Test
  void testDeleteById_NotFound_ThrowsException() {
    when(vehicleRepository.existsById(999L)).thenReturn(false);

    assertThrows(ResourceNotFoundException.class, () -> {
      vehicleService.deleteById(999L);
    });
  }
}
