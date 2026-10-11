
package com.worksphere.user;

public record AuthResponse(
        Long id,
        String name,
        String email
) {
    public static AuthResponse from(AppUser user) {
        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}
