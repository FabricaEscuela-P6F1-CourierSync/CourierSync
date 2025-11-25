package com.udea.CourierSync.services;

import com.udea.CourierSync.repository.ShipmentRepository;
import com.udea.CourierSync.repository.ClientRepository;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.udea.CourierSync.entity.Shipment;
import com.udea.CourierSync.entity.Client;
import com.udea.CourierSync.mapper.ShipmentMapper;
import com.udea.CourierSync.DTO.ShipmentDTO;
import java.util.List;
import java.util.Optional;
import com.udea.CourierSync.exception.BadRequestException;
import com.udea.CourierSync.exception.ResourceNotFoundException;
import com.udea.CourierSync.enums.ShipmentStatus;
import com.udea.CourierSync.entity.StatusHistory;

@Service
public class ShipmentService {
  @Autowired
  private ShipmentRepository shipmentRepository;

  @Autowired
  private ClientRepository clientRepository;

  @Autowired
  private ShipmentMapper shipmentMapper;

  public ShipmentDTO createShipment(ShipmentDTO dto) {
    if (dto == null)
      throw new BadRequestException("ShipmentDTO must not be null");

    Shipment shipment = shipmentMapper.toEntity(dto);

    if (shipment.getClient() == null || shipment.getClient().getId() == null) {
      throw new BadRequestException("Shipment must reference an existing client id");
    }

    Optional<Client> c = clientRepository.findById(shipment.getClient().getId());
    if (c.isEmpty()) {
      throw new ResourceNotFoundException("Client not found with id: " + shipment.getClient().getId());
    }
    shipment.setClient(c.get());
    shipment.setTrackingCode(generateTrackingCode());

    Shipment saved = shipmentRepository.save(shipment);
    return shipmentMapper.toDTO(saved);
  }

  public Optional<ShipmentDTO> findByTrackingCode(String trackingCode) {
    if (trackingCode == null || trackingCode.isBlank()) {
      throw new BadRequestException("trackingCode must not be null or blank");
    }
    return shipmentRepository.findByTrackingCode(trackingCode).map(shipmentMapper::toDTO);
  }

  public List<ShipmentDTO> findAll() {
    return shipmentRepository.findAll().stream().map(shipmentMapper::toDTO).toList();
  }

  private String generateTrackingCode() {
    long n = ThreadLocalRandom.current().nextLong(1_000_000L, 9_999_999L);
    return "CS" + Long.toString(n);
  }

  public Optional<ShipmentDTO> findById(Long id) {
    return shipmentRepository.findById(id).map(shipmentMapper::toDTO);
  }

  public ShipmentDTO update(Long id, ShipmentDTO dto) {
    if (dto == null)
      throw new BadRequestException("ShipmentDTO must not be null");

    Shipment existing = shipmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id));

    Shipment toSave = shipmentMapper.toEntity(dto);
    toSave.setId(id);
    // preserve tracking code and creation date if not provided
    if (toSave.getTrackingCode() == null)
      toSave.setTrackingCode(existing.getTrackingCode());

    Shipment saved = shipmentRepository.save(toSave);
    return shipmentMapper.toDTO(saved);
  }

  public void deleteById(Long id) {
    if (!shipmentRepository.existsById(id)) {
      throw new ResourceNotFoundException("Shipment not found with id: " + id);
    }
    shipmentRepository.deleteById(id);
  }

  public boolean isShipmentPending(Long id) {
    return shipmentRepository.findById(id)
        .map(shipment -> shipment.getStatus() == ShipmentStatus.PENDIENTE)
        .orElse(false);
  }

  public boolean canDriverUpdateStatus(Long id, ShipmentDTO dto) {
    return shipmentRepository.findById(id)
        .map(shipment -> {
          ShipmentStatus currentStatus = shipment.getStatus();
          ShipmentStatus newStatus = dto.getStatus();
          return (currentStatus == ShipmentStatus.PENDIENTE && newStatus == ShipmentStatus.EN_TRANSITO) ||
              (currentStatus == ShipmentStatus.EN_TRANSITO && newStatus == ShipmentStatus.ENTREGADO) ||
              (currentStatus == ShipmentStatus.EN_TRANSITO && newStatus == ShipmentStatus.NOVEDAD);
        })
        .orElse(false);
  }

  public ShipmentDTO updateStatus(Long id, ShipmentStatus status, String observations) {
    Shipment shipment = shipmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

    shipment.setStatus(status);
    if (observations != null && !observations.trim().isEmpty()) {
      StatusHistory history = new StatusHistory();
      history.setShipment(shipment);
      history.setNewStatus(status);
    }

    return shipmentMapper.toDTO(shipmentRepository.save(shipment));
  }
}
