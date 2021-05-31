from dataclasses import dataclass, field
import typing
from dottygen.automata.actions import Action

from dottygen.automata.states import NonTerminalState, ReceiveState, SendState, State, TerminalState, ReceiveErrorState

class EfsmBuilder:

    _roles: typing.Set[str]
    _send_states: typing.Dict[str, SendState]
    _receive_states: typing.Dict[str, ReceiveState]
    _error_detection_states: typing.Dict[str, ReceiveErrorState]
    _initial_state_id: str
    _terminal_state_candidates: typing.Set[str]

    def __init__(self, nodes: typing.List[str]):
        self._roles = set()
        self._send_states = {}
        self._receive_states = {}
        self._error_detection_states = {}
        self._initial_state_id = str(min(int(node) for node in nodes))
        self._terminal_state_candidates = set(nodes)

    def add_action_to_send_state(self, action: 'Action'):
        """Add the specified 'action' as a transition to the send state."""

        state_id = action.state_id
        send_state = self._send_states.get(state_id, SendState(state_id))
        send_state.add_action(action)

        # Register send state to EFSM.
        self._send_states[state_id] = send_state

        self._roles.add(action.role)
        self._terminal_state_candidates.discard(state_id)

    def add_action_to_receive_state(self, action: 'Action', error_detect=False):
        """Add the specified 'action' as a transition to the receive state."""

        state_id = action.state_id
        receive_state = self._receive_states.get(state_id, ReceiveState(state_id))

        if not error_detect:
            receive_state.add_action(action)
            self._receive_states[state_id] = receive_state
        else:
            receive_state.add_error_detection(action)
            self._error_detection_states[state_id] = receive_state

        self._roles.add(action.role)
        self._terminal_state_candidates.discard(state_id)
    
    def build(self) -> 'EFSM':
        """Build concrete EFSM instance."""

        # if len(self._terminal_state_candidates) > 1:
        #     raise Exception(f'Too many candidates for terminal state: {self._terminal_state_candidates}')

        terminal_state_id = self._terminal_state_candidates \
                            if self._terminal_state_candidates else None

        return EFSM(self._roles,
                    self._send_states,
                    self._receive_states,
                    self._error_detection_states,
                    self._initial_state_id,
                    terminal_state_id)

@dataclass
class EFSM:

    _roles: typing.Set[str]
    _send_states: typing.Dict[str, SendState]
    _receive_states: typing.Dict[str, ReceiveState]
    _error_detection_states: typing.Dict[str, ReceiveErrorState]
    _initial_state_id: str
    _terminal_state_id: typing.Optional[typing.Set[str]]

    _states: typing.Dict[str, State] = field(init=False)

    def __post_init__(self):

        # Accumulate all states in a single dictionary.
        self._states = dict(**self._send_states, **self._receive_states)
        if self._terminal_state_id is not None:
            for terminal_state in self._terminal_state_id:
                self._states[terminal_state] = TerminalState(terminal_state)

        # Deep linking of successor states in actions.
        for state in self.nonterminal_states:
            for action in state.actions:
                action.succ = self[action.succ_id]
            if not state.error_detection is None:
                state.error_detection.succ = self[state.error_detection.succ_id]

    
    @property
    def other_roles(self) -> typing.Set[str]:
        """Return the other roles that this endpoint interacts with."""

        return self._roles

    @property
    def states(self) -> typing.Iterable[State]:
        """Return all State instances for this EFSM -- send(s), receive(s) and terminal (if any)."""

        return self._states.values()

    @property
    def send_states(self) -> typing.Iterable[SendState]:
        """Return all SendState instances for this EFSM."""

        return self._send_states.values()

    @property
    def receive_states(self) -> typing.Iterable[ReceiveState]:
        """Return all ReceiveState instances for this EFSM."""

        return self._receive_states.values()

    @property
    def nonterminal_states(self) -> typing.Iterable[NonTerminalState]:
        """Return all NonTerminalState instances for this EFSM."""

        return dict(**self._send_states, **self._receive_states).values()

    @property
    def initial_state(self) -> State:
        """Return the State instance for the initial state for this EFSM."""

        return self[self._initial_state_id]

    def add_modified_receive(self, state: ReceiveState, label):
        dst = str(max([int(state1.id) for state1 in self._states.values()] + [int(terminal_state) for terminal_state in self._terminal_state_id]) + 1)
        term_state = TerminalState(dst)
        term_state.set_unreachable()
        self._terminal_state_id.add(dst)
        self._states[dst] = term_state
        action = Action.parse(label, state.id, dst)
        action.succ = term_state
        state.add_action(action)


    def has_terminal_state(self) -> bool:
        """Return true iff this EFSM has a terminal state."""

        return self._terminal_state_id is not None

    def is_send_state(self, state: State) -> bool:
        """Type guard for 'state' to check if it is a SendState."""

        return state.id in self._send_states
    
    def is_receive_state(self, state: State) -> bool:
        """Type guard for 'state' to check if it is a ReceiveState."""

        return state.id in self._receive_states

    def is_error_detection_state(self, state: State) -> bool:
        """Type guard for 'state' to check if it is a ReceiveState."""

        return state.id in self._error_detection_states

    def is_terminal_state(self, state: State):
        """Type guard for 'state' to check if it is a TerminalState."""

        return self._terminal_state_id is not None and state.id in self._terminal_state_id

    @property
    def terminal_state(self) -> State:
        """Return the State instance for the terminal state for this EFSM.
        Throws an exception if there is no terminal state."""

        if self._terminal_state_id is None:
            raise Exception(f'Trying to access non-existent terminal state')

        return self[self._terminal_state_id]

    def __getitem__(self, state_id: str) -> State:
        """Index into the EFSM by the specified 'state_id' to get the State
        instance identified by the specified 'state_id'."""
        return self._states[state_id]