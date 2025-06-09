package com.explore.Redis_Practice.Service;

import com.explore.Redis_Practice.Entity.Users;
import com.explore.Redis_Practice.Repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    private final RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserService(UserRepository userRepository, RedisTemplate<String, String> redisTemplate) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }

    public Users createUser(Users newUser) throws Exception {

        Users savedUser=  userRepository.save(newUser);
        String userForCache= objectMapper.writeValueAsString(savedUser);
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        valueOps.set("user:"+ savedUser.getId(), userForCache, 100, TimeUnit.SECONDS);
        return savedUser;
    }

    public ResponseEntity<Users> getUser(Integer id) {
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        String cachedUser = valueOps.get("user:"+id);

        if(cachedUser != null){
            try{
               Users user = objectMapper.readValue(cachedUser, Users.class);
                log.info("Fetched User from Cache with ID:{}", user.getId());
               return ResponseEntity.ok(user);
            } catch (Exception e) {
                log.info("Error : { }", e);
                return ResponseEntity.status(500).build();
            }
        }else{
            Optional<Users> userOpt = userRepository.findById(id);
            if(userOpt.isPresent()){
                Users user= userOpt.get();
                try{
                    String userForCache= objectMapper.writeValueAsString(user);
                    valueOps.set("user:"+ user.getId(), userForCache, 100, TimeUnit.SECONDS);
                }catch (Exception e) {
                    log.info("Error : { }", e);
                    return ResponseEntity.status(500).build();
                }
                log.info("Fetched User from DB with ID:{}", user.getId());
                return ResponseEntity.ok(user);
            }else{
                return ResponseEntity.notFound().build();
            }
        }
    }


    public ResponseEntity<String> updateUser(Integer id, Users updatedUser) throws Exception {
        Optional<Users> userOpt = userRepository.findById(id);
        if(userOpt.isPresent()){
            Users existingUser = userOpt.get();
            existingUser.setName(updatedUser.getName());
            existingUser.setEmail(updatedUser.getEmail());
            userRepository.save(existingUser);

            ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
            String userForCache= objectMapper.writeValueAsString(existingUser);
            valueOps.set("user:"+ existingUser.getId(), userForCache, 100, TimeUnit.SECONDS);

            return ResponseEntity.ok("User updated in H2 Database and cached in Redis");
        }else{
            return ResponseEntity.notFound().build();
        }
    }


    public ResponseEntity<String> deleteUser(Integer id) {
        if(userRepository.existsById(id)){
            userRepository.deleteById(id);
            redisTemplate.delete("user:"+ id);
            return ResponseEntity.ok("User deleted from H2 Database and Redis cache");
        }else{
            return ResponseEntity.notFound().build();
        }
    }
}
