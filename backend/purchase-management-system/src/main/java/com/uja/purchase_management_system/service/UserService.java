package com.uja.purchase_management_system.service;

import com.uja.purchase_management_system.dto.UserDTO;
import com.uja.purchase_management_system.dto.UserRegistrationDTO;
import java.util.List;

public interface UserService {
    UserDTO register(UserRegistrationDTO dto);
    List<UserDTO> findAll();
}