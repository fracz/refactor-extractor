/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.aida.fsm;

import com.badlogic.gdx.aida.msg.Telegram;

/**
 * A state machine manages the state transitions of its entity. Additionally,
 * the state machine may be delegated by the entity to handle its messages.
 *
 * @author davebaol
 */
public interface StateMachine<E> {

	public void update();

	public void changeState(State<E> newState);

	public void revertToPreviousState();

	public void setInitialState(State<E> state);

	public void setGlobalState(State<E> state);

	public State<E> getCurrentState();

	public State<E> getGlobalState();

	public boolean isInState(State<E> state);

	public boolean handleMessage(Telegram telegram);
}