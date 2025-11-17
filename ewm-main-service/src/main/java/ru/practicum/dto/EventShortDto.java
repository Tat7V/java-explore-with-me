package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventShortDto {
    @JsonProperty("id")
    Long id;

    @JsonProperty("annotation")
    String annotation;

    @JsonProperty("category")
    CategoryDto category;

    @JsonProperty("eventDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    @JsonProperty("paid")
    Boolean paid;

    @JsonProperty("title")
    String title;

    @JsonProperty("initiator")
    UserShortDto initiator;

    @JsonProperty("confirmedRequests")
    Long confirmedRequests;

    @JsonProperty("views")
    Long views;
}