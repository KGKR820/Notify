package com.kgkr.notify.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Notification {
    @Id
    private Long id;

    private Long userId;
    private String message;
    private boolean delivered;
}
