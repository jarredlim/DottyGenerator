class Merger():

    def __init__(self,efsms):
        self._efsms = efsms

    def _is_terminal_or_visited(self, efsm, states, visited):
        for state in states:
           if not efsm.is_terminal_state(state) and not state.id in visited:
               return False
        return True

    def _is_two_party(self, efsm, states, role, visited):
        if self._is_terminal_or_visited(efsm, states, visited):
            return True
        for state in states:
            if not efsm.is_terminal_state(state):
                actions = list(state.actions)
                for action in actions:
                    if action.role != role:
                        return False
        return True


    def _remove_third_pary(self, states, role, efsm, visited):
        while not self._is_two_party(efsm, states, role, visited):
            new_states = []
            while len(states) > 0:
                state = states.pop(0)
                if self._is_two_party(efsm, [state], role, visited):
                    new_states.append(state)
                else:
                    visited.add(state.id)
                    actions = list(state.actions)
                    for action in actions:
                        new_states.append(action.succ)
            states = new_states
        return states

    def _generate_channel_name(self,count, role1, role2):
        role_name = f"{role1}_{role2}"
        return f"C_{role_name}_{count}"

    def _get_channel_name(self, states1, states2):
        for state in states1:
            if state.has_nested:
                return True,state.channel_name, state.nested_type
        for state in states2:
            if state.has_nested:
                return True, state.channel_name, state.nested_type
        return False, "", ""

    def _search_action_index(self, action, actions):
        for i in range(len(actions)):
            if action.label == actions[i].label:
                return i
        return -1

    def _get_map_index(self, channel_map, states):
        for i in range(len(channel_map)):
            for state in states:
                if state.has_channel_name and state.channel_name in channel_map[i]:
                    return i
        return -1

    def _merge_two_state(self,role1, role2, efsm1, efsm2, state1, state2, count12, count21, channel_map, visited1, visited2):
        states1 = [state1]
        states2 = [state2]

        old_visited1 = set(visited1)
        old_visited2 = set(visited2)

        states1 = self._remove_third_pary(states1,role2, efsm1, visited1)
        states2 = self._remove_third_pary(states2, role1, efsm2, visited2)

        if self._is_terminal_or_visited(efsm1, states1, visited1) and self._is_terminal_or_visited(efsm2, states2, visited2):
            return count12, count21

        map_index = self._get_map_index(channel_map, states1+states2)
        channel_set = set()
        if map_index != -1:
            channel_set = channel_map.pop(map_index)

        for state in states1:
            if not efsm1.is_terminal_state(state) and not state.has_channel_name:
                channel_name = self._generate_channel_name(count12, role1, role2)
                channel_set.add(channel_name)
                state.set_channel_name(channel_name)
                count12 += 1
        for state in states2:
            if not efsm2.is_terminal_state(state) and not state.has_channel_name:
                channel_name = self._generate_channel_name(count21, role2, role1)
                channel_set.add(channel_name)
                state.set_channel_name(channel_name)
                count21 += 1

        channel_map.append(channel_set)

        actions1 = []
        actions2 = []
        for state in states1:
            if not efsm1.is_terminal_state(state):
                visited1.add(state1.id)
                actions1 += list(state.actions)
        for state in states2:
            if not efsm2.is_terminal_state(state):
                visited2.add(state2.id)
                actions2 += list(state.actions)

        while len(actions1) > 0 and len(actions2) > 0:
            action1 = actions1.pop(0)
            while self._search_action_index(action1, actions2) != -1 :
                action2 = actions2.pop(self._search_action_index(action1, actions2))
                count12, count21 = self._merge_two_state(role1, role2, efsm1, efsm2, action1.succ, action2.succ, count12, count21, channel_map, visited1, visited2)
            assert(action2 is not None)
            while self._search_action_index(action2, actions1) != -1 :
                action1 = actions1.pop(self._search_action_index(action2, actions1))
                count12, count21 = self._merge_two_state(role1, role2, efsm1, efsm2, action1.succ, action2.succ, count12, count21, channel_map, visited1, visited2)

        visited1 = old_visited1
        visited2 = old_visited2
        return count12, count21


    def merge(self):
        channel_map = []
        keys = list(self._efsms.keys())
        for i in range(len(keys)):
            for j in range(i+1, len(keys)):
                efsm1 = self._efsms[keys[i]]
                efsm2 = self._efsms[keys[j]]
                self._merge_two_state(keys[i], keys[j], efsm1, efsm2, efsm1[efsm1.initial_state.id], efsm2[efsm2.initial_state.id], 1,1, channel_map,set(), set())
        return channel_map