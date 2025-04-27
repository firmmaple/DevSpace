package org.jeffrey.service.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeffrey.api.dto.user.UserUpdateDTO;
import org.jeffrey.api.vo.User.UserVO;
import org.jeffrey.service.user.repository.entity.UserDO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface UserService extends IService<UserDO> {
    // IService 提供了如 save, saveBatch, saveOrUpdate, removeById,
    // updateById, getById, list, page 等常用业务方法。
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

    /**
     * Get users by their IDs
     * @param userIds list of user IDs
     * @return list of users
     */
    List<UserDO> getUsersByIds(List<Long> userIds);
    
    /**
     * Process an OAuth2 user login (either create a new user or fetch existing one)
     * @param username GitHub username
     * @param githubId GitHub user ID
     * @param email GitHub email
     * @param avatarUrl GitHub avatar URL
     * @return the user
     */
    UserDO processOAuth2User(String username, String githubId, String email, String avatarUrl);
    
    /**
     * 获取用户详情
     * @param userId 用户ID
     * @return 用户视图对象
     */
    UserVO getUserDetailById(Long userId);
    
    /**
     * 管理员删除用户
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteUserById(Long userId);
}
