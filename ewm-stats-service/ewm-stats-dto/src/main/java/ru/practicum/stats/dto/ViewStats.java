package ru.practicum.stats.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ViewStats {
    @JsonProperty("app")
    String app;

    @JsonProperty("uri")
    String uri;

    @JsonProperty("hits")
    Long hits;
}