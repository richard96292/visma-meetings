package com.visma.meetings.service;

import com.visma.meetings.dto.MeetingCreationRequest;
import com.visma.meetings.model.Meeting;
import com.visma.meetings.model.Person;
import com.visma.meetings.repository.MeetingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class MeetingService {
    private final MeetingRepository meetingRepository;

    public MeetingService(MeetingRepository meetingRepository) {
        this.meetingRepository = meetingRepository;
    }

    public void addMeeting(MeetingCreationRequest meetingCreationRequest) {
        Meeting meeting = new Meeting(
                UUID.randomUUID(),
                meetingCreationRequest.name(),
                meetingCreationRequest.responsiblePersonId(),
                meetingCreationRequest.description(),
                meetingCreationRequest.category(),
                meetingCreationRequest.type(),
                meetingCreationRequest.startDate(),
                meetingCreationRequest.endDate(),
                meetingCreationRequest.participants());

        meetingRepository.addMeeting(meeting);
    }

    public void deleteMeeting(UUID meetingId) {
        meetingRepository.deleteMeeting(meetingId);
    }

    public List<Meeting> getMeetings() {
        return meetingRepository.getMeetings();
    }

    public void addPersonToMeeting(UUID meetingId, Person person) {
        meetingRepository.getMeetings().stream()
                .filter(item -> Objects.equals(item.getId(), meetingId))
                .findFirst()
                .ifPresent(item -> item.getParticipants().add(person));
    }

    public void removePersonFromMeeting(UUID meetingId, Person person) {
        meetingRepository.removePersonFromMeeting(meetingId, person);
    }
}
