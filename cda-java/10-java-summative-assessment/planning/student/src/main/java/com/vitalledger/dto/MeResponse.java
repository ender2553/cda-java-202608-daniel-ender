package com.vitalledger.dto;

import java.util.List;

/** Identity/authorization view for the authenticated caller. */
public record MeResponse(String username, List<String> roles) {
}
