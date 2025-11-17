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
public class EventFullDto {
    @JsonProperty("id")
    Long id;

    @JsonProperty("annotation")
    String annotation;

    @JsonProperty("category")
    CategoryDto category;

    @JsonProperty("description")
    String description;

    @JsonProperty("eventDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    @JsonProperty("location")
    LocationDto location;

    @JsonProperty("paid")
    Boolean paid;

    @JsonProperty("participantLimit")
    Integer participantLimit;

    @JsonProperty("requestModeration")
    Boolean requestModeration;

    @JsonProperty("title")
    String title;

    @JsonProperty("state")
    String state;

    @JsonProperty("initiator")
    UserShortDto initiator;

    @JsonProperty("createdOn")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdOn;

    @JsonProperty("publishedOn")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime publishedOn;

    @JsonProperty("confirmedRequests")
    Long confirmedRequests;

    @JsonProperty("views")
    Long views;
}