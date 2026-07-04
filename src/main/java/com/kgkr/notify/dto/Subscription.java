package com.kgkr.notify.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Subscription {
    @Id
    private String id;

    private Long userId;
    private Long channelId;
}
