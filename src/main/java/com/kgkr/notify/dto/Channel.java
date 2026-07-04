package com.kgkr.notify.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Channel {
    @Id
    private Long id;
    private String name;
}