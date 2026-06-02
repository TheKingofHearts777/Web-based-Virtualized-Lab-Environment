package com.csproj.Cyberlab.API.auth;

import com.csproj.Cyberlab.API.exceptions.ForbiddenException;
import com.csproj.Cyberlab.API.exceptions.NotFoundException;
import com.csproj.Cyberlab.API.jwt.JwtService;
import com.csproj.Cyberlab.API.jwt.TokenType;
import com.csproj.Cyberlab.API.user.User;
import com.csproj.Cyberlab.API.user.UserRepo;
import com.csproj.Cyberlab.API.user.UserResponseDTO;
import com.csproj.Cyberlab.API.user.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

//----------------------
// Auth business logic
//----------------------
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final JwtService jwtService;

    /**
     * Authenticate a new login request
     * Generates a session JWT for successful authentication
     *
     * @param dto Login request data
     * @return Session info including jwt and user
     */
    public AuthResponseDTO authenticate(AuthDTO dto) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getUsername(),
                        dto.getPassword()
                )
        );

        // Guaranteed to exist if authentication passed without throwing an exception
        User user = userRepo.findByUsername(dto.getUsername()).get();
        String access = jwtService.generate(TokenType.ACCESS, user);
        String refresh = jwtService.generate(TokenType.REFRESH, user);

        // Persist hashed refresh token for later verification
        String hashRefresh = passwordEncoder.encode(refresh);
        user.setRefreshToken(hashRefresh);
        userRepo.save(user);

        return new AuthResponseDTO(access, refresh, new UserResponseDTO(user));
    }

    /**
     * Generates a new access token if supplied with a valid refresh token
     * Valid refresh tokens are non-expired and match the requesting client's persisted User.refreshToken
     *
     * @param token Refresh token
     * @param userId ID of User to generate new access token for
     * @return New access token
     */
    public String refresh(String token, String userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("Requested User not found", null));

        String hashedToken = passwordEncoder.encode(token);

        if (!jwtService.isValid(hashedToken, user)) {
            throw new ForbiddenException("Refresh token is invalid or expired, please re-authenticate", null);
        }

        return jwtService.generate(TokenType.ACCESS, user);
    }

    /**
     * Sets a User's refresh token to null so they may not extend their current session without re-authenticating.
     * Will not supply any error flags if there wasn't a refresh token to invalidate.
     *
     * @param userId ID of the User to logout
     */
    public void logout(String userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("Requested User not found", null));

        user.setRefreshToken(null);
        userRepo.save(user);
    }

    /**
     * Resource level authorization wrapper for when only one ID is necessary.
     * See overload method.
     *
     * @param authorizedId ID of authorized resource owner
     * @throws ForbiddenException Requester is not authorized to access the requested resource
     */
    public void authorizeResourceAccess(String authorizedId) throws ForbiddenException {
        List<String> l = new ArrayList<>() {{ add(authorizedId); }};
        authorizeResourceAccess(l);
    }

    /**
     * Determines resource level authorization to access or manipulate a resource.
     * Admin and instructor authorization is only enforced on the method level.
     * Students must own a resource in order to access or manipulate it.
     *
     * @param authorizedIds IDs of authorized resource owners
     * @throws ForbiddenException Requester does not own the requested resource
     */
    public void authorizeResourceAccess(List<String> authorizedIds) throws ForbiddenException {
        String auid = getAuthenticatedId();

        // No Security Context - this is getting called through an internal routine, not a client request
        // If this was a client request, lack of authentication would have been caught in the SecurityFilterChain
        if (auid.isEmpty()) {
            return;
        }

        User aUser = userRepo.findById(auid).get();
        UserType aUser_t = aUser.getUserType();

        // Authorization is only enforced on method level for admins and instructors
        // This should probably be revisited for instructors on the resource level to only access if they instruct the course
        // related to the resource are accessing (or their own resource)
        if (aUser_t.equals(UserType.Admin) || aUser_t.equals(UserType.Instructor)) {
            return;
        }

        if (!authorizedIds.contains(auid)) {
            log.warn("Unauthorized access attempt by: " + auid);
            throw new ForbiddenException("Client is not authorized to access this resource", null);
        }
    }

    /**
     * Extracts userId from the current SecurityContext
     *
     * @return authenticated user ID
     */
    private String getAuthenticatedId() {
        Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(obj instanceof User)) {
            throw new IllegalStateException("Authenticated principle is not of type User");
        }

        return ((User) obj).getId();
    }
}
