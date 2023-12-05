package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	private static DataBasePrepareService dataBasePrepareService;
	private static ParkingService parkingService;

	private String vehicleRegNumberTest = "ABCDEF";

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	private static void setUp() throws Exception {
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareService();
	}

	@BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1); //The vehicle for the test will be a CAR
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(vehicleRegNumberTest);
        dataBasePrepareService.clearDataBaseEntries();
    }

	@Test
	public void testParkingACar() {
		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		parkingService.processIncomingVehicle();

		Ticket ticket = ticketDAO.getTicket(vehicleRegNumberTest);

		assertNotNull(ticket);
		assertEquals(1, ticketDAO.getNbTicket(vehicleRegNumberTest));
		assertFalse(ticket.getParkingSpot().isAvailable());
	}

	@Test
	public void testParkingLotExit() throws Exception {
		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		parkingService.processIncomingVehicle();
		Ticket ticket = ticketDAO.getTicket(vehicleRegNumberTest);
		ticket.setInTime(
				new Date(ticketDAO.getTicket(vehicleRegNumberTest).getInTime().getTime() - TimeUnit.HOURS.toMillis(1)));
		ticketDAO.updateTicketIntime(ticket);

		parkingService.processExitingVehicle();
		ticket = ticketDAO.getTicket(vehicleRegNumberTest);

		assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
		assertEquals(TimeUnit.HOURS.toMillis(1), ticket.getOutTime().getTime() - ticket.getInTime().getTime());
	}

	@Test
	public void testParkingLotExitRecurringUser() throws Exception {
		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		// first ticket setting
		parkingService.processIncomingVehicle();
		Ticket firstTicket = ticketDAO.getTicket(vehicleRegNumberTest);
		firstTicket.setInTime(
				new Date(ticketDAO.getTicket(vehicleRegNumberTest).getInTime().getTime() - TimeUnit.HOURS.toMillis(1)));
		ticketDAO.updateTicketIntime(firstTicket);
		parkingService.processExitingVehicle();
		firstTicket = ticketDAO.getTicket(vehicleRegNumberTest);

		// second ticket setting
		parkingService.processIncomingVehicle();
		Ticket newTicket = ticketDAO.getTicket(vehicleRegNumberTest);
		newTicket.setInTime(
				new Date(ticketDAO.getTicket(vehicleRegNumberTest).getInTime().getTime() - TimeUnit.HOURS.toMillis(1)));
		ticketDAO.updateTicketIntime(newTicket);
		parkingService.processExitingVehicle();
		newTicket = ticketDAO.getTicket(vehicleRegNumberTest);

		BigDecimal expected = Fare.CAR_RATE_PER_HOUR.multiply(Fare.DISCOUNT);
		expected = expected.setScale(2, RoundingMode.HALF_UP);
		assertThat(newTicket.getPrice()).isEqualTo(expected);
	}

}
