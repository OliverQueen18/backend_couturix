package com.couturix.user;
import com.couturix.common.api.PageResponse;
import com.couturix.user.dto.UserDto;
import com.couturix.user.dto.UserRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class UserController {
    private final UserService userService;
    @GetMapping public PageResponse<UserDto> list(
            @RequestParam(required = false) String role,
            Pageable pageable) {
        return userService.list(role, pageable);
    }
    @GetMapping("/{id}") public UserDto get(@PathVariable Long id) { return userService.get(id); }
    @PostMapping public UserDto create(@Valid @RequestBody UserRequest request) { return userService.create(request); }
    @PutMapping("/{id}") public UserDto update(@PathVariable Long id, @Valid @RequestBody UserRequest request) { return userService.update(id, request); }
    @PatchMapping("/{id}/actif") public UserDto toggle(@PathVariable Long id, @RequestParam boolean actif) { return userService.toggle(id, actif); }
}
