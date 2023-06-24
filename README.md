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

* Premiere

Not defined in task, mentioned in user stories. ASSUMPTION: I'm assuming that it's a parameter for the movie itself (not specific showing), since it says in task description that "Not every movie are equal"

* Cleaning slot 

Time slot after each show we need care of cleaning the room.

### New terms

* Slot

TODO: proper definition

Item in a Schedule. Can be either a Show or a Block

* Block

TODO: proper definition

Block is a type of Slot that signifies blockage

## Run application

```shell
sbt run
```

## Run tests

```shell
sbt test
```
