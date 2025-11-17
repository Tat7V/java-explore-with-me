package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class NewEventDto {
    @NotBlank
    @Size(min = 20, max = 2000)
    @JsonProperty("annotation")
    String annotation;

    @NotNull
    @JsonProperty("category")
    Long category;

    @NotBlank
    @Size(min = 20, max = 7000)
    @JsonProperty("description")
    String description;

    @NotNull
    @Future
    @JsonProperty("eventDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    @NotNull
    @Valid
    @JsonProperty("location")
    LocationDto location;

    @JsonProperty("paid")
    Boolean paid = false;

    @PositiveOrZero
    @JsonProperty("participantLimit")
    Integer participantLimit = 0;

    @JsonProperty("requestModeration")
    Boolean requestModeration = true;

    @NotBlank
    @Size(min = 3, max = 120)
    @JsonProperty("title")
    String title;
}