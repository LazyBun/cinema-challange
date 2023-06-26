# cinema-challenge

Coding challenge: https://github.com/radbrackets/cinema-challange

## Details

The task is to model the work that the Planner does for planning of the week

### User stories

* Planner wants to be able to schedule a showing for a given movie for every weekday from 8 to 22
* 2 movies cannot be scheduled for the same time and room
* "Every show need to have maintenance slot to clean up whole Room. Every room have different cleaning slot."
  * According to dictionary, the cleaning slot takes place after each show
* Movie can require 3d glasses
* The movies can be of a different type. E.g. a movie marked as Premiere can only be shown after working hours (17-21)
* It's possible for a room to be blocked for specific dates&times


### Challenge notes summarized

* Movie catalog is preexisting, we don't need to take care detailed care of it. That being said, some basic representation will be necessary to complete the task
* Concurrency is something we need to consider
* Read DB & GUI is not necessary

### Glossary

* Planner

Person who works in the cinema and manages schedule of shows

* Show

Movie that is scheduled for specific time and room 

* Movie catalog

List of movies that we have available to show

* Cleaning slot 

Time after each show we need care of cleaning the room.

Note: I considered whether to model "cleaning slot" as implementation of Slot, but since cleaning slot is so strongly tied with Show I decided to just keep it as a parameter in Show itself.

### New terms

* Schedule

A schedule contains information about when the particular room is occupied and by what activity. 

Schedule is a "log" in a sense that future changes to e.g. room cleaning time should not change existing Schedule for this room.  

* Slot

Slot is an item with start and an end that blocks particular amount of time in Schedule. 

* Block

Block is a type of Slot that signifies blockage of a room in a Schedule.

* Premiere

ASSUMPTION: I'm assuming that it's a parameter for the movie itself (not specific Show)

## Run application

```shell
sbt run
```

## Run tests

```shell
sbt test
```
