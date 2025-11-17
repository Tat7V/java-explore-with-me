package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationDto {
    @JsonProperty("id")
    Long id;

    @JsonProperty("title")
    String title;

    @JsonProperty("pinned")
    Boolean pinned;

    @JsonProperty("events")
    List<EventShortDto> events;
}