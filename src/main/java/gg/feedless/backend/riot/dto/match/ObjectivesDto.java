package gg.feedless.backend.riot.dto.match;

public record ObjectivesDto(ObjectiveDto baron, ObjectiveDto champion, ObjectiveDto dragon, ObjectiveDto horde,
                            ObjectiveDto inhibitor, ObjectiveDto riftHerald, ObjectiveDto tower) {
}
