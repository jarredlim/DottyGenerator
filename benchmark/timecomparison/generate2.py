import matplotlib.pyplot as plt

def plot_bar_chart():
    labels = ['Function Gen\nW Tool', 'Function Gen\nW/O Tool', 'Type Gen\nW/O tool', 'Channel Gen\nW/O Tool']
    data = [192, 675, 561, 115]
    plt.xticks(range(len(data)), labels)
    plt.title("Implementation Time Differences for Adder Protocol")
    plt.ylabel("Time Taken(s)")
    bar = plt.bar(range(len(data)), data)
    bar[0].set_color('r')
    plt.savefig(f"benchmark/timecomparison/graphs/test_adder.png")