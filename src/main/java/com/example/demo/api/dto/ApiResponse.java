package com.example.demo.api.dto;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private boolean result;
    private String message = "task successfully";
    private T data = null;

    public ApiResponse(boolean success, String message, T data) {
        this.result = success;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, null, data);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> empty(String message) {
        return new ApiResponse<>(true, message, null);
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message,null);
	}
}