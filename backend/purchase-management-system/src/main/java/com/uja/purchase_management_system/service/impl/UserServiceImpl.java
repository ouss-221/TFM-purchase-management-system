package com.uja.purchase_management_system.service.impl;

import com.uja.purchase_management_system.dto.UserDTO;
import com.uja.purchase_management_system.dto.UserRegistrationDTO;
import com.uja.purchase_management_system.entity.Department;
import com.uja.purchase_management_system.entity.Role;
import com.uja.purchase_management_system.entity.User;
import com.uja.purchase_management_system.exception.ResourceNotFoundException;
import com.uja.purchase_management_system.repository.DepartmentRepository;
import com.uja.purchase_management_system.repository.UserRepository;
import com.uja.purchase_management_system.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, DepartmentRepository departmentRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO register(UserRegistrationDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + dto.getUsername());
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + dto.getEmail());
        }

        Role role;
        try {
            role = Role.valueOf(dto.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + dto.getRole());
        }
        if (role == Role.ADMIN) {
            throw new IllegalArgumentException("Cannot self-register as ADMIN");
        }

        Department dept = null;
        if (dto.getDepartmentId() != null) {
            dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + dto.getDepartmentId()));
        }

        User user = new User(dto.getUsername(), passwordEncoder.encode(dto.getPassword()),
                dto.getEmail(), dto.getFullName(), role, dept);

        return toDTO(userRepository.save(user));
    }

    @Override
    public List<UserDTO> findAll() {
        return userRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    private UserDTO toDTO(User u) {
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setEmail(u.getEmail());
        dto.setFullName(u.getFullName());
        dto.setRole(u.getRole().name());
        if (u.getDepartment() != null) dto.setDepartmentName(u.getDepartment().getName());
        dto.setEnabled(u.isEnabled());
        return dto;
    }
}