package com.udea.CourierSync.services;

import com.udea.CourierSync.DTO.UserDTO;
import com.udea.CourierSync.entity.User;
import com.udea.CourierSync.enums.UserRole;
import com.udea.CourierSync.exception.BadRequestException;
import com.udea.CourierSync.exception.ResourceNotFoundException;
import com.udea.CourierSync.mapper.UserMapper;
import com.udea.CourierSync.repository.UserRepository;
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
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @InjectMocks
  private UserService userService;

  private User user;
  private UserDTO userDTO;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setName("John Doe");
    user.setEmail("john@example.com");
    user.setPassword("encodedPassword");
    user.setPhone("1234567890");
    user.setRole(UserRole.ADMIN);

    userDTO = new UserDTO();
    userDTO.setId(1L);
    userDTO.setName("John Doe");
    userDTO.setEmail("john@example.com");
    userDTO.setPassword("encodedPassword");
    userDTO.setPhone("1234567890");
    userDTO.setRole(UserRole.ADMIN);
  }

  @Test
  void testCreate_Success() {
    when(userMapper.toEntity(userDTO)).thenReturn(user);
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(userMapper.toDTO(user)).thenReturn(userDTO);

    UserDTO result = userService.create(userDTO);

    assertNotNull(result);
    assertEquals(userDTO.getEmail(), result.getEmail());
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void testCreate_NullDTO_ThrowsException() {
    assertThrows(BadRequestException.class, () -> {
      userService.create(null);
    });
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void testFindById_Success() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(userMapper.toDTO(user)).thenReturn(userDTO);

    Optional<UserDTO> result = userService.findById(1L);

    assertTrue(result.isPresent());
    assertEquals(userDTO.getId(), result.get().getId());
  }

  @Test
  void testFindByEmail_Success() {
    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(userMapper.toDTO(user)).thenReturn(userDTO);

    Optional<UserDTO> result = userService.findByEmail("john@example.com");

    assertTrue(result.isPresent());
    assertEquals(userDTO.getEmail(), result.get().getEmail());
  }

  @Test
  void testFindAll_Success() {
    User user2 = new User();
    user2.setId(2L);
    user2.setEmail("jane@example.com");

    UserDTO userDTO2 = new UserDTO();
    userDTO2.setId(2L);
    userDTO2.setEmail("jane@example.com");

    when(userRepository.findAll()).thenReturn(Arrays.asList(user, user2));
    when(userMapper.toDTO(user)).thenReturn(userDTO);
    when(userMapper.toDTO(user2)).thenReturn(userDTO2);

    List<UserDTO> result = userService.findAll();

    assertEquals(2, result.size());
    verify(userRepository, times(1)).findAll();
  }

  @Test
  void testUpdate_Success() {
    UserDTO updatedDTO = new UserDTO();
    updatedDTO.setName("John Updated");
    updatedDTO.setEmail("john.updated@example.com");
    updatedDTO.setPassword("newPassword");

    User updatedUser = new User();
    updatedUser.setId(1L);
    updatedUser.setName("John Updated");
    updatedUser.setPassword("newPassword");

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(userMapper.toEntity(updatedDTO)).thenReturn(updatedUser);
    when(userRepository.save(any(User.class))).thenReturn(updatedUser);
    when(userMapper.toDTO(updatedUser)).thenReturn(updatedDTO);

    UserDTO result = userService.update(1L, updatedDTO);

    assertNotNull(result);
    verify(userRepository, times(1)).findById(1L);
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void testUpdate_WithoutPassword_PreservesOldPassword() {
    UserDTO updatedDTO = new UserDTO();
    updatedDTO.setName("John Updated");
    updatedDTO.setPassword(""); // Empty password

    User updatedUser = new User();
    updatedUser.setId(1L);
    updatedUser.setName("John Updated");

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(userMapper.toEntity(updatedDTO)).thenReturn(updatedUser);
    when(userRepository.save(any(User.class))).thenReturn(updatedUser);
    when(userMapper.toDTO(any(User.class))).thenReturn(updatedDTO);

    userService.update(1L, updatedDTO);

    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void testUpdate_NotFound_ThrowsException() {
    UserDTO updatedDTO = new UserDTO();
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> {
      userService.update(999L, updatedDTO);
    });
  }

  @Test
  void testDeleteById_Success() {
    when(userRepository.existsById(1L)).thenReturn(true);
    doNothing().when(userRepository).deleteById(1L);

    assertDoesNotThrow(() -> {
      userService.deleteById(1L);
    });

    verify(userRepository, times(1)).deleteById(1L);
  }

  @Test
  void testDeleteById_NotFound_ThrowsException() {
    when(userRepository.existsById(999L)).thenReturn(false);

    assertThrows(ResourceNotFoundException.class, () -> {
      userService.deleteById(999L);
    });
  }
}
