package com.udea.CourierSync.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.udea.CourierSync.services.UserService;
import com.udea.CourierSync.DTO.UserDTO;
import com.udea.CourierSync.DTO.SignUpRequest;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

  @Autowired
  private UserService userService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @PostMapping("/new")
  public ResponseEntity<UserDTO> createNewUser(@RequestBody SignUpRequest signUpRequest) {
    UserDTO userDTO = signUpRequest.toUserDTO();
    userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
    UserDTO created = userService.create(userDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @GetMapping
  public List<UserDTO> list() {
    return userService.findAll();
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDTO> get(@PathVariable Long id) {
    return userService.findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserDTO> update(@PathVariable Long id, @RequestBody UserDTO dto) {
    // Si viene una contraseña nueva, codificarla antes de actualizar
    if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
      dto.setPassword(passwordEncoder.encode(dto.getPassword()));
    }
    UserDTO updated = userService.update(id, dto);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    userService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
