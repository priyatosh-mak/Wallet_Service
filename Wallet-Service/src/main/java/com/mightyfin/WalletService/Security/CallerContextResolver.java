// package com.mightyfin.WalletService.Security;

// import java.util.Arrays;
// import java.util.Locale;
// import java.util.Set;
// import java.util.UUID;
// import java.util.regex.Pattern;
// import java.util.stream.Collectors;

// import org.jspecify.annotations.Nullable;
// import org.springframework.core.MethodParameter;
// import org.springframework.messaging.Message;
// import org.springframework.stereotype.Component;
// import org.springframework.web.bind.support.WebDataBinderFactory;
// import org.springframework.web.context.request.NativeWebRequest;
// import org.springframework.web.method.support.HandlerMethodArgumentResolver;
// import org.springframework.web.method.support.ModelAndViewContainer;

// import com.mightyfin.WalletService.Exception.UnauthorizedException;


// @Component 
// public class CallerContextResolver implements HandlerMethodArgumentResolver{

//      static final String USER_ID_HEADER = "X-User-Id";
//     static final String BUSINESS_ID_HEADER = "X-Business-Id";
//     static final String ROLES_HEADER = "X-User-Roles";

//     static final int MAX_ROLES_HEADER_LENGTH = 1024;
//     static final int MAX_ROLES = 20;

//     private static final Pattern ROLE_NAME =
//             Pattern.compile("^[a-z0-9_.:-]{1,64}$");

//     @Override
//     public boolean supportsParameter(MethodParameter parameter) {
//         return CallerContext.class.equals(parameter.getParameterType());
//     }

//     @Override
//     public Object resolveArgument(
//             MethodParameter parameter,
//             ModelAndViewContainer mavContainer,
//             NativeWebRequest webRequest,
//             WebDataBinderFactory binderFactory) {

//         Integer userId = parseUserId(
//                 webRequest.getHeader(USER_ID_HEADER));

//         UUID businessId = parseBusinessId(
//                 webRequest.getHeader(BUSINESS_ID_HEADER));

//         Set<String> roles = parseRoles(
//                 webRequest.getHeader(ROLES_HEADER));

//         return new CallerContext(userId, businessId, roles);
//     }

//     private static Integer parseUserId(String value) {

//         if (value == null || value.isBlank()) {
//             return null;
//         }

//         try {
//             int id = Integer.parseInt(value.trim());

//             if (id <= 0) {
//                 throw new UnauthorizedException(
//                         "Invalid " + USER_ID_HEADER + " header");
//             }

//             return id;

//         } catch (NumberFormatException e) {
//             throw new UnauthorizedException(
//                     "Invalid " + USER_ID_HEADER + " header");
//         }
//     }

//     private static UUID parseBusinessId(String value) {

//         if (value == null || value.isBlank()) {
//             return null;
//         }

//         try {
//             return UUID.fromString(value.trim());

//         } catch (IllegalArgumentException e) {
//             throw new UnauthorizedException(
//                     "Invalid " + BUSINESS_ID_HEADER + " header");
//         }
//     }

//     private static Set<String> parseRoles(String value) {

//         if (value == null || value.isBlank()) {
//             return Set.of();
//         }

//         if (value.length() > MAX_ROLES_HEADER_LENGTH) {
//             throw new UnauthorizedException(
//                     "Invalid " + ROLES_HEADER + " header");
//         }

//         Set<String> roles = Arrays.stream(value.split(","))
//                 .map(String::trim)
//                 .filter(s -> !s.isEmpty())
//                 .map(s -> s.toLowerCase(Locale.ROOT))
//                 .collect(Collectors.toUnmodifiableSet());

//         if (roles.size() > MAX_ROLES ||
//                 !roles.stream().allMatch(
//                         r -> ROLE_NAME.matcher(r).matches())) {

//             throw new UnauthorizedException(
//                     "Invalid " + ROLES_HEADER + " header");
//         }

//         return roles;
//     }


    



// }
