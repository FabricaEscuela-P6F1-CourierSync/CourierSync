package com.udea.CourierSync.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.udea.CourierSync.repository.UserRepository;
import com.udea.CourierSync.mapper.UserMapper;
import com.udea.CourierSync.DTO.UserDTO;
import com.udea.CourierSync.entity.User;

import java.util.List;
import java.util.Optional;
import com.udea.CourierSync.exception.BadRequestException;
import com.udea.CourierSync.exception.ResourceNotFoundException;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserMapper userMapper;

  public UserDTO create(UserDTO dto) {
    if (dto == null)
      throw new BadRequestException("UserDTO must not be null");
    User entity = userMapper.toEntity(dto);
    User saved = userRepository.save(entity);
    return userMapper.toDTO(saved);
  }

  public Optional<UserDTO> findById(Long id) {
    return userRepository.findById(id).map(userMapper::toDTO);
  }

  public Optional<UserDTO> findByEmail(String email) {
    return userRepository.findByEmail(email).map(userMapper::toDTO);
  }

  public List<UserDTO> findAll() {
    return userRepository.findAll().stream().map(userMapper::toDTO).toList();
  }

  public UserDTO update(Long id, UserDTO dto) {
    if (dto == null)
      throw new BadRequestException("UserDTO must not be null");
    
    // Cargar el usuario existente
    User existingUser = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    
    // Mapear el DTO a Entity
    User toSave = userMapper.toEntity(dto);
    toSave.setId(id);
    
    // Si el password viene null o vacío, mantener el password actual
    if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
      toSave.setPassword(existingUser.getPassword());
    }
    
    User saved = userRepository.save(toSave);
    return userMapper.toDTO(saved);
  }

  public void deleteById(Long id) {
    if (!userRepository.existsById(id)) {
      throw new ResourceNotFoundException("User not found with id: " + id);
    }
    userRepository.deleteById(id);
  }
}
