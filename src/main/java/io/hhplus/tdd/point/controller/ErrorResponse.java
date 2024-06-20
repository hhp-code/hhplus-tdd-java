package io.hhplus.tdd.point.controller;

public record ErrorResponse(
        String code,
        String message
) {
}
