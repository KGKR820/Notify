package com.kgkr.notify.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import java.time.Instant;

@Data
public class Notification {
    @Id
    private String id;

    private Long userId;
    private Long channelId;
    private String message;
    private boolean delivered;
    private boolean read;
    private Instant timestamp;
}
