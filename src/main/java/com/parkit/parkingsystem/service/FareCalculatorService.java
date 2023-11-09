package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	public void calculateFare(Ticket ticket, boolean hasDiscount) {
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}

		long inTime = ticket.getInTime().getTime();
		long outTime = ticket.getOutTime().getTime();

		double duration = (outTime - inTime) / (1000.0 * 60.0 * 60.0);
		double discount = hasDiscount ? Fare.DISCOUNT : 1.0;

		if (duration >= 0.5) {
			switch (ticket.getParkingSpot().getParkingType()) {
			case CAR: {
				ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR * discount);
				break;
			}
			case BIKE: {
				ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR * discount);
				break;
			}
			default:
				throw new IllegalArgumentException("Unkown Parking Type");
			}
		} else {
			ticket.setPrice(0.0);
		}
	}

	public void calculateFare(Ticket ticket) {
		calculateFare(ticket, false);
	}
}