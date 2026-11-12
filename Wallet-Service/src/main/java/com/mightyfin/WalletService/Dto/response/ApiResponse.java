package com.mightyfin.WalletService.Dto.response;

import java.time.Instant;
import java.util.List;

import lombok.Data;
@Data 
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String errorCode;
    private List<FieldViolation> errors;
    private Instant timestamp;

    public ApiResponse() {
    }

    public static class FieldViolation {

        private String field;
        private String message;

        public FieldViolation() {
        }

        public FieldViolation(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static <T> ApiResponse<T> success(String message, T data) {

        ApiResponse<T> response = new ApiResponse<>();

        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(Instant.now());

        return response;
    }

    public static <T> ApiResponse<T> error(
            String errorCode,
            String message,
            FieldViolation... violations) {

        ApiResponse<T> response = new ApiResponse<>();

        response.setSuccess(false);
        response.setMessage(message);
        response.setErrorCode(errorCode);

        if (violations != null && violations.length > 0) {
            response.setErrors(List.of(violations));
        }

        response.setTimestamp(Instant.now());

        return response;
    }
}
