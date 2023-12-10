package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.Date;

public class ParkingService {

	private static final Logger logger = LogManager.getLogger("ParkingService");

	private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

	private InputReaderUtil inputReaderUtil;
	private ParkingSpotDAO parkingSpotDAO;
	private TicketDAO ticketDAO;

	public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO) {
		this.inputReaderUtil = inputReaderUtil;
		this.parkingSpotDAO = parkingSpotDAO;
		this.ticketDAO = ticketDAO;
	}

	public void processIncomingVehicle() throws IllegalArgumentException {
		ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
		if (parkingSpot != null && parkingSpot.getId() > 0) {
			String vehicleRegNumber = getVehicleRegNumber();
			parkingSpot.setAvailable(false);
				parkingSpotDAO.updateParking(parkingSpot);// allot this parking space and mark it's availability as
															// false

				int nbTickets = ticketDAO.getNbTicket(vehicleRegNumber);
				if (nbTickets > 0) {
					logger.info(
							"Happy to see you again! As a regular user of our parking, you will get a 5% discount.");
				}

				Date inTime = new Date();
				Ticket ticket = new Ticket();
				ticket.setParkingSpot(parkingSpot);
				ticket.setVehicleRegNumber(vehicleRegNumber);
				ticket.setPrice(BigDecimal.ZERO);
				ticket.setInTime(inTime);
				ticketDAO.saveTicket(ticket);
				logger.info("Generated Ticket and saved in DB");
				logger.info("Please park your vehicle in spot number:" + parkingSpot.getId());
				logger.info("Recorded in-time for vehicle number:" + vehicleRegNumber + " is:" + inTime);
		}
	}

	private String getVehicleRegNumber() {
		logger.info("Please type the vehicle registration number and press enter key");
		return inputReaderUtil.readVehicleRegistrationNumber();
	}

	public ParkingSpot getNextParkingNumberIfAvailable() {
		int parkingNumber = 0;
		ParkingSpot parkingSpot = null;
		try {
			ParkingType parkingType = getVehicleType();
			parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
			if (parkingNumber > 0) {
				parkingSpot = new ParkingSpot(parkingNumber, parkingType, true);
			} else {
				throw new Exception("Error fetching parking number from DB. Parking slots might be full");
			}
		} catch (IllegalArgumentException ie) {
			logger.error("Error parsing user input for type of vehicle", ie);
		} catch (Exception e) {
			logger.error("Error fetching next available parking slot", e);
		}
		return parkingSpot;
	}

	private ParkingType getVehicleType() {
		logger.info("Please select vehicle type from menu");
		logger.info("1 CAR");
		logger.info("2 BIKE");
		int input = inputReaderUtil.readSelection();
		return switch (input) {
			case 1 -> ParkingType.CAR;
			case 2 -> ParkingType.BIKE;
			default -> {
				logger.info("Incorrect input provided");
				throw new IllegalArgumentException("Unexpected value: " + input);
			}
		};
	}

	public void processExitingVehicle() throws Exception {
		String vehicleRegNumber = getVehicleRegNumber();
		Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
		Date outTime = new Date();
		ticket.setOutTime(outTime);
		int nbTickets = ticketDAO.getNbTicket(vehicleRegNumber);

		if (nbTickets > 1) {
			fareCalculatorService.calculateFare(ticket, true);
		} else {
			fareCalculatorService.calculateFare(ticket);
		}

		if (ticketDAO.updateTicket(ticket)) {
			ParkingSpot parkingSpot = ticket.getParkingSpot();
			parkingSpot.setAvailable(true);
			parkingSpotDAO.updateParking(parkingSpot);
			logger.info("Please pay the parking fare:" + ticket.getPrice());
			logger.info("Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:" + outTime);
		} else {
			throw new Exception("Unable to update ticket information. Error occurred");
		}
	}
}
