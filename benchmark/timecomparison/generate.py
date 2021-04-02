import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

def plot_bar_chart():
    api = ['Two Buyer With Negotiate', 'Adder']

    type_generation_without = (3324, 561)
    function_generation_without = (3175, 675)
    channel_generation_without = (370, 115)

    type_generation_with = (0,0)
    function_generation_with = (323, 192)
    channel_generation_with = (0,0)

    df = pd.DataFrame(dict(
        A=[3324, 561],
        B=[6499, 1236],
        C=[6869, 1351],
        D=[323,192]
    ), index=api)

    fig = plt.figure(figsize=(20, 10))
    fig, ax = plt.subplots()

    ab_bar_list = [plt.bar([0, 1], df.C, align='edge',label='channel generation without API', width=0.2),
                   plt.bar([0, 1], df.B, align='edge',label='function generation without API', width=0.2),
                   plt.bar([0, 1], df.A, align='edge',label='type generation without API', width=0.2)]

    cd_bar_list = [plt.bar([0, 1], df.D, align='edge',label='function generation with API', width=-0.2)]

    N = 2
    #
    #
    # # ind = [x for x, _ in enumerate(api)]
    ind = np.arange(N)
    # width = 0.35
    #
    # plt.bar(ind, channel_generation_without, width=0.8, label='channel generation', color='purple')
    # plt.bar(ind, function_generation_without, width=0.8, label='function generation', color='orange',
    #         bottom=channel_generation_without)
    # plt.bar(ind, type_generation_without, width=0.8, label='type generation', color='limegreen', bottom=function_generation_without + channel_generation_without)
    # plt.bar(ind + width, function_generation_with, width=0.8, label='function generation', color='orange')

    plt.ylabel("Time Taken(s)")
    plt.xlabel("Protocol")
    plt.legend(loc="best")
    plt.title("Implementation Time Differences for Protocols")
    ax.set_xticks(ind)
    ax.set_xticklabels(api)
    plt.savefig(f"benchmark/timecomparison/graphs/test_twobuyer.png")