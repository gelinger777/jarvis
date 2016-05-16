package common.statemachine;

import java.util.*;

public abstract class StateMachine <State, Event, P> {

  protected Set<State> states = new HashSet<>();

  public StateMachine(State... states){

  }

  public abstract State state();

  public abstract void apply(Event event);

  public abstract P produce();

}


enum States{
  ONE,
  TWO,
  TREE
}

