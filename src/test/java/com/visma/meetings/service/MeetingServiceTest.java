package com.visma.meetings.service;

import com.visma.meetings.dto.MeetingResponse;
import com.visma.meetings.dto.MeetingRequest;
import com.visma.meetings.dto.PersonDTO;
import com.visma.meetings.exception.RequestValidationException;
import com.visma.meetings.mapper.MeetingMapper;
import com.visma.meetings.mapper.PersonMapper;
import com.visma.meetings.model.Meeting;
import com.visma.meetings.model.MeetingCategory;
import com.visma.meetings.model.MeetingType;
import com.visma.meetings.model.Person;
import com.visma.meetings.repository.MeetingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {
    @Mock
    private MeetingRepository meetingRepository;
    private MeetingService meetingService;

    @BeforeEach
    void setUp() {
        meetingService = new MeetingService(meetingRepository);
    }

    @Test
    void canAddMeeting() {
        MeetingRequest testMeetingRequest = MeetingRequest.builder().build();
        meetingService.addMeeting(testMeetingRequest);

        ArgumentCaptor<Meeting> meetingArgumentCaptor = ArgumentCaptor.forClass(Meeting.class);
        verify(meetingRepository).addMeeting(meetingArgumentCaptor.capture());

        Meeting capturedMeeting = meetingArgumentCaptor.getValue();
        assertNotNull(capturedMeeting.getId());
    }

    @Test
    void canDeleteMeeting() {
        UUID zeroUUID = new UUID(0, 0);
        Meeting meeting = Meeting.builder()
                .id(zeroUUID)
                .responsiblePersonId(zeroUUID)
                .build();

        when(meetingRepository.getMeetings()).thenReturn(List.of(meeting));
        meetingService.deleteMeeting(zeroUUID, zeroUUID);
        verify(meetingRepository, times(1)).deleteMeeting(zeroUUID);
    }

    @Test
    void canRemovePersonFromMeeting() {
        UUID zeroUUID = new UUID(0, 0);
        List<Person> participants = List.of(
                new Person(zeroUUID, "Responsible"),
                new Person(new UUID(1, 1), "Another"));
        Meeting meeting = Meeting.builder()
                .id(zeroUUID)
                .responsiblePersonId(zeroUUID)
                .participants(participants)
                .build();

        when(meetingRepository.getMeetings()).thenReturn(List.of(meeting));
        meetingService.removePersonFromMeeting(meeting.getId(), new UUID(1, 1));
        verify(meetingRepository, times(1))
                .removePersonFromMeeting(meeting.getId(), new UUID(1, 1));
    }

    @Test
    void canNotRemovePersonFromMeeting() {
        UUID zeroUUID = new UUID(0, 0);
        List<Person> participants = List.of(
                new Person(zeroUUID, "Responsible"),
                new Person(new UUID(1, 1), "Another"));
        Meeting meeting = Meeting.builder()
                .id(zeroUUID)
                .responsiblePersonId(zeroUUID)
                .participants(participants)
                .build();

        when(meetingRepository.getMeetings()).thenReturn(List.of(meeting));

        UUID meetingID = meeting.getId();
        Exception exception = assertThrows(RequestValidationException.class, () ->
                meetingService.removePersonFromMeeting(meetingID, zeroUUID)
        );

        String expectedMessage = "can't be removed as they are responsible";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void addPersonToMeeting() {
        UUID zeroUUID = new UUID(0, 0);
        List<Person> participants = List.of(
                new Person(zeroUUID, "Responsible"),
                new Person(new UUID(1, 1), "Another"));
        Meeting meeting = Meeting.builder()
                .id(zeroUUID)
                .responsiblePersonId(zeroUUID)
                .participants(participants)
                .build();

        when(meetingRepository.getMeetings()).thenReturn(List.of(meeting));

        UUID meetingID = meeting.getId();
        PersonDTO additionalPersonDTO = new PersonDTO(
                new UUID(2, 2),
                "Additional person");
        Person additionalPerson = PersonMapper.dtoToPerson(additionalPersonDTO);

        meetingService.addPersonToMeeting(meetingID, additionalPersonDTO);

        verify(meetingRepository, times(1))
                .addPersonToMeeting(meeting.getId(), additionalPerson);
    }

    @Test
    void checkFilters() {
        UUID zeroUUID = new UUID(0, 0);
        List<Person> participants = List.of(
                new Person(zeroUUID, "Responsible"),
                new Person(new UUID(1, 1), "Another"));

        List<Meeting> testMeetings = List.of(
                Meeting.builder()
                        .id(zeroUUID)
                        .description("First meeting")
                        .responsiblePersonId(zeroUUID)
                        .category(MeetingCategory.HUB)
                        .type(MeetingType.LIVE)
                        .startDate(LocalDateTime.of(2022, 10, 10, 10, 10))
                        .endDate(LocalDateTime.of(2022, 10, 10, 12, 10))
                        .participants(participants)
                        .build(),
                Meeting.builder()
                        .id(new UUID(1, 1))
                        .description("Second meeting description")
                        .responsiblePersonId(new UUID(1, 1))
                        .category(MeetingCategory.CODE_MONKEY)
                        .type(MeetingType.IN_PERSON)
                        .startDate(LocalDateTime.of(2023, 10, 10, 10, 10))
                        .endDate(LocalDateTime.of(2023, 10, 10, 12, 10))
                        .participants(participants)
                        .build()
        );

        when(meetingRepository.getMeetings()).thenReturn(testMeetings);
        List<MeetingResponse> expectedMeetingsResponse = testMeetings.stream()
                .map(MeetingMapper::meetingToResponse).toList();

        List<MeetingResponse> receivedMeetings = meetingService.getMeetings(null, null,
                null, null, null, null, null);
        assertEquals(expectedMeetingsResponse, receivedMeetings);

        receivedMeetings = meetingService.getMeetings("first", zeroUUID,
                MeetingCategory.HUB, null, null, null, null);
        assertEquals(List.of(expectedMeetingsResponse.get(0)), receivedMeetings);

        receivedMeetings = meetingService.getMeetings(null, null,
                null, null,
                LocalDateTime.of(2020, 10, 10, 12, 10),
                LocalDateTime.of(2024, 10, 10, 10, 10), null);
        assertEquals(expectedMeetingsResponse, receivedMeetings);

        receivedMeetings = meetingService.getMeetings(null, null,
                null, MeetingType.IN_PERSON, null, null, 1);
        assertEquals(List.of(expectedMeetingsResponse.get(1)), receivedMeetings);
    }
}
