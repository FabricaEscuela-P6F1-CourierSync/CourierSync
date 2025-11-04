package com.udea.CourierSync.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.udea.CourierSync.entity.User;
import com.udea.CourierSync.enums.UserRole;
import com.udea.CourierSync.repository.UserRepository;

@Component
public class DatabaseSeeder {

  private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @EventListener(ContextRefreshedEvent.class)
  public void seed() {
    logger.info("Starting database seeding...");
    try {
      seedUsersInTransaction();
    } catch (Exception e) {
      logger.error("Error while seeding database: {}", e.getMessage());
      logger.error("Stack trace: ", e);
    }
    logger.info("Database seeding completed.");
  }

  @Transactional(rollbackFor = Exception.class)
  public void seedUsersInTransaction() {
    seedUsers();
  }

  private void seedUsers() {
    String adminEmail = "admin@couriersync.com";
    
    // Buscar el usuario existente
    var existingUserOpt = userRepository.findByEmail(adminEmail);
    
    if (existingUserOpt.isEmpty()) {
      // Crear nuevo usuario si no existe
      try {
        logger.info("Creating admin user...");
        User adminUser = new User();
        adminUser.setName("Admin");
        adminUser.setEmail(adminEmail);
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setPhone("1234567890");

        userRepository.save(adminUser);
        logger.info("Admin user created successfully");
      } catch (Exception e) {
        logger.error("Error creating admin user: {}", e.getMessage());
        logger.error("Stack trace: ", e);
      }
    } else {
      // Verificar y actualizar el rol si es necesario
      User existingUser = existingUserOpt.get();
      if (existingUser.getRole() == null) {
        logger.warn("Admin user exists but has no role. Updating role to ADMIN...");
        try {
          existingUser.setRole(UserRole.ADMIN);
          userRepository.save(existingUser);
          logger.info("Admin user role updated successfully to ADMIN");
        } catch (Exception e) {
          logger.error("Error updating admin user role: {}", e.getMessage());
          logger.error("Stack trace: ", e);
        }
      } else {
        logger.info("Admin user already exists with role: {}", existingUser.getRole());
      }
    }
  }
}
