package org.jeffrey.service.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeffrey.api.dto.user.UserUpdateDTO;
import org.jeffrey.api.vo.User.UserVO;
import org.jeffrey.service.user.repository.entity.UserDO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface UserService extends IService<UserDO> {
    public List<UserDO> getAllUsers();

    public Optional<UserDO> getUserByUsername(String username);
    
    /**
     * Register a new user
     * @param username username
     * @param password password
     * @return true if registration is successful, false otherwise
     */
    boolean registerUser(String username, String password);

    UserVO updateUserProfile(UserUpdateDTO dto);
    
    /**
     * 更新用户头像
     * @param avatar 头像文件
     * @return 头像URL
     */
    String updateUserAvatar(MultipartFile avatar);
}
