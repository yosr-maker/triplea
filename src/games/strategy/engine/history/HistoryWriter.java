/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */


package games.strategy.engine.history;

import games.strategy.engine.data.Change;
import games.strategy.engine.data.PlayerID;

/**
 * Used to write to a history object.
 *  Delegates should use a DelegateHistoryWriter
 */
public class HistoryWriter implements java.io.Serializable
{
  private final History m_history;
  private HistoryNode m_current;
  
  public HistoryWriter(History history)
  {
    m_history = history;
  }

  /**
   * Can only be called if we are currently in a round or a step
   */
  public void startNextStep(String stepName, String delegateName, PlayerID player, String stepDisplayName)
  {
    //we are being called for the first time
    if (m_current == null)
      startNextRound(1);

    if (isCurrentEvent())
      closeCurrent();

      //stop the current step
    if (isCurrentStep())
      closeCurrent();

    if (!isCurrentRound())
    {
      throw new IllegalStateException("Not in a round");
    }

    Step currentStep = new Step(stepName, delegateName, player, m_history.getChanges().size(), stepDisplayName);
    m_current.add(currentStep);
    m_history.nodesWereInserted(m_current,  new int[] {m_current.getChildCount() -1} );
    m_current = currentStep;
  }

  public void startNextRound(int round)
  {
    if (isCurrentEvent())
      closeCurrent();
    if (isCurrentStep())
      closeCurrent();
    if (isCurrentRound())
      closeCurrent();

    Round currentRound = new Round(round, m_history.getChanges().size());

    ( (HistoryNode) m_history.getRoot()).add(currentRound);
    m_history.reload();
    m_current = currentRound;
  }

  private void closeCurrent()
  {
    //remove steps where nothing happened
    if (isCurrentStep())
    {
      HistoryNode parent = (HistoryNode) m_current.getParent();
      if (m_current.getChildCount() == 0)
      {
        int index = parent.getChildCount()  -1;
        parent.remove(m_current);
        m_history.nodesWereRemoved(parent, new int[] {index}, new Object[] {m_current});
      }

      m_current = parent;
      return;
    }

    ( (IndexedHistoryNode) m_current).setChangeEndIndex(m_history.getChanges().size());
    m_current = (HistoryNode) m_current.getParent();
  }

  public void startEvent(String eventName)
  {
    //close the current event
    if (isCurrentEvent())
      closeCurrent();

    if (!isCurrentStep())
      throw new IllegalStateException("Cant add an event, not a step");

    Event event = new Event(eventName, m_history.getChanges().size());

    m_current.add(event);
    m_history.reload(m_current);
    m_current = event;

  }

  private boolean isCurrentEvent()
  {
    return m_current instanceof Event;
  }

  private boolean isCurrentRound()
  {
    return m_current instanceof Round;
  }

  private boolean isCurrentStep()
  {
    return m_current instanceof Step;
  }

  /**
   * Add a child to the current event.
   */
  public void addChildToEvent(EventChild node)
  {
    if (!isCurrentEvent())
      throw new IllegalStateException("Not in an event");

    m_current.add(node);
    m_history.nodesWereInserted(m_current, new int[] {m_current.getChildCount() - 1});
  }

  /**
   * Add a change to the current event.
   */
  public void addChange(Change change)
  {
    if (!isCurrentEvent())
      throw new IllegalStateException("Not in an event, but trying to add change:" + change);
    m_history.changeAdded(change);
  }

  public void setRenderingData(Object details)
  {
    if (!isCurrentEvent())
      throw new IllegalStateException("Not in an event, but trying to set details:" + details);
    ( (Event) m_current).setRenderingData(details);
    m_history.reload(m_current);
  }
  

}
