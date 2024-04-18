package com.ecom.fyp2023.MiroWhiteBoardIntegration;

public interface FastResult<T> {
    void onSuccess(T value);
    void onError(Exception exception);
}