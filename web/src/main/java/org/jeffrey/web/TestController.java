package org.jeffrey.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeffrey.core.cache.RedisClient;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.core.util.JsonUtil;
import org.jeffrey.service.user.repository.entity.UserDO;
import org.jeffrey.service.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("test")
@Tag(name = "测试")
public class TestController {
    UserService userService;

    @Autowired
    public TestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("findById")
    @Operation(summary = "根据ID获取用户")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "获取失败")
    })
    @TraceLog(value = "根据ID获取用户", recordParams = true, recordResult = true)
    public UserDO findById(Long id) {
        System.out.println("根据ID获取用户");
        System.out.println(userService.getById(id));
        return userService.getById(id);
    }

    @GetMapping("redis/set")
    public String setRedis() {
        UserDO userDO = new UserDO();
        userDO.setId(1L);
        userDO.setUsername("testUser");
        userDO.setPassword("testPassword");

        String jsonValue = JsonUtil.toStr(userDO);
        RedisClient.setStr("testUser", jsonValue);

        return jsonValue;
    }

    @GetMapping("redis/get")
    public String putRedis() {
        String jsonValue = RedisClient.getStr("testUser");
        if (jsonValue == null) {
            return "No data found in Redis for key 'testUser'";
        }
        UserDO userDO = JsonUtil.toObj(jsonValue, UserDO.class);
        System.out.println("从Redis获取的用户信息: " + userDO);
        RedisClient.del("testUser");
        return "从Redis获取的用户信息: " + userDO;
    }

    @GetMapping("getAllUsers")
    @Operation(summary = "获取所有用户")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "获取失败")
    })
    public List<UserDO> getAllUsers() {
        System.out.println("获取所有用户");
        System.out.println(userService.getAllUsers());
        return userService.getAllUsers();
    }
}
