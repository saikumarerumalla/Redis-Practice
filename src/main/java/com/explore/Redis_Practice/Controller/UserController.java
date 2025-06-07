package com.explore.Redis_Practice.Controller;


import com.explore.Redis_Practice.Entity.Users;
import com.explore.Redis_Practice.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody Users newUser) throws Exception{
        Users user = userService.createUser(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body("User created with ID: " + user.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Users> getUser(@PathVariable Integer id){
        return userService.getUser(id);
    }

//    @PutMapping("/{id}")
//    public ResponseEntity<Void> updateUser (@PathVariable Integer id, @RequestBody Users updatedUser){
//        userService.updateUser(id, updatedUser);
//        return ResponseEntity.noContent().build();
//    }



}
