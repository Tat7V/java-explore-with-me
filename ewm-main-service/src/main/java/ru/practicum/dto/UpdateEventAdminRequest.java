package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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
public class UpdateEventAdminRequest {
    @Size(min = 20, max = 2000)
    @JsonProperty("annotation")
    String annotation;

    @JsonProperty("category")
    Long category;

    @Size(min = 20, max = 7000)
    @JsonProperty("description")
    String description;

    @Future
    @JsonProperty("eventDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    @Valid
    @JsonProperty("location")
    LocationDto location;

    @JsonProperty("paid")
    Boolean paid;

    @PositiveOrZero
    @JsonProperty("participantLimit")
    Integer participantLimit;

    @JsonProperty("requestModeration")
    Boolean requestModeration;

    @JsonProperty("stateAction")
    String stateAction;

    @Size(min = 3, max = 120)
    @JsonProperty("title")
    String title;
}