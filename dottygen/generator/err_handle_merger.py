import copy
from dottygen.automata.actions import ReceiveErrorAction, ReceiveAction

class ErrorDetectMerger():

    def __init__(self,efsms, unop=True):
        self._efsms = efsms
        self._unop = unop

    def _get_id_string(self, role,state):
        return f"{role}_{state.id}"

    def _all_is_visited_none_terminal(self, states, efsms, visited, crashedSet):
        roles = list(states.keys())
        for role in roles:
            if role not in crashedSet and self._get_id_string(role, states[role]) not in visited and not efsms[role].is_terminal_state(states[role]):
                return False
        return True

    def _can_reduce_states(self, state1, state2, efsm1, efsm2, role1, role2, visted, crashedSet):
           if self._get_id_string(role1, state1) in visted or self._get_id_string(role2, state2) in visted:
               return False
           elif role1 in crashedSet and efsm1.is_send_state(state1):
               return True
           elif role2 in crashedSet and efsm2.is_send_state(state2):
               return True
           elif role1 in crashedSet and role2 in crashedSet:
               return False
           elif role1 in crashedSet and efsm2.is_error_detection_state(state2):
               return list(state2.actions)[0].role == role1
           elif role2 in crashedSet and efsm1.is_error_detection_state(state1):
               return list(state1.actions)[0].role == role2
           elif efsm1.is_terminal_state(state1) and efsm2.is_error_detection_state(state2):
               return list(state2.actions)[0].role == role1
           elif efsm2.is_terminal_state(state2) and efsm1.is_error_detection_state(state1):
               return list(state1.actions)[0].role == role2
           elif efsm1.is_terminal_state(state1) and efsm2.is_send_state(state2):
               return list(state2.actions)[0].role == role1
           elif efsm2.is_terminal_state(state2) and efsm1.is_send_state(state1):
               return list(state1.actions)[0].role == role2
           elif efsm1.is_terminal_state(state1) and efsm2.is_terminal_state(state2):
               return False
           elif efsm1.is_receive_state(state1) and efsm2.is_send_state(state2) or efsm1.is_send_state(state1) and efsm2.is_receive_state(state2):
               return list(state1.actions)[0].role == role2 and list(state2.actions)[0].role == role1
           return False


    def _generate_channel_name(self,count, role1, role2):
        role_name = f"{role1}_{role2}"
        return f"C_{role_name}_{count}"

    def _search_action_index(self, action, actions):
        for i in range(len(actions)):
            if action.label == actions[i].label:
                return i
            if isinstance(actions[i], ReceiveErrorAction):
                return i
        return -1

    def _get_map_index(self, channel_map, state1, state2, combine_map, labels, channel_names, role1, role2, unop):
        for i in range(len(channel_map)):
            if self._get_id_string(role1, state1) in list(channel_names.keys()) and channel_names[self._get_id_string(role1, state1)] in channel_map[i][0]:
                    return i
            if self._get_id_string(role2, state2) in list(channel_names.keys()) and channel_names[self._get_id_string(role2, state2)] in channel_map[i][0]:
                    return i
            if not unop:
                for chan_name in list(combine_map.values()):
                    if chan_name in channel_map[i][0]:
                        return i
                if labels == channel_map[i][1] and len(labels) > 0:
                    return i
        return -1

    def _match_channels(self,state, channel_list, efsm, curr_labels, count, role1, role2, channel_names):

        if self._get_id_string(role1, state) in list(channel_names.keys()):
            return False, {}, count

        channel_type = ""
        if efsm.is_send_state(state):
            channel_type = "OUT"
        elif efsm.is_receive_state(state):
            channel_type = "IN"

        channel_map = {}
        map_index = -1

        for i in range(len(channel_list)):
            (channel_name, type, labels) = channel_list[i]
            if type == channel_type and labels == curr_labels:
                map_index = i
                allSet1 = frozenset([action.label for action in state.actions])
                allSet2 = frozenset(channel_name.keys())
                if allSet1 == allSet2:
                  return True, channel_name, count

        if map_index != -1:
            channel_name, _, _= channel_list.pop(map_index)
            channel_map = channel_name

        labels_set = frozenset([action.label for action in state.actions])
        if labels_set not in list(channel_map.keys()):
                channel_name = self._generate_channel_name(count, role1, role2)
                count+=1
                channel_map[labels_set] = channel_name
        channel_list.append((channel_map, channel_type, copy.deepcopy(curr_labels)))

        return True, channel_map, count


    def _merge_states(self, states, efsms, count_map, channel_map, visited_set, channel_list, channel_names, crashedSet, unop, modify_map, payloads):

        if self._all_is_visited_none_terminal(states, efsms, visited_set, crashedSet):
            return

        roles = list(states.keys())
        for i in range(len(roles)):
            for j in range(i+1, len(roles)):
                if(self._can_reduce_states(states[roles[i]], states[roles[j]], efsms[roles[i]], efsms[roles[j]], roles[i], roles[j], visited_set, crashedSet)):

                    state_i = states[roles[i]]
                    state_j = states[roles[j]]
                    efsms_i = efsms[roles[i]]
                    efsms_j = efsms[roles[j]]
                    ij_key = f"{roles[i]}_{roles[j]}"
                    ji_key = f"{roles[j]}_{roles[i]}"
                    ij_set = frozenset(list([roles[i], roles[j]]))

                    if efsms_i.is_send_state(state_i) and roles[i] in crashedSet:
                        visited_set.add(self._get_id_string(roles[i], state_i))
                        for action in list(state_i.actions):
                            new_states = copy.deepcopy(states)
                            new_states[roles[i]] = action.succ
                            self._merge_states(new_states, efsms, count_map, channel_map, visited_set, channel_list, channel_names, crashedSet, unop, modify_map, payloads)
                        visited_set.remove(self._get_id_string(roles[i], state_i))
                        return

                    elif efsms_j.is_send_state(state_j) and roles[j] in crashedSet:
                        visited_set.add(self._get_id_string(roles[j], state_j))
                        for action in list(state_j.actions):
                            new_states = copy.deepcopy(states)
                            new_states[roles[j]] = action.succ
                            self._merge_states(new_states, efsms, count_map, channel_map, visited_set, channel_list, channel_names, crashedSet, unop, modify_map, payloads)
                        visited_set.remove(self._get_id_string(roles[j], state_j))
                        return

                    elif (roles[i] in crashedSet or efsms_i.is_terminal_state(state_i)) and efsms_j.is_error_detection_state(state_j):
                        visited_set.add(self._get_id_string(roles[j], state_j))
                        if not roles[i] in crashedSet:
                            visited_set.add(self._get_id_string(roles[i], state_i))
                        new_states = copy.deepcopy(states)
                        new_states[roles[j]] = state_j.error_detection.succ
                        self._merge_states(new_states, efsms, count_map, channel_map, visited_set, channel_list, channel_names, crashedSet, unop, modify_map, payloads)
                        visited_set.remove(self._get_id_string(roles[j], state_j))
                        if not roles[i] in crashedSet:
                            visited_set.remove(self._get_id_string(roles[i], state_i))

                    elif (roles[j] in crashedSet or efsms_j.is_terminal_state(state_j)) and efsms_i.is_error_detection_state(state_i):
                        visited_set.add(self._get_id_string(roles[i], state_i))
                        if not roles[j] in crashedSet:
                            visited_set.add(self._get_id_string(roles[j], state_j))
                        new_states = copy.deepcopy(states)
                        new_states[roles[i]] = state_i.error_detection.succ
                        self._merge_states(new_states, efsms, count_map, channel_map, visited_set, channel_list, channel_names, crashedSet, unop, modify_map, payloads)
                        visited_set.remove(self._get_id_string(roles[i], state_i))
                        if not roles[j] in crashedSet:
                            visited_set.remove(self._get_id_string(roles[j], state_j))

                    elif efsms_i.is_terminal_state(state_i) and efsms_j.is_send_state(state_j):
                        visited_set.add(self._get_id_string(roles[i], state_i))
                        visited_set.add(self._get_id_string(roles[j], state_j))
                        for action in list(state_j.actions):
                            new_states = copy.deepcopy(states)
                            new_states[roles[j]] = action.succ
                            self._merge_states(new_states, efsms, count_map, channel_map, visited_set, channel_list, channel_names, crashedSet, unop, modify_map, payloads)
                        visited_set.remove(self._get_id_string(roles[i], state_i))
                        visited_set.remove(self._get_id_string(roles[j], state_j))

                    elif efsms_j.is_terminal_state(state_j) and efsms_i.is_send_state(state_i):
                        visited_set.add(self._get_id_string(roles[i], state_i))
                        visited_set.add(self._get_id_string(roles[j], state_j))
                        for action in list(state_i.actions):
                            new_states = copy.deepcopy(states)
                            new_states[roles[i]] = action.succ
                            self._merge_states(new_states, efsms, count_map, channel_map, visited_set, channel_list, channel_names, crashedSet, unop, modify_map, payloads)
                        visited_set.remove(self._get_id_string(roles[i], state_i))
                        visited_set.remove(self._get_id_string(roles[j], state_j))

                    elif efsms_j.is_send_state(state_j) and efsms_i.is_receive_state(state_i) or efsms_i.is_send_state(state_i) and efsms_j.is_receive_state(state_j):

                         if efsms_i.is_receive_state(state_i):
                             new_labels = set([action.label for action in list(state_i.actions)])
                         else:
                             new_labels = set([action.label for action in list(state_j.actions)])

                         countij = count_map[ij_key] if ij_key in list(count_map.keys()) else 1
                         countji = count_map[ji_key] if ji_key in list(
                             count_map.keys()) else 1
                         channel_list_ij = channel_list[ij_key] if ij_key in list(channel_list
                                                                                  .keys()) else []
                         channel_list_ji = channel_list[ji_key] if ji_key in list(channel_list
                                                                                  .keys()) else []


                         if not unop:
                            has_matched_i, new_chan_name_i, countij = self._match_channels(state_i, channel_list_ij, efsms_i,
                                                                                      new_labels, countij, roles[i], roles[j], channel_names)
                            has_matched_j, new_chan_name_j, countji = self._match_channels(state_j, channel_list_ji, efsms_j,
                                                                                      new_labels, countji, roles[j], roles
                                                                                      [i], channel_names)
                            combine_map = copy.deepcopy(new_chan_name_i)
                            combine_map.update(new_chan_name_j)

                         else:
                             combine_map = {}
                             has_matched_i = False
                             has_matched_j = False


                         map_ij = channel_map[ij_set] if ij_set in list(
                             channel_map.keys()) else []

                         map_index = self._get_map_index(map_ij, state_i, state_j, combine_map, new_labels, channel_names, roles[i], roles[j], unop)

                         channel_set = set()
                         labels = new_labels
                         if map_index != -1:
                             channel_set, labels = map_ij.pop(map_index)

                         add_chan_name = False

                         if labels != new_labels:
                             if len(labels) < len(new_labels):
                                 for chan in channel_set:
                                     modify_map[chan] = copy.deepcopy(new_labels)
                             else:
                                  add_chan_name = True

                         is_send_error = False
                         if efsms_i.is_error_detection_state(state_i) or efsms_j.is_error_detection_state(state_j):
                             is_send_error = True

                         if has_matched_i:
                             chan_name = new_chan_name_i[frozenset([action.label for action in state_i.actions])]
                             channel_names[self._get_id_string(roles[i], state_i)] = chan_name
                             channel_set.add(chan_name)
                             if add_chan_name:
                                 modify_map[chan_name] = copy.deepcopy(labels)
                         elif not efsms_i.is_terminal_state(state_i) and not self._get_id_string(roles[i], state_i) in list(channel_names.keys()):
                             channel_name = self._generate_channel_name(countij, roles[i], roles[j])
                             channel_set.add(channel_name)
                             if add_chan_name:
                                 modify_map[channel_name] = copy.deepcopy(labels)
                             channel_names[self._get_id_string(roles[i], state_i)] = channel_name
                             countij += 1
                         if efsms_i.is_send_state(state_i) and is_send_error:
                             state_i.set_send_error()

                         if has_matched_j:
                             chan_name = new_chan_name_j[frozenset([action.label for action in state_j.actions])]
                             channel_names[self._get_id_string(roles[j], state_j)] = chan_name
                             channel_set.add(chan_name)
                             if add_chan_name:
                                 modify_map[chan_name] = copy.deepcopy(labels)
                         elif not efsms_j.is_terminal_state(state_j) and not self._get_id_string(roles[j], state_j) in list(channel_names.keys()):
                             channel_name = self._generate_channel_name(countji, roles[j], roles[i])
                             channel_set.add(channel_name)
                             channel_names[self._get_id_string(roles[j], state_j)] = channel_name
                             if add_chan_name:
                                 modify_map[channel_name] = copy.deepcopy(labels)
                             countji += 1
                         if efsms_j.is_send_state(state_j) and is_send_error:
                             state_j.set_send_error()

                         if len(channel_set) > 0:
                             map_ij.append((channel_set, copy.deepcopy(new_labels)))

                         count_map[ij_key] = countij
                         count_map[ji_key] = countji
                         channel_list[ij_key] = channel_list_ij
                         channel_list[ji_key] = channel_list_ji
                         channel_map[ij_set] = map_ij

                         visited_set.add(self._get_id_string(roles[i], state_i))
                         visited_set.add(self._get_id_string(roles[j], state_j))

                         for action_i in list(state_i.actions):
                             for action_j in list(state_j.actions):
                                 if action_i.label == action_j.label:
                                     payloads[action_i.label] = action_i.payloads
                                     new_states = copy.deepcopy(states)
                                     new_states[roles[i]] = action_i.succ
                                     new_states[roles[j]] = action_j.succ
                                     self._merge_states(new_states, efsms, count_map, channel_map, visited_set, channel_list, channel_names, crashedSet, unop, modify_map, payloads)

                         if efsms_i.is_error_detection_state(state_i):
                             crashedSet.add(roles[j])
                             for action in list(state_j.actions):
                                 new_states = copy.deepcopy(states)
                                 new_states[roles[i]] = state_i.error_detection.succ
                                 new_states[roles[j]] = action.succ
                                 self._merge_states(new_states, efsms, count_map, channel_map, visited_set, channel_list, channel_names, crashedSet, unop, modify_map, payloads)
                             crashedSet.remove(roles[j])


                         elif efsms_j.is_error_detection_state(state_j):
                             crashedSet.add(roles[i])
                             for action in list(state_i.actions):
                                 new_states = copy.deepcopy(states)
                                 new_states[roles[j]] = state_j.error_detection.succ
                                 new_states[roles[i]] = action.succ
                                 self._merge_states(new_states, efsms, count_map, channel_map, visited_set, channel_list, channel_names, crashedSet, unop, modify_map, payloads)
                             crashedSet.remove(roles[i])

                         visited_set.remove(self._get_id_string(roles[i], state_i))
                         visited_set.remove(self._get_id_string(roles[j], state_j))


                    else:
                        print("YOU SHOULDNT COME HEREEEEE")
                        assert(False)

                    return


    def _assign_channel_names(self, state, efsm, role, channel_names, visited):
         if state.id in visited or efsm.is_terminal_state(state):
             return
         visited.add(state.id)
         if not state.has_channel_name:
             assert self._get_id_string(role, state) in list(channel_names.keys())
             state.set_channel_name(channel_names[self._get_id_string(role, state)])
         for action in list(state.actions):
             self._assign_channel_names(action.succ, efsm, role, channel_names, visited)
         if efsm.is_error_detection_state(state):
             self._assign_channel_names(state.error_detection.succ, efsm, role, channel_names, visited)
         visited.remove(state.id)


    def _modify_states(self, state, efsm, role, channel_names, modify_map, payloads, visited):
         if state.id in visited or efsm.is_terminal_state(state):
             return
         visited.add(state.id)
         if self._get_id_string(role, state) in list(channel_names.keys()) and \
                 channel_names[self._get_id_string(role, state)] in list(modify_map.keys()):
             all_labels = modify_map[channel_names[self._get_id_string(role, state)]]
             for label in all_labels:
                 if label not in [action.label for action in state.actions]:
                     efsm.add_modified_receive(state, f"{list(state.actions)[0].role}?{label}({','.join(payloads[label])})")
         for action in list(state.actions):
             self._modify_states(action.succ, efsm, role, channel_names, modify_map, payloads, visited)
         if efsm.is_error_detection_state(state):
             self._modify_states(state.error_detection.succ, efsm, role, channel_names, modify_map, payloads, visited)
         visited.remove(state.id)


    def merge(self):
        channel_map = {}
        channel_names = {}
        modify_map = {}
        all_init_states = {}
        payloads = {}
        keys = list(self._efsms.keys())
        for i in range(len(keys)):
           all_init_states[keys[i]] = self._efsms[keys[i]][self._efsms[keys[i]].initial_state.id]
        self._merge_states(all_init_states, self._efsms, {}, channel_map, set(), {}, channel_names, set(), False, modify_map, payloads)

        for i in range(len(keys)):
            self._modify_states(self._efsms[keys[i]][self._efsms[keys[i]].initial_state.id],
                                       self._efsms[keys[i]], keys[i], channel_names, modify_map
                                       ,payloads, set())
        for i in range(len(keys)):
           all_init_states[keys[i]] = self._efsms[keys[i]][self._efsms[keys[i]].initial_state.id]
        self._merge_states(all_init_states, self._efsms, {}, channel_map, set(), {}, channel_names, set(), self._unop, modify_map, payloads)

        for i in range(len(keys)):
           self._assign_channel_names(self._efsms[keys[i]][self._efsms[keys[i]].initial_state.id], self._efsms[keys[i]], keys[i], channel_names
                                      ,set())
        result = []
        for matched_list in list(channel_map.values()):
            for matched in matched_list:
                result += [matched[0]]
        return result