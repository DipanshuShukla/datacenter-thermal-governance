package com.enginpilot.digitaltwinapi.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/twin")
@RequiredArgsConstructor
@Slf4j
public class DigitalTwinController {

    private final StringRedisTemplate redisTemplate;
    private static final String REDIS_KEY_PREFIX = "twin:state:";

    @GetMapping("/{rackId}/state")
    public ResponseEntity<String> getTwinState(@PathVariable String rackId) {
        String stateJson = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + rackId);

        if (stateJson == null) {
            return ResponseEntity.notFound().build();
        }

        // Return the flattened JSON string exactly as populated by the Governance Engine
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(stateJson);
    }
}