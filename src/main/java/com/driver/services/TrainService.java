package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;
List<Train> trainList= new ArrayList<>();  // create train list optional
    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library

        Train train = new Train();
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        String route ="";
        List<Station> stationList= trainEntryDto.getStationRoute();

        for(int i=0;i<stationList.size();i++){
            if(i==stationList.size()-1){
                route+= stationList.get(i);
            }
            else{
                route += stationList.get(i)+",";
            }
        }
        train.setRoute(route);

        train.setDepartureTime(trainEntryDto.getDepartureTime());
        trainList.add(train);  // saving list of train
        return trainRepository.save(train).getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.
       Train train= trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
       List<Ticket> ticketList=train.getBookedTickets();
       String [] trainRoute=train.getRoute().split(",");
        HashMap<String,Integer> map=new HashMap<>();
        for(int i=0;i<trainRoute.length;i++){
            map.put(trainRoute[i],i);
        }
        if(!map.containsKey(seatAvailabilityEntryDto.getFromStation().toString())||!map.containsKey(seatAvailabilityEntryDto.getToStation().toString()))
        {
            return 0;
        }
       int seatBooked=0;
        for(Ticket ticket:ticketList){
            seatBooked+=ticket.getPassengersList().size();
        }
        int count = train.getNoOfSeats()-seatBooked;
        for(Ticket t:ticketList){
            String source=t.getFromStation().toString();
            String destination=t.getToStation().toString();
            if(map.get(seatAvailabilityEntryDto.getToStation().toString())<=map.get(destination)){
                count++;
            }
            else if (map.get(seatAvailabilityEntryDto.getFromStation().toString())>=map.get(destination)){
                count++;
            }
        }
       return count+2;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.
        Train train=trainRepository.findById(trainId).get();
        String distStation=station.toString();
        String arr[]=train.getRoute().split(",");
        boolean found=false;
        for(String s:arr){
            if(s.equals(distStation)){
                found=true;
                break;
            }
        }
    if(found == false){
        throw new Exception("Train is not passing from this station");
          }
    int passengerCount=0;
       List<Ticket>ticketList=train.getBookedTickets();
       for(Ticket ticket:ticketList){
           if(ticket.getFromStation().toString().equals(distStation)){
               passengerCount+=ticket.getPassengersList().size();
           }
       }
        return 0;  // return passanger count
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        Train train=trainRepository.findById(trainId).get();
        int age= Integer.MIN_VALUE;
        if(train.getBookedTickets().size()==0)
            return 0;
        List<Ticket>ticketList=train.getBookedTickets();
        for(Ticket ticket:ticketList){
            List<Passenger> passengerList=ticket.getPassengersList();
            for(Passenger passenger:passengerList)
            {
                age=Math.max(age, passenger.getAge());
            }
        }

        return age;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Integer>TrainList= new ArrayList<>();   // ans list of train passing
        List<Train> trainList1= trainRepository.findAll();   // list of all train
        for(Train t:trainList1){
            String s=t.getRoute();
            String[] stationInRoute=s.split(",");
            for(int i=0;i<stationInRoute.length;i++){
                if(Objects.equals(stationInRoute[i],String.valueOf(station))){
                    //get time windows
                    int startTimeMin= (startTime.getHour()*60)+startTime.getMinute();
                    int lastTimeMin=(endTime.getHour()*60)+endTime.getMinute();
                    int departureTimeInMin =(t.getDepartureTime().getHour()*60)+t.getDepartureTime().getMinute();
                    int arrivalTimeInMin=departureTimeInMin+(i*60);
                    if(arrivalTimeInMin>=startTimeMin && arrivalTimeInMin<=lastTimeMin)
                        TrainList.add(t.getTrainId());
                }
            }

        }

        return TrainList;
    }

}
