
package com.worksphere.user;

public record LoginResponse(
        String token,
        String tokenType,
        Long userId,
        String name,
        String email
) {
    public static LoginResponse from(AppUser user, String token) {
        return new LoginResponse(
                token,
                "Bearer",
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}
