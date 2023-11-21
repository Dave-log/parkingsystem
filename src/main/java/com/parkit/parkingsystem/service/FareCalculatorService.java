package com.parkit.parkingsystem.service;

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
		
		double duration = (outTime - inTime) / ConstantNumbers.HOUR_TO_MS_CONVERTER;
		double discount = hasDiscount ? Fare.DISCOUNT : ConstantNumbers.MULT_NEUTRAL_ELEMENT;

		if (duration >= ConstantNumbers.HALF_HOUR) {
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
			ticket.setPrice(ConstantNumbers.FREE);
		}
	}

	public void calculateFare(Ticket ticket) {
		calculateFare(ticket, false);
	}
}