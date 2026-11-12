// package com.mightyfin.WalletService.Security;

// import java.util.Collections;
// import java.util.Locale;
// import java.util.Set;
// import java.util.UUID;
// import java.util.stream.Collectors;

// import lombok.Data;

// @Data 
// public class CallerContext {


//     public static final String SUPER_ADMIN_ROLE = "super_admin";
//     public static final String INTERNAL_SERVICE_ROLE = "internal_service"; // for internal service calling

//     private Integer userId;
//     private UUID businessPublicUuid;
//     private Set<String> roles;


//      public CallerContext(Integer userId, UUID businessPublicUuid, Set<String> roles) {
//         this.userId = userId;
//         this.businessPublicUuid = businessPublicUuid;

//         this.roles = roles == null
//                 ? Set.of()
//                 : roles.stream()
//                     .filter(role -> role != null)
//                     .map(role -> role.toLowerCase(Locale.ROOT))
//                     .collect(Collectors.toUnmodifiableSet());
//     }

//     // Anonymous caller
//     public static CallerContext anonymous() {
//         return new CallerContext(null, null, Set.of());
//     }

//     // Check authentication
//     public boolean isAuthenticated() {
//         return userId != null
//                 || businessPublicUuid != null
//                 || isInternalService();
//     }

//     // Check super admin role
//     public boolean isSuperAdmin() {
//         return roles.contains(SUPER_ADMIN_ROLE);
//     }

//     // Check internal service role
//     public boolean isInternalService() {
//         return roles.contains(INTERNAL_SERVICE_ROLE);
//     }


// }
