package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

	private static ParkingService parkingService;

	@Mock
	private static InputReaderUtil inputReaderUtil;
	@Mock
	private static ParkingSpotDAO parkingSpotDAO;
	@Mock
	private static TicketDAO ticketDAO;

	private Ticket ticket;

	@BeforeEach
	private void setUpPerTest() {

		try {
			ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
			ticket = new Ticket();
			ticket.setInTime(new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)));
			ticket.setParkingSpot(parkingSpot);
			ticket.setVehicleRegNumber("ABCDEF");

			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up test mock objects");
		}
	}

	@Test
	public void testProcessExitingVehicle() throws Exception {

		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
		when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
		when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
		
		parkingService.processExitingVehicle();
		
		verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
	}

	@Test
    public void testProcessIncomingVehicle() {
		
    	when(inputReaderUtil.readSelection()).thenReturn(1);
    	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);  
    	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
    	
        parkingService.processIncomingVehicle();
        
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
    }

	@Test
    public void testProcessIncomingVehicle_InvalidParkingSpot() {	
		
    	when(inputReaderUtil.readSelection()).thenReturn(1);
    	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);
    	
        parkingService.processIncomingVehicle();
        
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(0)).saveTicket(any(Ticket.class));
    }

	@Test
    public void testProcessExitingVehicle_UnableUpdate() {
		
		when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
    	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
    	
    	assertThrows(Exception.class, ()-> parkingService.processExitingVehicle());	
    }

	@Test
    public void testGetNextParkingNumberIfAvailable() {
		
    	when(inputReaderUtil.readSelection()).thenReturn(1);
    	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
    	
    	ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class));
        
        assertEquals(1, parkingSpot.getId());
        assertEquals(true, parkingSpot.isAvailable());
    }

	@Test
    public void testGetNextParkingNumberIfAvailable_ParkingNumberNotFound() {
		
    	when(inputReaderUtil.readSelection()).thenReturn(2);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class));
        assertNull(parkingSpot);
    }

	@Test
    public void testGetNextParkingNumberIfAvailable_ParkingNumberWrongArgument() {
		
    	when(inputReaderUtil.readSelection()).thenReturn(3);

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        assertNull(parkingSpot);
    }
}
