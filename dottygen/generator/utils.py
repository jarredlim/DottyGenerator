def first_char_lower(input):
    return input[0].lower() + input[1:]

def get_labels_name(labels):
        label_name = ""
        for i in range(len(labels)):
            label_name += labels[i].get_name()
            if i != len(labels) - 1:
                label_name += "|"
        return label_name