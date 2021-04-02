class Counter:

    _function_timer = 0
    _type_timer = 0
    _class_timer = 0
    _efsm_timer = 0
    _nuscr_timer = 0
    _merge_timer = 0
    _role = "null"
    _print_list = []

    def set_role(self, role):
        self._role = role

    def _add_print(self, time, description):
        self._print_list.append(f"Role {self._role}: {description} took {time} s")

    def print_out(self):
        for output in self._print_list:
            print(output)

    def add_function_time(self, time):
        if time >= 0:
            self._add_print(time, "Generating function")
            self._function_timer += time

    def add_type_time(self, time):
        if time >= 0:
            self._add_print(time, "Generating type")
            self._type_timer += time

    def add_class_time(self, time):
        if time >= 0:
            self._add_print(time, "Generating class instance")
            self._class_timer += time

    def add_efsm_time(self, time):
        if time >= 0:
            self._add_print(time, "Generating efsm")
            self._efsm_timer += time

    def add_nuscr_time(self, time):
        if time >= 0:
            self._add_print(time, "Generating nuscr")
            self._nuscr_timer += time

    def add_merge_time(self, time):
        if time >= 0:
            self._add_print(time, "merging")
            self._merge_timer += time

    def get_function_time(self):
        return self._function_timer

    def get_nuscr_time(self):
        return self._nuscr_timer

    def get_efsm_time(self):
        return self._efsm_timer

    def get_class_time(self):
        return self._class_timer

    def get_type_time(self):
        return self._type_timer

    def get_merge_time(self):
        return self._merge_timer
