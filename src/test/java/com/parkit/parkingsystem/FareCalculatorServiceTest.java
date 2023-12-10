package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class FareCalculatorServiceTest {

	private static FareCalculatorService fareCalculatorService;
	private Ticket ticket;

	@BeforeAll
	private static void setUp() {
		fareCalculatorService = new FareCalculatorService();
	}

	@BeforeEach
	private void setUpPerTest() {
		ticket = new Ticket();
	}

	@Test
	public void testCalculateFare_Car() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);

		assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
	}

	@Test
	public void testCalculateFare_Bike() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);

		assertEquals(Fare.BIKE_RATE_PER_HOUR, ticket.getPrice());

	}

	@Test
	public void testCalculateFare_UnkownType() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
	}

	@Test
	public void testCalculateFare_BikeWithFutureInTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
	}

	@Test
	public void testCalculateFare_BikeWithMoreThanOneHourParkingTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);

		BigDecimal expected = (Fare.BIKE_RATE_PER_HOUR).multiply(new BigDecimal("3.00"));
		expected = expected.setScale(2, RoundingMode.HALF_UP);
		assertEquals(expected, ticket.getPrice());
	}

	@Test
	public void testCalculateFare_BikeWithLessThanOneHourParkingTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(45));
		// 45 minutes parking time should give 3/4th parking fare
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);

		BigDecimal expected = (Fare.BIKE_RATE_PER_HOUR).multiply(new BigDecimal("0.75"));
		expected = expected.setScale(2, RoundingMode.HALF_UP);
		assertEquals(expected, ticket.getPrice());
	}

	@Test
	public void testCalculateFare_CarWithMoreThanOneHourParkingTime() {		
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);

		BigDecimal expected = (Fare.CAR_RATE_PER_HOUR).multiply(new BigDecimal("3.00"));
		expected = expected.setScale(2, RoundingMode.HALF_UP);
		assertEquals(expected, ticket.getPrice());
	}

	@Test
	public void testCalculateFare_CarWithLessThanOneHourParkingTime() {		
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(45));
		// 45 minutes parking time should give 3/4th parking fare
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);

		BigDecimal expected = (Fare.CAR_RATE_PER_HOUR).multiply(new BigDecimal("0.75"));
		expected = expected.setScale(2, RoundingMode.HALF_UP);
		assertEquals(expected, ticket.getPrice());
	}

	@Test
	public void testCalculateFare_CarWithMoreThanADayParkingTime() {		
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));
		// 24 hours parking time should give 24 * parking fare per hour
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);

		BigDecimal expected = (Fare.CAR_RATE_PER_HOUR).multiply(BigDecimal.valueOf(24L));
		expected = expected.setScale(2, RoundingMode.HALF_UP);
		assertEquals(expected, ticket.getPrice());
	}

	@Test
	public void testCalculateFare_CarWithLessThan30minutesParkingTime() {		
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(15));
		// 15 minutes parking time should give 0 parking fare
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);

		assertEquals(Fare.FREE, ticket.getPrice());
	}

	@Test
	public void testCalculateFare_BikeWithLessThan30minutesParkingTime() {		
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(15));
		// 15 minutes parking time should give 0 parking fare
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);

		assertEquals(Fare.FREE, ticket.getPrice());
	}

	@Test
	public void testCalculateFare_CarWithDiscount() {		
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));
		// 1 hour parking time with discount should give 5% of parking fare per hour
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket, true);

		BigDecimal expected = (Fare.CAR_RATE_PER_HOUR).multiply(new BigDecimal("0.95"));
		expected = expected.setScale(2, RoundingMode.HALF_UP);
		assertEquals(expected, ticket.getPrice());
	}

	@Test
	public void testCalculateFare_BikeWithDiscount() {		
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));
		// 1 hour parking time with discount should give 5% of parking fare per hour
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket, true);

		BigDecimal expected = (Fare.BIKE_RATE_PER_HOUR).multiply(new BigDecimal("0.95"));
		expected = expected.setScale(2, RoundingMode.HALF_UP);
		assertEquals(expected, ticket.getPrice());
	}
	
	@Test
	public void testCalculateFare_WrongParkingType() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.TEST, false);
		
		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		
		assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));	
	}
}
