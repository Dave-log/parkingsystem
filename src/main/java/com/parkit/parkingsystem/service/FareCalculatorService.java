package com.parkit.parkingsystem.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

import com.parkit.parkingsystem.constants.ConstantNumbers;
import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	public void calculateFare(Ticket ticket, boolean hasDiscount) {
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}

		long inTime = ticket.getInTime().getTime();
		long outTime = ticket.getOutTime().getTime();
		
		BigDecimal durationInMillis = BigDecimal.valueOf(outTime - inTime);
		BigDecimal millisToHourConverter = BigDecimal.valueOf(TimeUnit.HOURS.toMillis(1));
		BigDecimal duration = durationInMillis.divide(millisToHourConverter, 2, RoundingMode.HALF_UP);
		BigDecimal discount = hasDiscount ? Fare.DISCOUNT : ConstantNumbers.MULT_NEUTRAL_ELEMENT;

		if (duration.compareTo(ConstantNumbers.HALF_HOUR) >= 0) {
			switch (ticket.getParkingSpot().getParkingType()) {
			case CAR: {
				ticket.setPrice(duration.multiply(Fare.CAR_RATE_PER_HOUR).multiply(discount));
				break;
			}
			case BIKE: {
				ticket.setPrice(duration.multiply(Fare.BIKE_RATE_PER_HOUR).multiply(discount));
				break;
			}
			default:
				throw new IllegalArgumentException("Unkown Parking Type");
			}
		} else {
			ticket.setPrice(Fare.FREE);
		}
	}

	public void calculateFare(Ticket ticket) {
		calculateFare(ticket, false);
	}
}