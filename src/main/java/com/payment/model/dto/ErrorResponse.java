package com.payment.model.dto;

import java.time.ZonedDateTime;

public record ErrorResponse(int status, String message, ZonedDateTime timestamp) {
}
