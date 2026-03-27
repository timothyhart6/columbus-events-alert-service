package com.ColumbusEventAlertService.services;

import com.ColumbusEventAlertService.models.Event;
import com.ColumbusEventAlertService.services.smsProviders.TwilioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TextMessageServiceTest {
    @InjectMocks
    private TextMessageService textMessageService;
    @Mock
    private TwilioService twilioService;
    @Mock
    private EventAggregatorService eventAggregatorService;

    ArrayList<Event> events;

    @BeforeEach
    public void setup() {
        events = new ArrayList<>();
    }

    @Test
    public void testSendTodaysEvents() {

        textMessageService.sendTodaysEvents();
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(twilioService).sendTextMessage(messageCaptor.capture());
    }

    @Test
    void testSendTodaysEvents_NoEventToday() {
        String expectedMessage = "No Events today!";
        when(eventAggregatorService.getCurrentDayEvents()).thenReturn(events);

        textMessageService.sendTodaysEvents();
        verify(twilioService).sendTextMessage(eq(expectedMessage));
    }
}