package com.simba.meetingnotification.contact

import com.simba.meetingnotification.ui.contact.ContactCheckBeforeSubmitViewModel
import com.simba.meetingnotification.ui.data.entities.Contact
import com.simba.meetingnotification.ui.data.entities.Event
import com.simba.meetingnotification.ui.data.repositories.BackgroundImageManagerRepository
import com.simba.meetingnotification.ui.data.repositories.ContactRepository
import com.simba.meetingnotification.ui.data.repositories.EventRepository
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

//Probe testklasse für Junit.
class ContactCheckBeforeSubmitViewModelTest {

    private lateinit var contactRepository: ContactRepository
    private lateinit var eventRepository: EventRepository
    private lateinit var backgroundImageManagerRepository: BackgroundImageManagerRepository
    private lateinit var contactCheckBeforeSubmitViewModel: ContactCheckBeforeSubmitViewModel


    @Before
    fun setup(){
        contactRepository = mockk()
        eventRepository = mockk()
        backgroundImageManagerRepository = mockk()


        coEvery { contactRepository.getContactStream(4) } returns Contact(4,"Dr","mane","bane",'m',"03847473","Seas")
        coEvery { contactRepository.getAllContactsStream() } returns flowOf(listOf(Contact(4,"Dr","mane","bane",'m',"03847473","Seas")))
        coEvery { eventRepository.getEvents(4) } returns listOf(Event(1,"1996-04-06","18:30",4,true))
        coEvery { backgroundImageManagerRepository.get()} returns flowOf(4)

        contactCheckBeforeSubmitViewModel = ContactCheckBeforeSubmitViewModel(contactRepository,eventRepository,backgroundImageManagerRepository)
    }


    @Test
    fun getDayDuration() = runTest{
        //This test be inconsistent because the localdate.now() changes every day.
        val viewmodel = contactCheckBeforeSubmitViewModel.getDayDuration("2025-09-20")
        assertEquals("38",viewmodel)
    }
}