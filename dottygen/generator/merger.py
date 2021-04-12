class Merger():

    def __init__(self,efsms):
        self._efsms = efsms

    def _is_terminal_or_visited(self, efsm, states, visited):
        for state in states:
           if not efsm.is_terminal_state(state) and not state.id in visited:
               return False
        return True

    def _is_two_party(self, efsm, states, role, visited):
        for state in states:
            if not efsm.is_terminal_state(state):
                actions = list(state.actions)
                for action in actions:
                    if action.role != role:
                        return False
        return True


    def _remove_third_pary(self, states, role, efsm, visited):
        while not self._is_two_party(efsm, states, role, visited):
            old_states = set([state.id for state in states])
            new_states = []
            while len(states) > 0:
                state = states.pop(0)
                if self._is_two_party(efsm, [state], role, visited) and not self._is_terminal_or_visited(efsm,[state], visited):
                    new_states.append(state)
                elif not self._is_terminal_or_visited(efsm, [state], visited):
                    visited.add(state.id)
                    actions = list(state.actions)
                    for action in actions:
                        new_states.append(action.succ)
            # if old_states ==  set([state.id for state in new_states]):
            #     print("hhello")
            #     for state in new_states:
            #         if self._is_two_party(efsm, [state], role, visited) and not state.id in visited and not efsm.is_terminal_state(state):
            #             print(state)
            #             states.append(state)
            # else:
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

    def _get_map_index(self, channel_map, states, new_chan_names, labels):
        for i in range(len(channel_map)):
            for state in states:
                if state.has_channel_name and state.channel_name in channel_map[i][0]:
                    return i
            for chan_name in new_chan_names:
                if chan_name in channel_map[i][0]:
                    return i
            if labels == channel_map[i][1] and len(labels) > 0:
                return i
        return -1

    def _match_channels(self,states1, channel_list, efsm1, count, role1, role2):

        if len(states1) == 0:
            return False, "null", count

        for state in states1:
            if efsm1.is_terminal_state(state):
                return False, "null", count

        labels_set = set([action.label for action in states1[0].actions])

        for state in states1:
            if state.has_channel_name or set([action.label for action in state.actions]) != labels_set:
                return False, "null", count

        channel_type = ""
        if efsm1.is_send_state(states1[0]):
            channel_type = "OUT"
        elif efsm1.is_receive_state(states1[0]):
            channel_type = "IN"

        for channel_name, type, labels in channel_list:
            if type == channel_type and labels == labels_set:
                return True, channel_name, count

        channel_name = self._generate_channel_name(count, role1, role2)
        channel_list.append((channel_name, channel_type, labels_set))

        return True, channel_name, count+1


    def _merge_two_state(self,role1, role2, efsm1, efsm2, state1, state2, count12, count21, channel_map, visited1, visited2, channel_list1, channel_list2):
        states1 = [state1]
        states2 = [state2]

        old_visited1 = set(visited1)
        old_visited2 = set(visited2)

        states1 = self._remove_third_pary(states1,role2, efsm1, visited1)
        states2 = self._remove_third_pary(states2, role1, efsm2, visited2)

        if self._is_terminal_or_visited(efsm1, states1, visited1) and self._is_terminal_or_visited(efsm2, states2, visited2):
            return count12, count21

        has_matched1, new_chan_name1, count12 = self._match_channels(states1, channel_list1, efsm1, count12, role1, role2)
        has_matched2, new_chan_name2, count21 = self._match_channels(states2, channel_list2, efsm2, count21, role2, role1)

        labels = set()
        not_terminal = True
        for state in states1 + states2:
            if efsm1.is_terminal_state(state) or efsm2.is_terminal_state(state):
                not_terminal = False
                labels = set()
                break

        if not_terminal:
            for state in states1 + states2:
                labels = labels.union(set([action.label for action in state.actions]))


        map_index = self._get_map_index(channel_map, states1 + states2, [new_chan_name1] + [new_chan_name2], labels)
        channel_set = set()
        if map_index != -1:
            channel_set, labels = channel_map.pop(map_index)

        for state in states1:
            if has_matched1:
                state.set_channel_name(new_chan_name1)
                channel_set.add(new_chan_name1)
            elif not efsm1.is_terminal_state(state) and not state.has_channel_name:
                channel_name = self._generate_channel_name(count12, role1, role2)
                channel_set.add(channel_name)
                state.set_channel_name(channel_name)
                count12 += 1
        for state in states2:
            if has_matched2:
                state.set_channel_name(new_chan_name2)
                channel_set.add(new_chan_name2)
            elif not efsm2.is_terminal_state(state) and not state.has_channel_name:
                channel_name = self._generate_channel_name(count21, role2, role1)
                channel_set.add(channel_name)
                state.set_channel_name(channel_name)
                count21 += 1

        if len(channel_set) > 0:
            channel_map.append((channel_set, labels))

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
                count12, count21 = self._merge_two_state(role1, role2, efsm1, efsm2, action1.succ, action2.succ, count12, count21, channel_map, visited1, visited2, channel_list1, channel_list2)
            assert(action2 is not None)
            while self._search_action_index(action2, actions1) != -1 :
                action1 = actions1.pop(self._search_action_index(action2, actions1))
                count12, count21 = self._merge_two_state(role1, role2, efsm1, efsm2, action1.succ, action2.succ, count12, count21, channel_map, visited1, visited2, channel_list1, channel_list2)

        visited1 = old_visited1
        visited2 = old_visited2
        return count12, count21


    def merge(self):
        channel_map = []
        keys = list(self._efsms.keys())
        for i in range(len(keys)):
            for j in range(i+1, len(keys)):
                new_map = []
                efsm1 = self._efsms[keys[i]]
                efsm2 = self._efsms[keys[j]]
                self._merge_two_state(keys[i], keys[j], efsm1, efsm2, efsm1[efsm1.initial_state.id], efsm2[efsm2.initial_state.id], 1,1, new_map,set(), set(), [], [])
                channel_map = channel_map + new_map
        return [map[0] for map in channel_map]