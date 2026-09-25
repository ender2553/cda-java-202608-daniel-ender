package com.example.rpg.service;
import com.example.rpg.domain.*; import com.example.rpg.repository.UserRepository; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Service; import java.util.Locale;
/** Authentication service keeps password handling out of the UI and domain model. */
@Service public final class AuthService { private final UserRepository users; private final PasswordEncoder encoder; public AuthService(UserRepository users,PasswordEncoder encoder){this.users=users;this.encoder=encoder;} public UserAccount register(String username,String password){String name=Domain.cleanName(username.toLowerCase(Locale.ROOT),"username",32);if(password==null||password.length()<12||password.length()>128)throw new IllegalArgumentException("password must be 12-128 characters");if(users.findByUsername(name).isPresent())throw new IllegalArgumentException("username already exists");
        // Never persist or return a plaintext password. BCrypt also generates a unique salt.
        return users.save(new UserAccount(0,name,encoder.encode(password)));} public UserAccount login(String username,String password){UserAccount user=users.findByUsername(username.toLowerCase(Locale.ROOT)).orElseThrow(()->new IllegalArgumentException("invalid credentials"));
        // A generic error avoids revealing whether a username exists (user enumeration defense).
        if(password==null||!encoder.matches(password,user.passwordHash()))throw new IllegalArgumentException("invalid credentials");return user;} }
